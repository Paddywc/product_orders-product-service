package product.orders.productservice.domain.exception;

import java.util.UUID;

public class ConcurrentProductUpdateException extends RuntimeException {

    public ConcurrentProductUpdateException(UUID productId) {
        super("Product was updated concurrently: " + productId);
    }

}
