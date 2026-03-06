package product.orders.productservice.application;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import product.orders.productservice.domain.exception.ConcurrentProductUpdateException;
import product.orders.productservice.domain.exception.ProductNotFoundException;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.domain.service.ProductDomainService;
import product.orders.productservice.messaging.event.ProductCreatedEvent;
import product.orders.productservice.messaging.producer.ProductEventProducer;
import product.orders.productservice.repository.ProductRepository;
import product.orders.productservice.view.service.ProductViewService;

import java.util.UUID;

@Service
public class ProductApplicationServiceImpl implements ProductApplicationService {

    private final ProductDomainService domainService;

    private final ProductRepository productRepository;

    private final ProductViewService viewService;

    private final ProductEventProducer productEventProducer;

    public ProductApplicationServiceImpl(ProductDomainService domainService, ProductRepository productRepository, ProductViewService viewService, ProductEventProducer productEventProducer) {
        this.domainService = domainService;
        this.productRepository = productRepository;
        this.viewService = viewService;
        this.productEventProducer = productEventProducer;
    }

    // ----------------------------------------------------
    // Create
    // ----------------------------------------------------

    /**
     * Creates a new product with the given details and save it to the DB. Fire a product created event.
     *
     * @param name          The name of the product.
     * @param description   The description of the product.
     * @param priceUsdCents The price of the product in USD cents.
     * @param status        The status of the product.
     * @param category      The category of the product.
     * @return The UUID of the newly created product.
     */
    @Transactional
    @Override
    public UUID createProduct(String name, String description, long priceUsdCents, ProductStatus status, ProductCategory category) {
        Product product = Product.create(
                name,
                description,
                priceUsdCents,
                status,
                category
        );

        saveProduct(product);

        // Fire product created event
        ProductCreatedEvent event = ProductCreatedEvent.of(product.getProductId(), product.getName());
        productEventProducer.publish(event);

        return product.getProductId();
    }

    // ----------------------------------------------------
    // Update
    // ----------------------------------------------------

    @Transactional
    @Override
    public void changePrice(UUID productId, Long newPriceUSDCents) {
        Product product = findProduct(productId);

        domainService.changePrice(product, newPriceUSDCents);

        saveProduct(product);
    }

    @Transactional
    @Override
    public void deactivateProduct(UUID productId) {
        Product product = findProduct(productId);

        domainService.deactivate(product);

        saveProduct(product);
    }

    @Transactional
    @Override
    public void activateProduct(UUID productId) {
        Product product = findProduct(productId);

        domainService.activate(product);

        saveProduct(product);
    }


    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );
    }

    /**
     * Catch optimistic locking exceptions if thrown and throw a concurrent update exception. Also invalidates the
     * product views cache
     *
     * @param product the product to save
     */
    private void saveProduct(Product product) {
        try {
            productRepository.save(product);
            viewService.upsert(product);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            throw new ConcurrentProductUpdateException(product.getProductId());
        }
    }
}
