FROM golang:1.26-alpine AS build
WORKDIR /src
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /out/notification-service-go .

FROM alpine:3.22
WORKDIR /app
COPY --from=build /out/notification-service-go /app/notification-service-go
EXPOSE 8090
CMD ["/app/notification-service-go"]

