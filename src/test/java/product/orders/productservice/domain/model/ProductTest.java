package product.orders.productservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    /**
     * Test case: Successful creation of a product with valid inputs.
     */
    @Test
    void testCreateProduct_ValidInputs_SuccessfulCreation() {
        // Arrange
        String name = "Test Product";
        String description = "Test Product Description";
        long priceUsdCents = 1000L;
        ProductStatus status = ProductStatus.ACTIVE;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // Act
        Product product = Product.create(name, description, priceUsdCents, status, category);

        // Assert
        assertNotNull(product.getProductId());
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(priceUsdCents, product.getPriceUsdCents());
        assertEquals(status, product.getStatus());
        assertEquals(category, product.getCategory());
    }

    /**
     * Test case: Creation fails when the product name is null.
     */
    @Test
    void testCreateProduct_NameIsNull_ThrowsException() {
        // Arrange
        String name = null;
        String description = "Valid Description";
        long priceUsdCents = 1000L;
        ProductStatus status = ProductStatus.ACTIVE;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                Product.create(name, description, priceUsdCents, status, category)
        );
    }

    /**
     * Test case: Creation fails when the product price is negative.
     */
    @Test
    void testCreateProduct_PriceIsNegative_ThrowsException() {
        // Arrange
        String name = "Valid Name";
        String description = "Valid Description";
        long priceUsdCents = -1000L; // Invalid price
        ProductStatus status = ProductStatus.ACTIVE;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                Product.create(name, description, priceUsdCents, status, category)
        );
    }

    /**
     * Test case: Creation of a product with an empty description.
     */
    @Test
    void testCreateProduct_EmptyDescription_CreatedSuccessfully() {
        // Arrange
        String name = "Valid Name";
        String description = ""; // Valid, but empty
        long priceUsdCents = 500L;
        ProductStatus status = ProductStatus.INACTIVE;
        ProductCategory category = ProductCategory.FOOD;

        // Act
        Product product = Product.create(name, description, priceUsdCents, status, category);

        // Assert
        assertNotNull(product.getProductId());
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(priceUsdCents, product.getPriceUsdCents());
        assertEquals(status, product.getStatus());
        assertEquals(category, product.getCategory());
    }

    /**
     * Test case: Creation fails when the category is null.
     */
    @Test
    void testCreateProduct_NullCategory_ThrowsException() {
        // Arrange
        String name = "Valid Name";
        String description = "Valid Description";
        long priceUsdCents = 600L;
        ProductStatus status = ProductStatus.ACTIVE;
        ProductCategory category = null; // Invalid category

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                Product.create(name, description, priceUsdCents, status, category)
        );
    }
}