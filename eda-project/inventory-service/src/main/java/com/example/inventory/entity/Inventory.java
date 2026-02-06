package com.example.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventories")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Inventory {
    
    @Id
    private String productId;
    private Integer stock;

    public void decreaseStock(Integer quantity) {
        if (this.stock < quantity) {
            throw new RuntimeException("Out of stock");
        }
        this.stock -= quantity;
    }
}
