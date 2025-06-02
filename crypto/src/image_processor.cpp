#include "image_processor.h"
#include <fstream>
#include <iostream>
#include <cstring>
#include <mpi.h>
#include <omp.h>

bool ImageProcessor::processImage(
    const std::string& inputPath,
    const std::string& outputPath,
    const std::string& key,
    CryptoMode mode,
    Operation operation
) {
    try {
        // Read input file
        std::vector<unsigned char> fileData = readFile(inputPath);
        if (fileData.empty()) {
            std::cerr << "Failed to read input file: " << inputPath << std::endl;
            return false;
        }

        // Parse BMP header
        if (fileData.size() < sizeof(BMPHeader)) {
            std::cerr << "File too small to be a valid BMP" << std::endl;
            return false;
        }

        BMPHeader header = parseBMPHeader(fileData);
        
        // Verify BMP signature
        if (header.signature != 0x4D42) { // "BM" in little endian
            std::cerr << "Not a valid BMP file" << std::endl;
            return false;
        }

        std::cout << "Processing BMP image: " << header.width << "x" << header.height 
                  << " (" << header.bitsPerPixel << " bits per pixel)" << std::endl;

        // Extract pixel data (skip header)
        std::vector<unsigned char> pixelData(
            fileData.begin() + header.dataOffset,
            fileData.end()
        );

        std::cout << "Pixel data size: " << pixelData.size() << " bytes" << std::endl;

        // Get MPI info
        int mpiRank, mpiSize;
        MPI_Comm_rank(MPI_COMM_WORLD, &mpiRank);
        MPI_Comm_size(MPI_COMM_WORLD, &mpiSize);

        std::vector<unsigned char> processedPixels;

        if (mpiSize > 1) {
            // Distributed processing with MPI
            std::cout << "Using MPI with " << mpiSize << " processes (rank " << mpiRank << ")" << std::endl;
            
            // Calculate chunk size for this process
            size_t totalSize = pixelData.size();
            size_t chunkSize = totalSize / mpiSize;
            size_t startOffset = mpiRank * chunkSize;
            size_t endOffset = (mpiRank == mpiSize - 1) ? totalSize : startOffset + chunkSize;
            
            // Ensure chunk alignment to AES block boundaries (16 bytes)
            if (startOffset % 16 != 0) {
                startOffset = (startOffset / 16) * 16;
            }
            if (endOffset % 16 != 0 && mpiRank != mpiSize - 1) {
                endOffset = ((endOffset / 16) + 1) * 16;
            }
            
            std::vector<unsigned char> localChunk(
                pixelData.begin() + startOffset,
                pixelData.begin() + std::min(endOffset, totalSize)
            );
            
            std::cout << "Process " << mpiRank << " processing chunk: " 
                      << startOffset << " to " << std::min(endOffset, totalSize) 
                      << " (" << localChunk.size() << " bytes)" << std::endl;

            // Process local chunk
            std::vector<unsigned char> processedChunk = CryptoEngine::processData(
                localChunk, key, mode, operation
            );

            // Gather results
            std::vector<int> recvCounts(mpiSize);
            std::vector<int> displs(mpiSize);
            
            int localSize = static_cast<int>(processedChunk.size());
            MPI_Allgather(&localSize, 1, MPI_INT, recvCounts.data(), 1, MPI_INT, MPI_COMM_WORLD);
            
            int totalProcessedSize = 0;
            for (int i = 0; i < mpiSize; i++) {
                displs[i] = totalProcessedSize;
                totalProcessedSize += recvCounts[i];
            }
            
            processedPixels.resize(totalProcessedSize);
            MPI_Allgatherv(
                processedChunk.data(), localSize, MPI_UNSIGNED_CHAR,
                processedPixels.data(), recvCounts.data(), displs.data(), 
                MPI_UNSIGNED_CHAR, MPI_COMM_WORLD
            );
        } else {
            // Single process with OpenMP parallelization
            std::cout << "Using OpenMP with " << omp_get_max_threads() << " threads" << std::endl;
            processedPixels = CryptoEngine::processData(pixelData, key, mode, operation);
        }

        // Only rank 0 writes the output file
        if (mpiRank == 0) {
            // Reconstruct the full file
            std::vector<unsigned char> outputData;
            
            // Copy header
            outputData.insert(outputData.end(), 
                fileData.begin(), 
                fileData.begin() + header.dataOffset);
            
            // Add processed pixel data
            outputData.insert(outputData.end(), 
                processedPixels.begin(), 
                processedPixels.end());

            // Write output file
            if (!writeFile(outputPath, outputData)) {
                std::cerr << "Failed to write output file: " << outputPath << std::endl;
                return false;
            }

            std::cout << "Successfully processed image. Output written to: " << outputPath << std::endl;
        }

        return true;
    } catch (const std::exception& e) {
        std::cerr << "Error processing image: " << e.what() << std::endl;
        return false;
    }
}

std::vector<unsigned char> ImageProcessor::readFile(const std::string& path) {
    std::ifstream file(path, std::ios::binary);
    if (!file) {
        return {};
    }

    file.seekg(0, std::ios::end);
    size_t size = file.tellg();
    file.seekg(0, std::ios::beg);

    std::vector<unsigned char> data(size);
    file.read(reinterpret_cast<char*>(data.data()), size);
    
    return data;
}

bool ImageProcessor::writeFile(const std::string& path, const std::vector<unsigned char>& data) {
    std::ofstream file(path, std::ios::binary);
    if (!file) {
        return false;
    }

    file.write(reinterpret_cast<const char*>(data.data()), data.size());
    return file.good();
}

BMPHeader ImageProcessor::parseBMPHeader(const std::vector<unsigned char>& data) {
    BMPHeader header;
    std::memcpy(&header, data.data(), sizeof(BMPHeader));
    return header;
}

void ImageProcessor::writeBMPHeader(std::vector<unsigned char>& data, const BMPHeader& header) {
    std::memcpy(data.data(), &header, sizeof(BMPHeader));
}
