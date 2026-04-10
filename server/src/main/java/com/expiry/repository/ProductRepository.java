package com.expiry.repository;

import com.expiry.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Item, Long> {

    List<Item> findByUser_Id(Long userId);

    List<Item> findByUser_IdAndItemStatus(Long userId, String itemStatus);

    Optional<Item> findByIdAndUser_Id(Long id, Long userId);
}