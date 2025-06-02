#ifndef CRYPTO_ENGINE_H
#define CRYPTO_ENGINE_H

#include <vector>
#include <string>
#include <openssl/aes.h>
#include <openssl/evp.h>
#include <openssl/rand.h>

enum class CryptoMode {
    AES_ECB,
    AES_CBC
};

enum class Operation {
    ENCRYPT,
    DECRYPT
};

class CryptoEngine {
public:
    static std::vector<unsigned char> processData(
        const std::vector<unsigned char>& input,
        const std::string& key,
        CryptoMode mode,
        Operation operation,
        const std::vector<unsigned char>& iv = {}
    );

private:
    static std::vector<unsigned char> padData(const std::vector<unsigned char>& data);
    static std::vector<unsigned char> removePadding(const std::vector<unsigned char>& data);
    static std::vector<unsigned char> generateIV();
    static void processBlock(
        EVP_CIPHER_CTX* ctx,
        const unsigned char* input,
        unsigned char* output,
        int inputLen,
        int& outputLen
    );
};

#endif // CRYPTO_ENGINE_H
