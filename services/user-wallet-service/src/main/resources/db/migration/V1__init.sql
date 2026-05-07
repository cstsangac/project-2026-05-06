CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE app_user (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE wallet (
  user_id UUID PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
  balance_amount NUMERIC(19,2) NOT NULL CHECK (balance_amount >= 0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE wallet_tx (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  tx_type TEXT NOT NULL CHECK (tx_type IN ('TOPUP', 'GIFT_DEBIT')),
  amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  balance_after_amount NUMERIC(19,2) NOT NULL CHECK (balance_after_amount >= 0),
  reference_id TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE gift_event (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  stream_id UUID NOT NULL,
  sender_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  gift_type TEXT NOT NULL,
  gift_price NUMERIC(19,2) NOT NULL CHECK (gift_price > 0),
  client_request_id TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX gift_event_sender_stream_req_uniq
  ON gift_event(sender_user_id, stream_id, client_request_id)
  WHERE client_request_id IS NOT NULL;

