package ro.mihaisturza.cryptoflow.image;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ImageProcessingMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private byte[] imageData;
    private String symmetricKey;
    private Operation operation;
    private EncryptionMode encryptionMode;
    private String imageName;
}
