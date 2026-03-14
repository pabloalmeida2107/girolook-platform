package Girolook.com.GiroLook.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // O "crachá" que aponta para o pai

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private BigDecimal priceAtPurchase;
    private Integer quantity;

    public OrderItem() {}


    public OrderItem(Product product, BigDecimal priceAtPurchase, Integer quantity, Order order) {
        this.product = product;
        this.priceAtPurchase = priceAtPurchase;
        this.quantity = quantity;
        this.order = order;
    }
}
