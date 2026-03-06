package product.orders.productservice.view.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import product.orders.productservice.domain.exception.ProductNotFoundException;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.repository.ProductRepository;
import product.orders.productservice.view.mapper.ProductViewMapper;
import product.orders.productservice.view.model.ProductView;
import product.orders.productservice.view.repository.ProductViewRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductViewRepository viewRepository;
    private final ProductRepository productRepository;

    private final ProductViewMapper mapper;

    public ProductViewServiceImpl(ProductViewRepository viewRepository, ProductRepository productRepository, ProductViewMapper mapper) {
        this.viewRepository = viewRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public ProductView getProduct(UUID productId) {
        return viewRepository.findById(productId)
                .orElseGet(() -> loadAndCache(productId));
    }

    /**
     * Saves a product view to the cache
     * @param product the product to save
     */
    @Override
    public void upsert(Product product){
        ProductView view = mapper.toView(product);
        viewRepository.save(view);
    }

    @Override
    public  Page<ProductView>  getAllActiveProducts(Pageable pageable) {
        Page<ProductView> cached = viewRepository.findByStatus(ProductStatus.ACTIVE.name(), pageable);
        if (!cached.isEmpty()) {
            return cached;
        }

        Page<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return cacheAndWrapAndReturnProductViews(products);
    }


    @Override
    public  Page<ProductView>  getProductsByCategory(ProductCategory category, ProductStatus optionalStatusFiler, Pageable pageable) {

        Page<ProductView> cached = optionalStatusFiler == null
                ? viewRepository.findByCategory(category.name(), pageable)
                : viewRepository.findByCategoryAndStatus(category.name(), optionalStatusFiler.name(), pageable);

        if (!cached.isEmpty()) {
            return cached;
        }

        Page<Product> products = optionalStatusFiler == null
                ? productRepository.findByCategory(category, pageable)
                : productRepository.findByCategoryAndStatus(category, optionalStatusFiler, pageable);
        return cacheAndWrapAndReturnProductViews(products);
    }


    private ProductView loadAndCache(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );

        ProductView view = mapper.toView(product);
        viewRepository.save(view);
        return view;
    }

    private  Page<ProductView> cacheAndWrapAndReturnProductViews(Page<Product> products) {
        List<ProductView> views = products.stream()
                .map(mapper::toView)
                .toList();

        viewRepository.saveAll(views);
        return new PageImpl<>(
                views,
                products.getPageable(),
                products.getTotalElements()
        );
    }
}
