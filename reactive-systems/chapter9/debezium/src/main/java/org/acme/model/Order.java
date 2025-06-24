package org.acme.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order  extends PanacheEntity {

    @Column(nullable = false)
    private Long customerId;

    private String description;

    private BigDecimal total;

    public static Order from(Row row) {
        Order order = new Order();
        order.id = row.getLong("id");
        order.customerId = row.getLong("customerid");
        order.description = row.getString("description");
        order.total = row.getBigDecimal("total");
        return order;
    }
}
