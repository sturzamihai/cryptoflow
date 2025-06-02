#include <iostream>
#include <string>
#include <mpi.h>
#include <omp.h>
#include "crypto_engine.h"
#include "image_processor.h"

void printUsage(const char* programName) {
    std::cout << "Usage: " << programName << " <operation> <mode> <key> <input_file> <output_file>" << std::endl;
    std::cout << "  operation: encrypt or decrypt" << std::endl;
    std::cout << "  mode: ECB or CBC" << std::endl;
    std::cout << "  key: encryption key (16-32 characters)" << std::endl;
    std::cout << "  input_file: path to input BMP file" << std::endl;
    std::cout << "  output_file: path to output BMP file" << std::endl;
    std::cout << std::endl;
    std::cout << "Example: " << programName << " encrypt CBC mySecretKey123456 input.bmp output.bmp" << std::endl;
}

int main(int argc, char* argv[]) {
    // Initialize MPI
    MPI_Init(&argc, &argv);
    
    int mpiRank;
    MPI_Comm_rank(MPI_COMM_WORLD, &mpiRank);
    
    // Only rank 0 processes arguments and prints messages
    if (mpiRank == 0) {
        if (argc != 6) {
            printUsage(argv[0]);
            MPI_Finalize();
            return 1;
        }
    }

    // Parse arguments
    std::string operationStr = argv[1];
    std::string modeStr = argv[2];
    std::string key = argv[3];
    std::string inputFile = argv[4];
    std::string outputFile = argv[5];

    // Validate arguments
    Operation operation;
    if (operationStr == "encrypt") {
        operation = Operation::ENCRYPT;
    } else if (operationStr == "decrypt") {
        operation = Operation::DECRYPT;
    } else {
        if (mpiRank == 0) {
            std::cerr << "Error: Invalid operation. Use 'encrypt' or 'decrypt'" << std::endl;
        }
        MPI_Finalize();
        return 1;
    }

    CryptoMode mode;
    if (modeStr == "ECB") {
        mode = CryptoMode::AES_ECB;
    } else if (modeStr == "CBC") {
        mode = CryptoMode::AES_CBC;
    } else {
        if (mpiRank == 0) {
            std::cerr << "Error: Invalid mode. Use 'ECB' or 'CBC'" << std::endl;
        }
        MPI_Finalize();
        return 1;
    }

    if (key.length() < 16 || key.length() > 32) {
        if (mpiRank == 0) {
            std::cerr << "Error: Key must be between 16 and 32 characters" << std::endl;
        }
        MPI_Finalize();
        return 1;
    }

    if (mpiRank == 0) {
        std::cout << "Crypto Processor starting..." << std::endl;
        std::cout << "Operation: " << operationStr << std::endl;
        std::cout << "Mode: AES-256-" << modeStr << std::endl;
        std::cout << "Input: " << inputFile << std::endl;
        std::cout << "Output: " << outputFile << std::endl;
        std::cout << "OpenMP threads available: " << omp_get_max_threads() << std::endl;
        
        int mpiSize;
        MPI_Comm_size(MPI_COMM_WORLD, &mpiSize);
        std::cout << "MPI processes: " << mpiSize << std::endl;
    }

    // Process the image
    bool success = ImageProcessor::processImage(inputFile, outputFile, key, mode, operation);

    if (mpiRank == 0) {
        if (success) {
            std::cout << "Processing completed successfully!" << std::endl;
        } else {
            std::cerr << "Processing failed!" << std::endl;
        }
    }

    MPI_Finalize();
    return success ? 0 : 1;
}
