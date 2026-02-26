package Girolook.com.GiroLook.models;

import Girolook.com.GiroLook.models.enums.OrderStatus;

import Girolook.com.GiroLook.models.enums.OrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "Order")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private BigDecimal totalAmount;


    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDate updatedAt;





}
