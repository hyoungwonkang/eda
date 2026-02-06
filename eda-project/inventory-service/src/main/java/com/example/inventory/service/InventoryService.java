package com.example.inventory.service;

import org.springframework.stereotype.Service;

import com.example.inventory.entity.Inventory;
import com.example.inventory.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public void decreaseStock(String productId, Integer quantity) {
        Inventory foundInventory = inventoryRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Inventory not found"));
        foundInventory.decreaseStock(quantity); // 재고 부족 시 예외 발생
    }
}
