package Girolook.com.GiroLook.models;

import Girolook.com.GiroLook.models.enums.Category;
import Girolook.com.GiroLook.models.enums.ProductStatus;
import Girolook.com.GiroLook.models.enums.ProductStatus;
import Girolook.com.GiroLook.models.enums.ProductType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter

@SQLDelete(sql = "UPDATE products SET active = false WHERE id = ?")

@SQLRestriction("active = true")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private String color;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Product(){}

    public Product(Store store, String name, String description, BigDecimal price, String size, Category category, String color, String imageUrl, ProductType type) {
        this.store = store;
        this.name = name;
        this.description = description;
        this.price = price;
        this.size = size;
        this.category = category;
        this.color = color;
        this.imageUrl = imageUrl;
        this.type = type;
    }
}
