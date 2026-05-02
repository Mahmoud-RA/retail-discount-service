package com.d360.retailDiscountService.model.document;

import com.d360.retailDiscountService.model.enums.items.ItemCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class ItemDocument {
    @Id
    private String id;

    private Long itemId;

    private String name;

    private ItemCategoryEnum category;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal unitPrice;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
