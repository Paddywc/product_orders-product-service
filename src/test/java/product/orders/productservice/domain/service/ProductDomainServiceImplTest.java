package product.orders.productservice.domain.service;

import org.junit.jupiter.api.Test;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductDomainServiceImplTest {

    /**
     * Test case: Successfully changing the price of a product with valid inputs.
     */
    @Test
    void testChangePrice_ValidInputs_SuccessfulUpdate() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        Product mockProduct = mock(Product.class);
        long newPrice = 1500L;

        // Act
        service.changePrice(mockProduct, newPrice);

        // Assert
        verify(mockProduct, times(1)).chancePrice(newPrice);
    }

    /**
     * Test case: Attempting to change the price of a product with a null product instance.
     */
    @Test
    void testChangePrice_NullProduct_ThrowsException() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        Long newPrice = 1500L;

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                service.changePrice(null, newPrice)
        );
        assertEquals("Product must be provided", exception.getMessage());
    }

    /**
     * Test case: Attempting to change the price of a product with a null price.
     */
    @Test
    void TestChangePrice_NullPrice_ThrowsException() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        Product mockProduct = mock(Product.class);

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                service.changePrice(mockProduct, null)
        );
        assertEquals("Price must be provided", exception.getMessage());
    }

    /**
     * Test case: Verify that changing the price updates the product's price correctly.
     */
    @Test
    void changePrice_ValidInputs_UpdatesProductCorrectly() {
        // Arrange
        Product product = Product.create(
                "Test Product",
                "Test Description",
                1000L,
                ProductStatus.ACTIVE,
                ProductCategory.FOOD);
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        long newPrice = 2000L;

        // Act
        service.changePrice(product, newPrice);

        // Assert
        assertEquals(newPrice, product.getPriceUsdCents());
    }


    /**
     * Test case: Successfully activating a product delegates to the entity.
     */
    @Test
    void testActivate_ValidProduct_DelegatesToEntity() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        Product mockProduct = mock(Product.class);

        // Act
        service.activate(mockProduct);

        // Assert
        verify(mockProduct, times(1)).activate();
        verifyNoMoreInteractions(mockProduct);
    }

    /**
     * Test case: Activating with null product throws with the correct message.
     */
    @Test
    void testActivate_NullProduct_ThrowsException() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                service.activate(null)
        );
        assertEquals("Product must be provided", exception.getMessage());
    }

    /**
     * Test case: Successfully deactivating a product delegates to the entity.
     */
    @Test
    void testDeactivate_ValidProduct_DelegatesToEntity() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        Product mockProduct = mock(Product.class);

        // Act
        service.deactivate(mockProduct);

        // Assert
        verify(mockProduct, times(1)).deactivate();
        verifyNoMoreInteractions(mockProduct);
    }

    /**
     * Test case: Deactivating with null product throws with the correct message.
     */
    @Test
    void testDeactivate_NullProduct_ThrowsException() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                service.deactivate(null)
        );
        assertEquals("Product must be provided", exception.getMessage());
    }

    /**
     * Test case: Activating updates status on a real product instance.
     */
    @Test
    void activate_ValidProduct_UpdatesStatus() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        Product product = Product.create(
                "Test Product",
                "Test Description",
                1000L,
                ProductStatus.INACTIVE,
                ProductCategory.FOOD
        );

        // Act
        service.activate(product);

        // Assert
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    /**
     * Test case: Deactivating updates status on a real product instance.
     */
    @Test
    void deactivate_ValidProduct_UpdatesStatus() {
        // Arrange
        ProductDomainServiceImpl service = new ProductDomainServiceImpl();
        Product product = Product.create(
                "Test Product",
                "Test Description",
                1000L,
                ProductStatus.ACTIVE,
                ProductCategory.FOOD
        );

        // Act
        service.deactivate(product);

        // Assert
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }
}