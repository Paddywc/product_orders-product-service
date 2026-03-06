package product.orders.productservice.api.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import product.orders.productservice.api.query.dto.ProductResponse;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.view.model.ProductView;
import product.orders.productservice.view.service.ProductViewService;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductQueryController {

    private final ProductViewService viewService;

    public ProductQueryController(ProductViewService viewService) {
        this.viewService = viewService;
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        ProductView view = viewService.getProduct(id);
        return ProductResponse.from(view);
    }

    @GetMapping
    public Iterable<ProductResponse> getActiveProducts(
            @RequestParam(name = "category", required = false) ProductCategory category,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ProductView> productViews;
        if (category != null) {
            productViews = viewService.getProductsByCategory(category, ProductStatus.ACTIVE, pageable);
        } else {
            productViews = viewService.getAllActiveProducts(pageable);
        }

        // Map views to product responses
        return productViews.stream()
                .map(ProductResponse::from)
                .toList();
    }


}
