#include "crypto_engine.h"
#include <openssl/evp.h>
#include <openssl/aes.h>
#include <openssl/rand.h>
#include <omp.h>
#include <iostream>
#include <cstring>

std::vector<unsigned char> CryptoEngine::processData(
    const std::vector<unsigned char>& input,
    const std::string& key,
    CryptoMode mode,
    Operation operation,
    const std::vector<unsigned char>& iv
) {
    // Prepare key (ensure it's 32 bytes for AES-256)
    std::vector<unsigned char> keyBytes(32, 0);
    size_t keyLen = std::min(key.length(), static_cast<size_t>(32));
    std::memcpy(keyBytes.data(), key.c_str(), keyLen);

    // Prepare IV for CBC mode
    std::vector<unsigned char> ivBytes;
    if (mode == CryptoMode::AES_CBC) {
        if (operation == Operation::ENCRYPT) {
            ivBytes = generateIV();
        } else {
            ivBytes = iv;
        }
    }

    // Choose cipher based on mode
    const EVP_CIPHER* cipher = (mode == CryptoMode::AES_ECB) ? 
        EVP_aes_256_ecb() : EVP_aes_256_cbc();

    // Process data in parallel blocks
    std::vector<unsigned char> processedData;
    const size_t blockSize = AES_BLOCK_SIZE;
    
    if (operation == Operation::ENCRYPT) {
        // Pad data for encryption
        std::vector<unsigned char> paddedInput = padData(input);
        
        // For CBC mode, prepend IV to output
        if (mode == CryptoMode::AES_CBC) {
            processedData.insert(processedData.end(), ivBytes.begin(), ivBytes.end());
        }
        
        processedData.resize(processedData.size() + paddedInput.size());
        
        // Process blocks in parallel
        const size_t numBlocks = paddedInput.size() / blockSize;
        
        #pragma omp parallel for
        for (size_t i = 0; i < numBlocks; i++) {
            EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
            
            if (mode == CryptoMode::AES_ECB) {
                EVP_EncryptInit_ex(ctx, cipher, nullptr, keyBytes.data(), nullptr);
            } else {
                // For CBC, each block needs proper chaining
                EVP_EncryptInit_ex(ctx, cipher, nullptr, keyBytes.data(), ivBytes.data());
            }
            
            EVP_CIPHER_CTX_set_padding(ctx, 0); // We handle padding manually
            
            int outLen;
            size_t inputOffset = i * blockSize;
            size_t outputOffset = (mode == CryptoMode::AES_CBC ? 16 : 0) + i * blockSize;
            
            processBlock(ctx, 
                &paddedInput[inputOffset], 
                &processedData[outputOffset], 
                blockSize, 
                outLen);
            
            EVP_CIPHER_CTX_free(ctx);
        }
    } else {
        // Decryption
        std::vector<unsigned char> inputToDecrypt = input;
        size_t dataOffset = 0;
        
        // For CBC mode, extract IV from beginning
        if (mode == CryptoMode::AES_CBC) {
            ivBytes.assign(input.begin(), input.begin() + 16);
            inputToDecrypt.assign(input.begin() + 16, input.end());
            dataOffset = 16;
        }
        
        processedData.resize(inputToDecrypt.size());
        
        // Process blocks in parallel
        const size_t numBlocks = inputToDecrypt.size() / blockSize;
        
        #pragma omp parallel for
        for (size_t i = 0; i < numBlocks; i++) {
            EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
            
            if (mode == CryptoMode::AES_ECB) {
                EVP_DecryptInit_ex(ctx, cipher, nullptr, keyBytes.data(), nullptr);
            } else {
                EVP_DecryptInit_ex(ctx, cipher, nullptr, keyBytes.data(), ivBytes.data());
            }
            
            EVP_CIPHER_CTX_set_padding(ctx, 0);
            
            int outLen;
            size_t inputOffset = i * blockSize;
            size_t outputOffset = i * blockSize;
            
            processBlock(ctx, 
                &inputToDecrypt[inputOffset], 
                &processedData[outputOffset], 
                blockSize, 
                outLen);
            
            EVP_CIPHER_CTX_free(ctx);
        }
        
        // Remove padding
        processedData = removePadding(processedData);
    }

    return processedData;
}

std::vector<unsigned char> CryptoEngine::padData(const std::vector<unsigned char>& data) {
    size_t blockSize = AES_BLOCK_SIZE;
    size_t paddingNeeded = blockSize - (data.size() % blockSize);
    
    std::vector<unsigned char> padded = data;
    for (size_t i = 0; i < paddingNeeded; i++) {
        padded.push_back(static_cast<unsigned char>(paddingNeeded));
    }
    
    return padded;
}

std::vector<unsigned char> CryptoEngine::removePadding(const std::vector<unsigned char>& data) {
    if (data.empty()) return data;
    
    unsigned char paddingValue = data.back();
    if (paddingValue > AES_BLOCK_SIZE || paddingValue == 0) {
        return data; // Invalid padding
    }
    
    // Verify padding
    size_t dataSize = data.size();
    for (size_t i = dataSize - paddingValue; i < dataSize; i++) {
        if (data[i] != paddingValue) {
            return data; // Invalid padding
        }
    }
    
    return std::vector<unsigned char>(data.begin(), data.end() - paddingValue);
}

std::vector<unsigned char> CryptoEngine::generateIV() {
    std::vector<unsigned char> iv(AES_BLOCK_SIZE);
    if (RAND_bytes(iv.data(), AES_BLOCK_SIZE) != 1) {
        throw std::runtime_error("Failed to generate random IV");
    }
    return iv;
}

void CryptoEngine::processBlock(
    EVP_CIPHER_CTX* ctx,
    const unsigned char* input,
    unsigned char* output,
    int inputLen,
    int& outputLen
) {
    int len;
    if (EVP_CipherUpdate(ctx, output, &len, input, inputLen) != 1) {
        throw std::runtime_error("Cipher update failed");
    }
    outputLen = len;
    
    if (EVP_CipherFinal_ex(ctx, output + len, &len) != 1) {
        throw std::runtime_error("Cipher final failed");
    }
    outputLen += len;
}
