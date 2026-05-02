package com.d360.retailDiscountService.model.document;

import com.d360.retailDiscountService.model.enums.users.UserTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;

    private Long userId;

    private UserTypeEnum userType;

    private LocalDate customerSince;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
