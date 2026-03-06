package product.orders.productservice.api.command.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChangePriceRequest(@NotNull @PositiveOrZero Long priceUSDCents) {
}
