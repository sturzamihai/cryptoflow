cmake_minimum_required(VERSION 3.16)
project(CryptoProcessor)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fopenmp")

# Find required packages
find_package(OpenSSL REQUIRED)
find_package(MPI REQUIRED)
find_package(OpenMP REQUIRED)

# Include directories
include_directories(${MPI_INCLUDE_PATH})
include_directories(src)

# Source files
set(SOURCES
    src/main.cpp
    src/crypto_engine.cpp
    src/image_processor.cpp
)

# Create executable
add_executable(crypto_processor ${SOURCES})

# Link libraries
target_link_libraries(crypto_processor 
    OpenSSL::SSL 
    OpenSSL::Crypto 
    ${MPI_LIBRARIES}
    OpenMP::OpenMP_CXX
)

if(MPI_COMPILE_FLAGS)
    set_target_properties(crypto_processor PROPERTIES
        COMPILE_FLAGS "${MPI_COMPILE_FLAGS}")
endif()

if(MPI_LINK_FLAGS)
    set_target_properties(crypto_processor PROPERTIES
        LINK_FLAGS "${MPI_LINK_FLAGS}")
endif()
