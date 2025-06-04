package ro.mihaisturza.cryptoflow.crypto;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.mihaisturza.cryptoflow.image.EncryptionMode;
import ro.mihaisturza.cryptoflow.image.Operation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@Profile("consumer")
public class CryptoService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
    
    private static final String CRYPTO_PROCESSOR_PATH = "/app/crypto_processor"; // Path to compiled C++ executable
    private static final String TEMP_DATA_PATH = "/tmp/crypto"; // Temporary directory for processing
    private static final int PROCESS_TIMEOUT_SECONDS = 60; // Timeout for crypto process execution
    
    public byte[] processImage(byte[] imageData, String key, Operation operation, EncryptionMode mode, String fileName) {
        try {
            // Create temp directory if it doesn't exist
            Files.createDirectories(Paths.get(TEMP_DATA_PATH));
            
            // Generate unique file names
            String timestamp = String.valueOf(System.currentTimeMillis());;
            String inputFileName = timestamp + "_input_" + fileName;
            String outputFileName = timestamp + "_output_" + fileName;
            
            Path inputFile = Paths.get(TEMP_DATA_PATH, inputFileName);
            Path outputFile = Paths.get(TEMP_DATA_PATH, outputFileName);
            
            // Write input data to file
            Files.write(inputFile, imageData);
            logger.info("Written input file: {}", inputFile);
            
            // Prepare command arguments
            String operationStr = operation.name().toLowerCase();
            String modeStr = extractModeFromEnum(mode);
            
            String[] command = {
                CRYPTO_PROCESSOR_PATH,
                operationStr,
                modeStr,
                key,
                inputFile.toString(),
                outputFile.toString()
            };
            
            logger.info("Executing crypto command: {}", String.join(" ", command));
            
            // Execute crypto processor directly
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    logger.info("Crypto output: {}", line);
                }
            }
            
            // Wait for completion
            boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Crypto process timed out");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("Crypto process failed with exit code " + exitCode + ": " + output.toString());
            }
            
            // Read result
            if (!Files.exists(outputFile)) {
                throw new RuntimeException("Output file was not created: " + outputFile);
            }
            
            byte[] result = Files.readAllBytes(outputFile);
            logger.info("Successfully processed image. Output size: {} bytes", result.length);
            
            // Cleanup
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing image with crypto service", e);
            throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
        }
    }
    
    private String extractModeFromEnum(EncryptionMode mode) {
        // Convert AES_ECB -> ECB, AES_CBC -> CBC
        String modeStr = mode.name();
        if (modeStr.startsWith("AES_")) {
            return modeStr.substring(4); // Remove "AES_" prefix
        }
        return modeStr;
    }
}
