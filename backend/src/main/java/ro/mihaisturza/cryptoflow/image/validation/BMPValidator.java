package ro.mihaisturza.cryptoflow.image.validation;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BMPValidator implements ConstraintValidator<ValidBMPFile, MultipartFile> {
    private static final byte[] BMP_SIGNATURE = { 0x42, 0x4D }; // "BM"
    private static final int BMP_HEADER_SIZE = 54;

    private long maxSize;

    @Override
    public void initialize(ValidBMPFile constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            addViolation(context, "File cannot be null or empty");
            return false;
        }

        // Check file size
        if (file.getSize() > maxSize) {
            addViolation(context, String.format("File size (%d bytes) exceeds maximum allowed size of %d bytes",
                    file.getSize(), maxSize));
            return false;
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bmp")) {
            addViolation(context, "File must have .bmp extension");
            return false;
        }

        // Check BMP signature
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[BMP_HEADER_SIZE];
            int bytesRead = inputStream.read(header);

            if (bytesRead < BMP_HEADER_SIZE) {
                addViolation(context, "File is too small to be a valid BMP (minimum 54 bytes required)");
                return false;
            }

            if (header[0] != BMP_SIGNATURE[0] || header[1] != BMP_SIGNATURE[1]) {
                addViolation(context, String.format("Invalid BMP signature. Expected 'BM', got '%c%c'",
                        (char) header[0], (char) header[1]));
                return false;
            }

            return true;

        } catch (IOException e) {
            addViolation(context, "Failed to read file for validation: " + e.getMessage());
            return false;
        }
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
