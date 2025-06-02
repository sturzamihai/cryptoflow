package ro.mihaisturza.cryptoflow.amqp;

import java.util.List;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "cryptoflow.processing";

    @Bean
    public SerializerMessageConverter messageConverter() {
        SerializerMessageConverter converter = new SerializerMessageConverter();

        converter.setAllowedListPatterns(
                List.of("ro.mihaisturza.cryptoflow.*",
                "java.lang.*",
                "java.util.*",
                "java.time.*")
        );

        return converter;
    }

    @Bean
    public Queue imageProcessingQueue() {
        return QueueBuilder.durable(QUEUE_NAME).withArgument("x-max-length-bytes", 500_000_000)
                .withArgument("x-message-ttl", 360000).build();
    }
}
