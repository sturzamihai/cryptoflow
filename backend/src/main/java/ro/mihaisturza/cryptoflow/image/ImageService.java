package ro.mihaisturza.cryptoflow.image;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ro.mihaisturza.cryptoflow.amqp.MessagePublisherService;
import ro.mihaisturza.cryptoflow.consumer.ProcessedImage;
import ro.mihaisturza.cryptoflow.consumer.ProcessedImageRepository;

@Service
@Profile("backend")
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    @Autowired
    private MessagePublisherService messagePublisherService;

    @Autowired
    private ProcessedImageRepository processedImageRepository;

    public UUID processImage(MultipartFile file, String key, Operation operation, EncryptionMode mode) {
        try {
            byte[] imageData = file.getBytes();
            String imageName = file.getOriginalFilename();
            UUID id = UUID.randomUUID();

            ImageProcessingMessage message = new ImageProcessingMessage(id, imageData, key, operation, mode, imageName);
            messagePublisherService.publishMessage(message);

            logger.info("Published image processing message for image: {} (ID: {})", imageName, id);

            return id;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to process image: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to process image", e);
        }
    }

    public List<ProcessedImage> getProcessedImages() {
        return processedImageRepository.findAll();
    }
}
