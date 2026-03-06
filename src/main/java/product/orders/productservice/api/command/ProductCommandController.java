package product.orders.productservice.api.command;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import product.orders.productservice.api.command.dto.ChangePriceRequest;
import product.orders.productservice.api.command.dto.CreateProductRequest;
import product.orders.productservice.api.command.dto.ProductCreatedResponse;
import product.orders.productservice.application.ProductApplicationService;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductCommandController {

    private final ProductApplicationService productApplicationService;

    public ProductCommandController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    // ----------------------------------------------------
    // Create
    // ----------------------------------------------------
    @PostMapping
    public ResponseEntity<ProductCreatedResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        UUID productId = productApplicationService.createProduct(
                request.name(),
                request.description(),
                request.priceUSDCents(),
                ProductStatus.valueOf(request.status()),
                ProductCategory.valueOf(request.category()));

        return new ResponseEntity<>(new ProductCreatedResponse(productId), HttpStatus.CREATED);
    }

    @PatchMapping("/{productId}/price")
    public ResponseEntity<Void> changePrice(
            @PathVariable UUID productId,
            @Valid @RequestBody ChangePriceRequest request) {
        productApplicationService.changePrice(productId, request.priceUSDCents());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable UUID productId) {
        productApplicationService.deactivateProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}/activate")
    public ResponseEntity<Void> activateProduct(@PathVariable UUID productId) {
        productApplicationService.activateProduct(productId);
        return ResponseEntity.noContent().build();
    }

}
