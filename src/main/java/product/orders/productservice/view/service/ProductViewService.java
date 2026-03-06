package product.orders.productservice.view.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.view.model.ProductView;

import java.util.List;
import java.util.UUID;

public interface ProductViewService {
    ProductView getProduct(UUID productId);


    /**
     * Saves a product view to the cache
     * @param product the product to save
     */
    void upsert(Product product);

    Page<ProductView> getAllActiveProducts(Pageable pageable);

    Page<ProductView>  getProductsByCategory(ProductCategory category, ProductStatus optionalStatusFiler, Pageable pageable);
}
