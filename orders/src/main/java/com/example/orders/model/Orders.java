package com.example.orders.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "1", message = "Debe indicar el producto")
    private Long product;

    @DecimalMin(value = "1", message = "La cantidad minima debe ser 1 unidad")
    @DecimalMax(value = "10000", message = "La cantidad no puede superar las 10.000 unidades")
    private Integer amount;

    private LocalDateTime date;

    @PrePersist
    private void onCreate() {
        date = LocalDateTime.now();
    }

}
