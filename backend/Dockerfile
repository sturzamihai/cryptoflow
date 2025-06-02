# Use Ubuntu 22.04 as base image
FROM ubuntu:22.04

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
ENV PATH="$JAVA_HOME/bin:$PATH"

# Update package lists and install dependencies
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
    curl \
    wget \
    build-essential \
    cmake \
    pkg-config \
    libssl-dev \
    libopenmpi-dev \
    libomp-dev \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN adduser --disabled-password --gecos '' spring-user

# Create app directory
RUN mkdir -p /app
WORKDIR /app

# Copy Maven wrapper and configuration
COPY backend/mvnw ./
COPY backend/.mvn ./.mvn
COPY backend/pom.xml ./

# Copy Java source code
COPY backend/src ./src

# Copy crypto processor source code
COPY crypto /app/crypto

# Build Java application
RUN mvn install -DskipTests

# Build crypto processor
WORKDIR /app/crypto
RUN cmake . && make

# Copy the built crypto processor executable to app directory
RUN cp crypto_processor /app/

# Switch back to app directory
WORKDIR /app

# Set proper permissions
RUN chown -R spring-user:spring-user /app
RUN chmod +x /app/crypto_processor

# Create volume for temporary files
VOLUME /tmp

# Switch to non-root user
USER spring-user

# Start the Java application
ENTRYPOINT ["java", "-jar", "target/cryptoflow-0.0.1-SNAPSHOT.jar"]