package com.d360.retailDiscountService.model.document;


import com.d360.retailDiscountService.model.document.snapshot.BillCalculationSnapshot;
import com.d360.retailDiscountService.model.document.snapshot.BillItemSnapshot;
import com.d360.retailDiscountService.model.document.snapshot.UserSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bills")
public class BillDocument {

    @Id
    private String id;

    private UserSnapshot user;

    private List<BillItemSnapshot> items;

    private BillCalculationSnapshot calculation;

    private LocalDateTime createdAt;
}