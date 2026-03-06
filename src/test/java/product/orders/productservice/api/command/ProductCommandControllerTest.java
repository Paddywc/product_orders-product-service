package product.orders.productservice.api.command;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import product.orders.productservice.application.ProductApplicationService;
import product.orders.productservice.domain.exception.ConcurrentProductUpdateException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductCommandController.class)
class ProductCommandControllerTest {


    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductApplicationService productApplicationService;


    @Autowired
    private WebApplicationContext context;

    @Test
    void testCreateProduct_WithValidData_ReturnsCreatedResponse() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(productApplicationService.createProduct(
                anyString(),
                anyString(),
                anyLong(),
                any(),
                any()
        )).thenReturn(productId);

        String requestContent = """
                {
                    "name": "Test Product",
                    "description": "A description for the test product",
                    "priceUSDCents": 1500,
                    "category": "ELECTRONICS",
                    "status": "ACTIVE"
                }
                """;

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId.toString()));
    }

    @Test
    void createProduct_withInvalidData_returnsBadRequest() throws Exception {
        // Arrange
        String requestContent = """
                {
                    "name": "",
                    "description": "A description for the test product",
                    "priceUsdCents": -1500,
                    "category": "ELECTRONICS",
                    "status": ""
                }
                """;

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_WithUnexpectedError_ReturnsInternalServerError() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(productApplicationService.createProduct(
                anyString(),
                anyString(),
                anyLong(),
                any(),
                any()
        )).thenReturn(productId);
        when(productApplicationService.createProduct(anyString(), anyString(), anyLong(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        String requestContent = """
                {
                    "name": "Test Product",
                    "description": "A description for the test product",
                    "priceUSDCents": 1500,
                    "category": "ELECTRONICS",
                    "status": "ACTIVE"
                }
                """;

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testChangePrice_ValidRequest_ReturnsNoContent() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();

        String requestContent = """
                {
                    "priceUSDCents": 12000
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/products/{productId}/price", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isNoContent());

        verify(productApplicationService)
                .changePrice(productId, 12000L);
    }

    @Test
    void testChangePrice_WhenConcurrentUpdate_ReturnsConflict() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        String requestContent = """
                {
                    "priceUSDCents": 12000
                }
                """;

        doThrow(new ConcurrentProductUpdateException(productId))
                .when(productApplicationService)
                .changePrice(productId, 12000L);

        mockMvc.perform(
                        patch("/products/{productId}/price", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                .andExpect(status().isConflict());
    }

    @Test
    void testDeactivateProduct_PostedWithProductId_ReturnsNoContent() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();

        doNothing().when(productApplicationService)
                .deactivateProduct(productId);

        // Act & Assert
        mockMvc.perform(put("/products/{productId}/deactivate", productId))
                .andExpect(status().isNoContent());
        verify(productApplicationService).deactivateProduct(productId);
    }

    @Test
    void testActivateProduct_PostedWithProductId_ReturnsNoContent() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();

        doNothing().when(productApplicationService)
                .activateProduct(productId);

        // Act & Assert
        mockMvc.perform(put("/products/{productId}/activate", productId))
                .andExpect(status().isNoContent());
        verify(productApplicationService).activateProduct(productId);
    }
}