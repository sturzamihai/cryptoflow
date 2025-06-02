FROM ubuntu:22.04

# Avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    libssl-dev \
    openmpi-bin \
    openmpi-doc \
    libopenmpi-dev \
    libomp-dev \
    && rm -rf /var/lib/apt/lists/*

# Create working directory
WORKDIR /app

# Copy source files
COPY . .

# Compile the application
RUN mpic++ -O3 -fopenmp \
    -o crypto_processor \
    src/main.cpp src/crypto_engine.cpp src/image_processor.cpp \
    -lssl -lcrypto -fopenmp -std=c++17

# Create directories for input/output
RUN mkdir -p /app/data

# Set the container to run as a service (keep alive)
CMD ["tail", "-f", "/dev/null"]
