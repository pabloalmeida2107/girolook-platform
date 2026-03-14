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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

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
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Order(){}

    public Order(User user, Store store, List<OrderItem> items, OrderStatus orderStatus, BigDecimal totalAmount, OrderType orderType, LocalDate rentalStartDate, LocalDate rentalEndDate) {
        this.user = user;
        this.store = store;
        this.items = items;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.orderType = orderType;
        this.rentalStartDate = rentalStartDate;
        this.rentalEndDate = rentalEndDate;


        if (items != null) {
            items.forEach(item -> item.setOrder(this));
        }
    }


    public Order(User user, Store store, List<OrderItem> items, OrderStatus orderStatus, BigDecimal totalAmount, OrderType orderType) {
        this.user = user;
        this.store = store;
        this.items = items;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.orderType = orderType;

        if (items != null) {
            items.forEach(item -> item.setOrder(this));
        }
    }
}
