package product.orders.productservice.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Product {


    @Id
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 255)
    @NotNull
    @Size(max = 255)
    private String name;

    @Column(name = "product_description", length = 2000)
    private String description;


    @PositiveOrZero
    @NotNull
    @Column(name = "price_usc_cents", nullable = false)
    private long priceUsdCents;

    @Column(name = "product_status", nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "product_category", nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    protected Product() {
    }

    private Product(UUID productId, String name, String description, long priceUsdCents, ProductStatus status, ProductCategory category) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.priceUsdCents = priceUsdCents;
        this.status = status;
        this.category = category;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // ----------------------------------------------------
    // Factory
    // ----------------------------------------------------
    public static Product create(String name, String description, long priceUsdCents, ProductStatus status, ProductCategory category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be null/blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name must be <= 255 characters");
        }
        if (priceUsdCents < 0) {
            throw new IllegalArgumentException("Product price must be >= 0");
        }
        if (status == null) {
            throw new IllegalArgumentException("Product status must not be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Product category must not be null");
        }
        return new Product(UUID.randomUUID(), name, description, priceUsdCents, status, category);
    }

    // ----------------------------------------------------
    // Domain behavior
    // ----------------------------------------------------

    public void chancePrice(long priceUsdCents) {
        this.priceUsdCents = priceUsdCents;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (status == ProductStatus.ACTIVE) {
            return; // idempotent
        }
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();

    }

    public void deactivate() {
        if (status == ProductStatus.INACTIVE) {
            return; // idempotent
        }
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();

    }

    // ----------------------------------------------------
    // Getters
    // ----------------------------------------------------

    public UUID getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getPriceUsdCents() {
        return priceUsdCents;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public ProductCategory getCategory() {
        return category;
    }
}
