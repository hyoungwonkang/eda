package com.example.order.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {
    
    private final RestClient restClient;
    
    public InventoryClient() {
        this.restClient = RestClient.builder()
            .baseUrl("http://localhost:8082")
            .build();
    }
    
    public Integer getStock(String productId) {
        return restClient.get()
            .uri("/api/inventory/{productId}/stock", productId)
            .retrieve()
            .body(Integer.class);
    }
}
