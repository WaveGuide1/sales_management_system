package io.barth.sms.service;

import io.barth.sms.entity.Product;
import io.barth.sms.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImp implements ProductService{

    private final ProductRepository productRepository;

    public ProductServiceImp(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()){
            return product.get();
        }
        throw new RuntimeException("No Content Found");
    }

    @Override
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}