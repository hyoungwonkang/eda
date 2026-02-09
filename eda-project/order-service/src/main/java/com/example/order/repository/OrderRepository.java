package com.example.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
    
}