FROM python:3.11-slim

WORKDIR /app

# Instalar dependências do sistema
RUN apt-get update && apt-get install -y \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copiar requirements e instalar dependências Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar código da aplicação
COPY proxy.py .

# Tornar o script executável
RUN chmod +x proxy.py

# Expor portas
EXPOSE 5557 5558

# Comando para executar o proxy
CMD ["python", "proxy.py"]
