package product.orders.productservice.view.mapper;

import org.springframework.stereotype.Component;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.view.model.ProductView;

@Component
public class ProductViewMapper {

    public ProductView toView(Product product) {
        return new ProductView(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPriceUsdCents(),
                product.getCategory().name(),
                product.getStatus().name()
        );
    }
}
