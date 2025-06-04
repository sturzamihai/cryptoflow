package ro.mihaisturza.cryptoflow.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.mihaisturza.cryptoflow.amqp.RabbitMQConfig;
import ro.mihaisturza.cryptoflow.crypto.CryptoService;
import ro.mihaisturza.cryptoflow.image.ImageProcessingMessage;

@Component
@Profile("consumer")
public class ImageMessageConsumer {
        private static final Logger logger = LoggerFactory.getLogger(ImageMessageConsumer.class);

        @Autowired
        private CryptoService cryptoService;

        @Autowired
        private ProcessedImageRepository processedImageRepository;

        @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
        public void processImageMessage(ImageProcessingMessage message) {
                logger.info("Received image processing message for image: {} (ID: {})",
                                message.getImageName(), message.getId());

                try {
                        String originalExtension = "";
                        String originalName = message.getImageName();
                        if (originalName != null && originalName.contains(".")) {
                                originalExtension = originalName.substring(originalName.lastIndexOf("."));
                        }
                        String uuidFileName = message.getId().toString() + originalExtension;

                        // Process the image using the crypto service
                        byte[] processedImage = cryptoService.processImage(
                                        message.getImageData(),
                                        message.getSymmetricKey(),
                                        message.getOperation(),
                                        message.getEncryptionMode(),
                                        uuidFileName);

                        logger.info("Successfully processed image: {} (ID: {}). Result size: {} bytes",
                                        message.getImageName(), message.getId(), processedImage.length);

                        ProcessedImage processedImageEntity = new ProcessedImage(
                                        message.getId().toString(),
                                        message.getImageName(),
                                        processedImage,
                                        message.getOperation().name(),
                                        message.getEncryptionMode().name());

                        ProcessedImage savedImage = processedImageRepository.save(processedImageEntity);
                        logger.info("Saved processed image to database with ID: {}", savedImage.getId());
                } catch (Exception e) {
                        logger.error("Failed to process image: {} (ID: {})",
                                        message.getImageName(), message.getId(), e);
                }
        }
}
