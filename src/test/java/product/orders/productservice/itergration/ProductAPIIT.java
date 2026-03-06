package product.orders.productservice.itergration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import product.orders.productservice.api.command.dto.ChangePriceRequest;
import product.orders.productservice.api.command.dto.CreateProductRequest;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.repository.ProductRepository;
import product.orders.productservice.view.repository.ProductViewRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Testcontainers
class ProductAPIIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepository productRepository;


    @Autowired
    ProductViewRepository productViewRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // Redis (read model)
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

    }

    @Autowired(required = false)
    RedisTemplate<Object, Object> redisTemplate;

    @BeforeEach
    void cleanRedis() {
        // Prefer repository deleteAll; if indexes/keys ever linger, template flush is a safety net.
        productViewRepository.deleteAll();

        if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        }
    }

    @Test
    void testGetActiveProducts_InactiveAndActiveProductExist_ReturnsOnlyActive() throws Exception {
        // Arrange
        Product inactiveProduct = Product.create(
                "Old Phone",
                "Deprecated",
                199_00L,
                ProductStatus.INACTIVE,
                ProductCategory.ELECTRONICS
        );
        Product activeProduct = Product.create(
                "New Phone",
                "Latest Model",
                499_00L,
                ProductStatus.ACTIVE,
                ProductCategory.ELECTRONICS
        );

        productRepository.save(inactiveProduct);
        productRepository.save(activeProduct);

        mockMvc.perform(get("/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("New Phone"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].category").value("ELECTRONICS"))
                .andExpect(jsonPath("$[0].priceUSDCents").value(499_00L));
    }


    @Test
    void testFullFlow_PostCreateProductThenQueryProduct_ReturnsPostedProduct() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "Phone",
                "Smart phone",
                10000L,
                "ELECTRONICS",
                "ACTIVE");

        // Act & Assert
        String createResponse =
                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.productId").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        UUID productId = UUID.fromString(
                objectMapper.readTree(createResponse)
                        .get("productId")
                        .asString()
        );

        assertThat(productId).isNotNull();

        // Save json response

        // Query product by productId
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Query active products
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId.toString()));
    }

    @Test
    void testFullFlow_CreateUpdateThenQueryProduct_ReturnsUpdatedProduct() throws Exception {
        // Arrange
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "Phone",
                "Smart phone",
                10000L,
                "ELECTRONICS",
                "ACTIVE");

        // Act & Assert
        String createResponse =
                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.productId").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        UUID productId = UUID.fromString(
                objectMapper.readTree(createResponse)
                        .get("productId")
                        .asString()
        );

        assertThat(productId).isNotNull();

        // Update the price
        ChangePriceRequest changePriceRequest = new ChangePriceRequest(15000L);
        mockMvc.perform(patch("/products/{id}/price", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePriceRequest)))
                .andExpect(status().isNoContent());

        // Assert that the product is still active and the price has been updated
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.priceUSDCents").value(15000L));

        // Deactivate the product
        mockMvc.perform(put("/products/{productId}/deactivate", productId))
                .andExpect(status().isNoContent());

        // Assert that the product is deactivated
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}
