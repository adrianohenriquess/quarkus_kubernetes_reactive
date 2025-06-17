package org.acme.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Customer extends PanacheEntity {

    @Column(nullable = false)
    @NotBlank(message = "Customer name can not be blank")
    @Length(min = 3, message = "Customer names must be at least three characters")
    private String name;

    @Transient
    private List<Order> orders = new ArrayList<>();
}
