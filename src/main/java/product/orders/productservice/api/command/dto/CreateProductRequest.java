package product.orders.productservice.api.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProductRequest(@NotBlank String name,
                                   String description,
                                   @PositiveOrZero long priceUSDCents,
                                   @NotBlank String category,
                                   @NotBlank String status) {
}
