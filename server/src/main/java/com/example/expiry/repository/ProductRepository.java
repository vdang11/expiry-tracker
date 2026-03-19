package com.example.expiry.repository;
import com.example.expiry.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Item, Long> {
    List<Item> findByUser_Id(Long userId);
    List<Item> findByUser_IdAndItemStatus(Long userId, String itemStatus);
}