package ro.mihaisturza.cryptoflow.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.mihaisturza.cryptoflow.image.ImageProcessingMessage;

@Service
public class MessagePublisherService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishMessage(ImageProcessingMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
    }
}
