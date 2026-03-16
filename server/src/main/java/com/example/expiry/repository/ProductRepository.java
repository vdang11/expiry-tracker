package com.example.expiry.repository;
import com.example.expiry.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Item, Long> {
}