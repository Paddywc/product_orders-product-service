package product.orders.productservice.messaging.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ProductCreatedEvent(@NotNull UUID eventId,
                                  @NotNull UUID productId,
                                  @NotEmpty String name,
                                  Instant occurredAt) {

    public  static ProductCreatedEvent of(UUID productId, String name) {
        return new ProductCreatedEvent(UUID.randomUUID(), productId, name, Instant.now());
    }
}
