package ro.mihaisturza.cryptoflow.image;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ro.mihaisturza.cryptoflow.amqp.MessagePublisherService;

@Service
public class ImageService {
    private static final int BMP_HEADER_SIZE = 54;
    private static final byte[] BMP_SIGNATURE = { 0x42, 0x4D }; // "BM" in ASCII

    @Autowired
    private MessagePublisherService messagePublisherService;

    public UUID processImage(MultipartFile file, String key, Operation operation, EncryptionMode mode) {
        try {
            byte[] imageData = file.getBytes();
            String imageName = file.getOriginalFilename();
            UUID id = UUID.randomUUID();

            ImageProcessingMessage message = new ImageProcessingMessage(id, imageData, key, operation, mode, imageName);
            messagePublisherService.publishMessage(message);

            return id;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process image", e);
        }
    }
}
