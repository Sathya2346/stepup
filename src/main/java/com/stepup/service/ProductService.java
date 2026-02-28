package com.stepup.service;

import com.stepup.model.Product;
import com.stepup.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.stepup.repository.ProductSpecification;
import java.math.BigDecimal;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getLatestProducts(int count) {
        return productRepository.findAll(PageRequest.of(0, count, Sort.by("id").descending())).getContent();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice,
            org.springframework.data.domain.Sort sort) {
        if (sort == null)
            sort = org.springframework.data.domain.Sort.unsorted();

        Specification<Product> spec = Specification.where(ProductSpecification.hasKeyword(keyword))
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.priceBetween(minPrice, maxPrice));

        return productRepository.findAll(spec, sort);
    }

    public List<Product> getRecommendations(String context, String categoryOverride) {
        String category = categoryOverride;
        String keyword = null;
        String search = context.toLowerCase();

        if (search.contains("women"))
            category = "Womens";
        else if (search.contains("men"))
            category = "Mens";
        else if (search.contains("kid"))
            category = "Kids";
            
        // Final fallback to override if provided and not already set by keywords
        if (category == null) category = categoryOverride;

        if (search.contains("shoe") || search.contains("footwear")) {
            // Keep category filter
        } else if (search.contains("clog")) {
            keyword = "clog";
        } else if (search.contains("heel")) {
            keyword = "heel";
        } else if (search.contains("run")) {
            keyword = "running";
        }

        // Search primarily by category if detected, otherwise fallback to
        // keyword="shoe"
        if (category == null && keyword == null) {
            keyword = "shoe";
        }

        return searchProducts(keyword, category, null, null,
                org.springframework.data.domain.Sort.by("rating").descending());
    }
}
