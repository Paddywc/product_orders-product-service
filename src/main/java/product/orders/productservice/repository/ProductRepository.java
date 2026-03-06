package product.orders.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

    Page<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status, Pageable pageable);
}
