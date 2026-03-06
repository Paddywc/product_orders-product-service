package product.orders.productservice.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import product.orders.productservice.config.KafkaTopicsProperties;
import product.orders.productservice.messaging.event.ProductCreatedEvent;

import java.util.UUID;

@Component
public class ProductEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaTopicsProperties topics;


    public ProductEventProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    private String getTopicName() {
        return topics.getProductEvents();
    }

    private <T> Message<T> buildMessage(T event, UUID productId, String eventType) {
        return MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, getTopicName())
                .setHeader(KafkaHeaders.KEY, productId.toString())
                .setHeader("eventType", eventType)
                .build();
    }

    public void publish(ProductCreatedEvent event){
        kafkaTemplate.send(buildMessage(event, event.productId(), "ProductCreatedEvent"));
    }
}
