FROM golang:1.21-alpine AS builder

# Instalar dependências necessárias
RUN apk add --no-cache git

# Definir diretório de trabalho
WORKDIR /app

# Copiar módulos Go
COPY go.mod go.sum ./

# Baixar dependências
RUN go mod download

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o main ./src

# Estágio final - imagem mínima
FROM alpine:latest

# Instalar ca-certificates para HTTPS
RUN apk --no-cache add ca-certificates

# Criar usuário não-root
RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -s /bin/sh -D appuser

WORKDIR /root/

# Copiar binário compilado
COPY --from=builder /app/main .

# Mudar proprietário do arquivo
RUN chown appuser:appgroup main

# Mudar para usuário não-root
USER appuser

# Comando para executar a aplicação
CMD ["./main"]
