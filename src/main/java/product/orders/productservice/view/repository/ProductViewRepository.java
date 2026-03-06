package product.orders.productservice.view.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import product.orders.productservice.view.model.ProductView;

import java.util.UUID;

@Repository
public interface ProductViewRepository extends CrudRepository<ProductView, UUID> {

    Page<ProductView> findByStatus(String status, Pageable pageable);

    Page<ProductView> findByCategory(String category, Pageable pageable);

    Page<ProductView> findByCategoryAndStatus(String category, String status, Pageable pageable);


}
