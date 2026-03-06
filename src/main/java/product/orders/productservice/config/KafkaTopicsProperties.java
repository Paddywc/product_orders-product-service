package product.orders.productservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topic")
public class KafkaTopicsProperties {

    private String productEvents;

    public String getProductEvents() {
        return productEvents;
    }

    public void setProductEvents(String productEvents) {
        this.productEvents = productEvents;
    }
}
