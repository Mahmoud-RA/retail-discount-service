package com.d360.retailDiscountService.model.document.snapshot;

import com.d360.retailDiscountService.exception.BusinessException;
import com.d360.retailDiscountService.exception.enums.ErrorCode;
import com.d360.retailDiscountService.model.document.ItemDocument;
import com.d360.retailDiscountService.model.dto.bills.requests.BillItemRequest;
import com.d360.retailDiscountService.model.enums.items.ItemCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemSnapshot {
    private Long itemId;
    private String name;
    private ItemCategoryEnum category;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal unitPrice;
    private Integer quantity;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal totalItemPrice;


    public static List<BillItemSnapshot> toSnapshot(List<ItemDocument> dbItems, List<BillItemRequest> requestItems) {
        return requestItems.stream()
                .map(requestItem -> {
                    ItemDocument item = dbItems.stream()
                            .filter(dbItem -> dbItem.getItemId().equals(requestItem.getItemId()))
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND, "Item not found: " + requestItem.getItemId()));
                    return BillItemSnapshot.builder()
                            .itemId(item.getItemId())
                            .name(item.getName())
                            .category(item.getCategory())
                            .unitPrice(item.getUnitPrice())
                            .quantity(requestItem.getQuantity())
                            .totalItemPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(requestItem.getQuantity())))
                            .build();
                })
                .toList();
    }
}