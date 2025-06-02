#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#include <string>
#include <vector>
#include "crypto_engine.h"

struct BMPHeader {
    uint16_t signature;
    uint32_t fileSize;
    uint16_t reserved1;
    uint16_t reserved2;
    uint32_t dataOffset;
    uint32_t headerSize;
    int32_t width;
    int32_t height;
    uint16_t planes;
    uint16_t bitsPerPixel;
    uint32_t compression;
    uint32_t imageSize;
    int32_t xResolution;
    int32_t yResolution;
    uint32_t colorsUsed;
    uint32_t importantColors;
} __attribute__((packed));

class ImageProcessor {
public:
    static bool processImage(
        const std::string& inputPath,
        const std::string& outputPath,
        const std::string& key,
        CryptoMode mode,
        Operation operation
    );

private:
    static std::vector<unsigned char> readFile(const std::string& path);
    static bool writeFile(const std::string& path, const std::vector<unsigned char>& data);
    static BMPHeader parseBMPHeader(const std::vector<unsigned char>& data);
    static void writeBMPHeader(std::vector<unsigned char>& data, const BMPHeader& header);
};

#endif // IMAGE_PROCESSOR_H
