package product.orders.productservice.application;

import org.springframework.transaction.annotation.Transactional;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;

import java.util.UUID;

public interface ProductApplicationService {
    /**
     * Creates a new product with the given details and save it to the DB
     *
     * @param name          The name of the product.
     * @param description   The description of the product.
     * @param priceUsdCents The price of the product in USD cents.
     * @param status        The status of the product.
     * @param category      The category of the product.
     * @return The UUID of the newly created product.
     */
    @Transactional
    UUID createProduct(String name, String description, long priceUsdCents, ProductStatus status, ProductCategory category);

    @Transactional
    void changePrice(UUID productId, Long newPriceUSDCents);

    @Transactional
    void deactivateProduct(UUID productId);

    @Transactional
    void activateProduct(UUID productId);

}
