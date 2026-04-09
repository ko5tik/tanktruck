# Build stage
FROM golang:1.24-alpine AS builder

WORKDIR /app

# Install dependencies
RUN apk add --no-cache gcc musl-dev

# Copy go.mod and go.sum
COPY go.mod go.sum ./
RUN go mod download

# Copy the source code
COPY ./ ./

# Build the bot
RUN go build -o xusdbot ./bot/

# Final stage
FROM alpine:latest

RUN apk add --no-cache ca-certificates

WORKDIR /root/

# Copy the binary from the builder
COPY --from=builder /app/xusdbot .
# Copy the default config
COPY ./cfg/ ./cfg/

# Run the bot
ENTRYPOINT ["./xusdbot"]

