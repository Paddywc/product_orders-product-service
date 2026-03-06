package product.orders.productservice.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import product.orders.productservice.domain.exception.ConcurrentProductUpdateException;
import product.orders.productservice.domain.exception.ProductNotFoundException;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.domain.service.ProductDomainService;
import product.orders.productservice.messaging.producer.ProductEventProducer;
import product.orders.productservice.repository.ProductRepository;
import product.orders.productservice.view.service.ProductViewService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceImplTest {

    @Mock
    private ProductDomainService domainService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductViewService viewService;

    @Mock
    private ProductEventProducer productEventProducer;

    @InjectMocks
    private ProductApplicationServiceImpl service;


    @Test
    void createProduct_PassedValidProductData_SavesProductAndUpdatesView() {
        // Arrange
        UUID productId = UUID.randomUUID();
        String name = "Product Name";
        String description = "Product Description";
        long priceUsdCents = 1500;
        ProductStatus status = ProductStatus.ACTIVE;
        ProductCategory category = ProductCategory.ELECTRONICS;
        Product product = mock(Product.class);
        when(product.getProductId()).thenReturn(productId);

        try (var mocked = mockStatic(Product.class)) {
            mocked.when(() -> Product.create(name, description, priceUsdCents, status, category)).thenReturn(product);

            // Act
            UUID result = service.createProduct(name, description, priceUsdCents, status, category);

            // Assert
            assertThat(result).isEqualTo(productId);
            verify(productRepository).save(any());
            verify(viewService).upsert(product);
        }
    }


    @Test
    void testChangePrice_GivenValidData_DelegatesToDomainAndSaves() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = mock(Product.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        service.changePrice(productId, 12000L);

        verify(domainService).changePrice(product, 12000L);
        verify(productRepository).save(product);
        verify(viewService).upsert(product);
    }

    @Test
    void testChangePrice_ProductNotFound_ThrowsException() {
        // Arrange
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                service.changePrice(productId, 1000L)
        ).isInstanceOf(ProductNotFoundException.class);
    }


    @Test
    void testActivateProduct_GivenValidData_DelegatesToDomainAndSaves() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = mock(Product.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        service.activateProduct(productId);

        verify(domainService).activate(product);
        verify(productRepository).save(product);
        verify(viewService).upsert(product);
    }

    @Test
    void testActivateProduct_ProductNotFound_ThrowsException() {
        // Arrange
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                service.activateProduct(productId)
        ).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void testDeactivateProduct_GivenValidData_DelegatesToDomainAndSaves() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = mock(Product.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        service.deactivateProduct(productId);

        verify(domainService).deactivate(product);
        verify(productRepository).save(product);
        verify(viewService).upsert(product);
    }

    @Test
    void testDeactivateProduct_ProductNotFound_ThrowsException() {
        // Arrange
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                service.deactivateProduct(productId)
        ).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void testDeactivateProduct_OptimisticLockingFailing_ThrowsConcurrentProductUpdateException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = mock(Product.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenThrow(new OptimisticLockingFailureException("Optimistic locking exception"));

        // Act & Assert
        assertThrows(ConcurrentProductUpdateException.class, () -> {
            service.deactivateProduct(productId);
        });

        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(viewService);
    }

}