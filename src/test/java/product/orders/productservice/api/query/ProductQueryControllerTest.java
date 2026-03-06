package product.orders.productservice.api.query;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import product.orders.productservice.domain.exception.ProductNotFoundException;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.view.model.ProductView;
import product.orders.productservice.view.service.ProductViewService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductQueryController.class)
class ProductQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductViewService viewService;

    /**
     * Test for verifying the behavior of `getById` method
     * when the product is successfully retrieved.
     */
    @Test
    void testGetById_ProductExists_ReturnsProductResponse() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductView productView = new ProductView(
                productId,
                "Test Product",
                "Test Description",
                1000L,
                "BOOKS",
                "ACTIVE"
        );
        when(viewService.getProduct(productId)).thenReturn(productView);

        String expectedResponse = """
                {
                    "productId": "%s",
                    "name": "Test Product",
                    "description": "Test Description",
                    "priceUSDCents": 1000,
                    "category": "BOOKS",
                    "status": "ACTIVE"
                }
                """.formatted(productId);

        // Act & Assert
        mockMvc.perform(get("/products/{id}", productId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    /**
     * Test for verifying the behavior of `getById` method
     * when the product is not found.
     */
    @Test
    void testGetById_ProductDoesNotExist_ReturnNotFound() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(viewService.getProduct(productId)).thenThrow(new ProductNotFoundException(productId));

        // Act & Assert
        mockMvc.perform(get("/products/{id}", productId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test for verifying the behavior of `getById` method
     * when the input UUID format is invalid.
     */
    @Test
    void testGetById_InvalidUUIDFormat_ReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/products/{id}", "invalid-uuid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetActiveProducts_WithoutCategory_ReturnsArrayOfActiveProducts() throws Exception {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        // Create mocks of product views
        ProductView v1 = mock(ProductView.class);
        when(v1.getProductId()).thenReturn(id1);
        when(v1.getName()).thenReturn("Alpha");
        when(v1.getDescription()).thenReturn("Desc A");
        when(v1.getPriceUSDCents()).thenReturn(1000L);
        when(v1.getCategory()).thenReturn("BOOKS");
        when(v1.getStatus()).thenReturn("ACTIVE");

        ProductView v2 = mock(ProductView.class);
        when(v2.getProductId()).thenReturn(id2);
        when(v2.getName()).thenReturn("Beta");
        when(v2.getDescription()).thenReturn("Desc B");
        when(v2.getPriceUSDCents()).thenReturn(2500L);
        when(v2.getCategory()).thenReturn("ELECTRONICS");
        when(v2.getStatus()).thenReturn("ACTIVE");

        // Mock the pagination
        Page<ProductView> page = new PageImpl<>(List.of(v1, v2));
        when(viewService.getAllActiveProducts(any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Act & Assert
        mockMvc.perform(get("/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // response is a JSON array
                .andExpect(jsonPath("$[0].productId").value(id1.toString()))
                .andExpect(jsonPath("$[0].name").value("Alpha"))
                .andExpect(jsonPath("$[0].description").value("Desc A"))
                .andExpect(jsonPath("$[0].priceUSDCents").value(1000))
                .andExpect(jsonPath("$[0].category").value("BOOKS"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].productId").value(id2.toString()))
                .andExpect(jsonPath("$[1].name").value("Beta"));

        // Assert that the non-category query was used
        verify(viewService).getAllActiveProducts(pageableCaptor.capture());
        verify(viewService, never()).getProductsByCategory(any(), any(), any());

        // Assert that ordered by name
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageSize()).isEqualTo(20); // from @PageableDefault
        assertThat(captured.getSort().getOrderFor("name")).isNotNull();
        assertThat(captured.getSort().getOrderFor("name").isAscending()).isTrue();
    }

    @Test
    void testGetActiveProducts_WithCategory_ReturnsArrayOfActiveProductsByCategory() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        ProductView v = mock(ProductView.class);
        when(v.getProductId()).thenReturn(id);
        when(v.getName()).thenReturn("Book 1");
        when(v.getDescription()).thenReturn("Desc");
        when(v.getPriceUSDCents()).thenReturn(1999L);
        when(v.getCategory()).thenReturn("BOOKS");
        when(v.getStatus()).thenReturn("ACTIVE");

        Page<ProductView> page = new PageImpl<>(List.of(v));
        when(viewService.getProductsByCategory(eq(ProductCategory.BOOKS), eq(ProductStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Act & Assert
        mockMvc.perform(get("/products")
                        .param("category", "BOOKS")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].productId").value(id.toString()))
                .andExpect(jsonPath("$[0].category").value("BOOKS"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        // Verify that only the category query was used
        verify(viewService, never()).getAllActiveProducts(any(Pageable.class));
        verify(viewService).getProductsByCategory(eq(ProductCategory.BOOKS), eq(ProductStatus.ACTIVE), pageableCaptor.capture());

        // Verify that the query was ordered by name and paginated
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageSize()).isEqualTo(20); // from @PageableDefault
        assertThat(captured.getSort().getOrderFor("name")).isNotNull();
        assertThat(captured.getSort().getOrderFor("name").isAscending()).isTrue();
    }

    @Test
    void TestGetActiveProducts_InvalidCategory_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/products")
                        .param("category", "NOT_A_CATEGORY"))
                .andExpect(status().isBadRequest());
    }

}