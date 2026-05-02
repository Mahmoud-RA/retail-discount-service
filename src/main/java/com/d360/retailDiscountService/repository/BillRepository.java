package com.d360.retailDiscountService.repository;

import com.d360.retailDiscountService.model.document.BillDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BillRepository extends MongoRepository<BillDocument, String> {

    Page<BillDocument> findByUserUserId(Long userId, Pageable pageable);
}