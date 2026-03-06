package product.orders.productservice.api.query.dto;

import product.orders.productservice.view.model.ProductView;

import java.util.UUID;

public record ProductResponse(UUID productId,
                              String name,
                              String description,
                              Long priceUSDCents,
                              String category,
                              String status) {

    public static ProductResponse from(ProductView product) {
        return new ProductResponse(product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPriceUSDCents(),
                product.getCategory(),
                product.getStatus());
    }
}
