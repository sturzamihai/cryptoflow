package ro.mihaisturza.cryptoflow.image;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.mihaisturza.cryptoflow.image.validation.ValidBMPFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ImageProcessingRequest {
    @NotNull(message = "File is required")
    @ValidBMPFile(maxSize = 100 * 1024 * 1024) // 100MB default
    private MultipartFile file;

    @NotBlank(message = "Encryption key is required")
    @Size(min = 16, max = 32, message = "Encryption key must be between 16 and 32 characters")
    private String key;

    @NotBlank(message = "Mode is required")
    @Pattern(regexp = "^(ECB|CBC)$", message = "Mode must be either ECB or CBC")
    private String mode;

    public EncryptionMode getEncryptionMode() {
        return EncryptionMode.valueOf("AES_" + mode.toUpperCase());
    }
}
