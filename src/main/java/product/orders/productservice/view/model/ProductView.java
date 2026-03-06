package product.orders.productservice.view.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.UUID;

/**
 * A A Product View is a read-optimized representation of a product, stored in Redis, built
 * from Product state, and safe to throw away at any time.
 */
@RedisHash(value = "product-view", timeToLive = 900)
public class ProductView implements Serializable {

    @Id
    UUID productId;

    @Indexed
    String name;

    String description;
    Long priceUSDCents;

    @Indexed
    String category;

    @Indexed
    String status;

    public ProductView() {
    }

    public ProductView(UUID productId, String name, String description, Long priceUSDCents, String category, String status) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.priceUSDCents = priceUSDCents;
        this.category = category;
        this.status = status;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getPriceUSDCents() {
        return priceUSDCents;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }
}



