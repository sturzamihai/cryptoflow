package ro.mihaisturza.cryptoflow.consumer;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_images")
@NoArgsConstructor
@Getter
@Setter
public class ProcessedImage {
    @Id
    private String id;

    @Column(name = "image_name", nullable = false)
    private String imageName;

    @Column(name = "image_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "encryption_mode", nullable = false)
    private String encryptionMode;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedImage(String id, String imageName, byte[] imageData, String operation, String encryptionMode) {
        this.id = id;
        this.imageName = imageName;
        this.imageData = imageData;
        this.operation = operation;
        this.encryptionMode = encryptionMode;
        this.processedAt = LocalDateTime.now();
    }
}
