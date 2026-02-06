package com.example.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventory.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    
}
