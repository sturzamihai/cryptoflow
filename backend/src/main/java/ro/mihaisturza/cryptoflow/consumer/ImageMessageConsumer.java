package ro.mihaisturza.cryptoflow.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ro.mihaisturza.cryptoflow.amqp.RabbitMQConfig;
import ro.mihaisturza.cryptoflow.image.ImageProcessingMessage;

@Component
@Profile("consumer")
public class ImageMessageConsumer {
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processImageMessage(ImageProcessingMessage message) {
        System.out.println("Received image processing message: " + message);
        
    }
}
