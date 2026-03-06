package product.orders.productservice.itergration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import product.orders.productservice.application.ProductApplicationService;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.repository.ProductRepository;
import product.orders.productservice.view.model.ProductView;
import product.orders.productservice.view.service.ProductViewService;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("integrationtest")
public class ProductViewWriteThroughIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    ProductApplicationService productApplicationService;

    @Autowired
    ProductViewService productViewService;

    @Autowired
    ProductRepository productRepository;

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void testWriteThough_UpdateProduct_OverwritesRedisView(){
        // Arrange
        Long originalPrice = 2000L;
        Long newPrice = 5000L;

        UUID productId = productApplicationService.createProduct(
                "Test Name",
                "Test desc",
                originalPrice,
                ProductStatus.ACTIVE,
                ProductCategory.BOOKS
        );

        // Prime the cache
        ProductView initialView = productViewService.getProduct(productId);
        assertThat(initialView.getPriceUSDCents()).isEqualTo(originalPrice);

        // Act
        productApplicationService.changePrice(productId, newPrice);

        // Assert: Redis now reflects updated data
        ProductView updatedView =
                productViewService.getProduct(productId);

        assertThat(updatedView.getPriceUSDCents()).isEqualTo(newPrice);
    }
}
