package com.d360.retailDiscountService.repository;

import com.d360.retailDiscountService.model.document.ItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends MongoRepository<ItemDocument, String> {

    Optional<ItemDocument> findByItemId(Long itemId);

    List<ItemDocument> findByItemIdIn(List<Long> itemIds);
}
