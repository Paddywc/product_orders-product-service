package product.orders.productservice.domain.service;

import product.orders.productservice.domain.model.Product;

public interface ProductDomainService {
    /**
     * Business rule:
     * Discontinuing a product is idempotent.
     */
    void deactivate(Product product);

    /**
     * Business rule:
     * Activating a product is idempotent.
     */
    void activate(Product product);

    void changePrice(Product product, Long newPriceUSDCents);
}
