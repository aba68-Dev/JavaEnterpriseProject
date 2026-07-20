package com.enterprise.repository;

import com.enterprise.domain.entity.Product;
import com.enterprise.domain.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * JPA repository for Product entities.
 * Extends JpaSpecificationExecutor to support dynamic filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.status = 'ACTIVE'
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:search IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.sku)  LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            @Param("search")     String search,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.status = 'ACTIVE'")
    Page<Product> findLowStockProducts(@Param("threshold") int threshold, Pageable pageable);
}
