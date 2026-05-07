package main

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/gorilla/websocket"
	"github.com/segmentio/kafka-go"
)

type GiftSentEvent struct {
	GiftEventId    string    `json:"giftEventId"`
	StreamId       string    `json:"streamId"`
	SenderUserId   string    `json:"senderUserId"`
	SenderUsername string    `json:"senderUsername"`
	GiftType       string    `json:"giftType"`
	// giftPrice comes from Java BigDecimal and is usually encoded as a JSON number.
	// Keep it as raw JSON so we can accept both number and string without failing unmarshalling.
	GiftPrice      json.RawMessage `json:"giftPrice"`
	SentAt         time.Time `json:"sentAt"`
}

type Hub struct {
	mu      sync.RWMutex
	clients map[string]map[*websocket.Conn]struct{} // streamId -> conns
}

func NewHub() *Hub {
	return &Hub{clients: make(map[string]map[*websocket.Conn]struct{})}
}

func (h *Hub) Add(streamId string, c *websocket.Conn) {
	h.mu.Lock()
	defer h.mu.Unlock()
	if h.clients[streamId] == nil {
		h.clients[streamId] = make(map[*websocket.Conn]struct{})
	}
	h.clients[streamId][c] = struct{}{}
}

func (h *Hub) Remove(streamId string, c *websocket.Conn) {
	h.mu.Lock()
	defer h.mu.Unlock()
	if h.clients[streamId] == nil {
		return
	}
	delete(h.clients[streamId], c)
	if len(h.clients[streamId]) == 0 {
		delete(h.clients, streamId)
	}
}

func (h *Hub) Broadcast(streamId string, payload []byte) {
	h.mu.RLock()
	conns := h.clients[streamId]
	h.mu.RUnlock()
	for c := range conns {
		_ = c.SetWriteDeadline(time.Now().Add(5 * time.Second))
		if err := c.WriteMessage(websocket.TextMessage, payload); err != nil {
			_ = c.Close()
			h.Remove(streamId, c)
		}
	}
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool { return true }, // MVP
}

func main() {
	port := env("PORT", "8090")
	kafkaBootstrap := env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
	topic := env("KAFKA_TOPIC_GIFT_SENT", "gift.sent")
	groupId := env("KAFKA_GROUP_ID", "notification-service-go")

	hub := NewHub()

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		streamId := r.URL.Query().Get("streamId")
		if streamId == "" {
			http.Error(w, "missing streamId", http.StatusBadRequest)
			return
		}
		conn, err := upgrader.Upgrade(w, r, nil)
		if err != nil {
			return
		}
		hub.Add(streamId, conn)
		log.Printf("ws connected streamId=%s", streamId)

		// Read loop just to detect disconnects.
		go func() {
			defer func() {
				_ = conn.Close()
				hub.Remove(streamId, conn)
				log.Printf("ws disconnected streamId=%s", streamId)
			}()
			for {
				if _, _, err := conn.ReadMessage(); err != nil {
					return
				}
			}
		}()
	})

	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	go consumeAndBroadcast(ctx, kafkaBootstrap, topic, groupId, hub)

	log.Printf("notification-service-go listening on :%s (kafka=%s topic=%s)", port, kafkaBootstrap, topic)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}

func consumeAndBroadcast(ctx context.Context, bootstrap, topic, groupId string, hub *Hub) {
	r := kafka.NewReader(kafka.ReaderConfig{
		Brokers:        []string{bootstrap},
		Topic:          topic,
		GroupID:        groupId,
		MinBytes:       1,
		MaxBytes:       10e6,
		CommitInterval: time.Second,
	})
	defer r.Close()

	for {
		m, err := r.ReadMessage(ctx)
		if err != nil {
			if ctx.Err() != nil {
				return
			}
			log.Printf("kafka read error: %v", err)
			time.Sleep(time.Second)
			continue
		}
		var evt GiftSentEvent
		if err := json.Unmarshal(m.Value, &evt); err != nil {
			log.Printf("bad event json: %v", err)
			continue
		}
		// Broadcast the original event JSON to avoid any re-encoding surprises.
		hub.Broadcast(evt.StreamId, m.Value)
	}
}

func env(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

