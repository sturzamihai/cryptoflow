# Crypto Service

A high-performance, parallelized image encryption/decryption service using OpenMPI and OpenMP with AES-256 ECB/CBC modes.

## Features

- **Parallel Processing**: Uses both OpenMPI (distributed) and OpenMP (shared memory) for maximum performance
- **AES Encryption**: Supports AES-256 in ECB and CBC modes
- **BMP Image Support**: Processes BMP images while preserving headers
- **Command Line Interface**: Simple CLI for easy integration with other services
- **Docker Ready**: Containerized for easy deployment and scaling

## Usage

### Command Line

```bash
./crypto_processor <operation> <mode> <key> <input_file> <output_file>
```

### Parameters

- `operation`: `encrypt` or `decrypt`
- `mode`: `ECB` or `CBC`
- `key`: Encryption key (16-32 characters)
- `input_file`: Path to input BMP file
- `output_file`: Path to output BMP file

### Examples

```bash
# Encrypt with AES-CBC
./crypto_processor encrypt CBC mySecretKey123456 input.bmp encrypted.bmp

# Decrypt with AES-CBC
./crypto_processor decrypt CBC mySecretKey123456 encrypted.bmp decrypted.bmp

# Encrypt with AES-ECB
./crypto_processor encrypt ECB mySecretKey123456 input.bmp encrypted.bmp
```

## Docker Usage

### Build the image

```bash
docker build -t crypto-service .
```

### Run with MPI (multiple processes)

```bash
docker run --rm \
  -v $(pwd)/data:/app/data \
  crypto-service \
  mpirun -np 4 ./crypto_processor encrypt CBC mySecretKey123456 /app/data/image.bmp /app/data/encrypted.bmp
```

## Performance

- **OpenMP**: Utilizes all available CPU cores for parallel block processing
- **MPI**: Can distribute work across multiple container instances
- **Optimized**: Compiled with -O3 optimization flags
- **Memory Efficient**: Processes data in chunks to handle large images

## Security

- Uses OpenSSL's EVP interface for cryptographic operations
- Supports AES-256 encryption (256-bit keys)
- Proper PKCS#7 padding for block alignment
- Secure random IV generation for CBC mode

## File Format Support

Currently supports BMP (Bitmap) images:

- Preserves BMP headers during encryption/decryption
- Works with any BMP bit depth
- Maintains image metadata and structure
