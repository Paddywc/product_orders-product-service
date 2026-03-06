package product.orders.productservice.domain.service;


import org.springframework.stereotype.Service;
import product.orders.productservice.domain.model.Product;

import java.util.Objects;

@Service
public class ProductDomainServiceImpl implements ProductDomainService {

    /**
     * Business rule:
     * Discontinuing a product is idempotent.
     */
    @Override
    public void deactivate(Product product) {
        Objects.requireNonNull(product, "Product must be provided");

        product.deactivate();
    }

    /**
     * Business rule:
     * Activating a product is idempotent.
     */
    @Override
    public void activate(Product product) {
        Objects.requireNonNull(product, "Product must be provided");

        product.activate();
    }

    @Override
    public void changePrice(Product product, Long newPriceUSDCents){
        Objects.requireNonNull(product, "Product must be provided");
        Objects.requireNonNull(newPriceUSDCents, "Price must be provided");

        product.chancePrice(newPriceUSDCents);
    }
}
