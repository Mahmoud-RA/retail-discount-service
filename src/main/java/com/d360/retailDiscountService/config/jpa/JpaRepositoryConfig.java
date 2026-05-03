package com.d360.retailDiscountService.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.d360.retailDiscountService.repository")
public class JpaRepositoryConfig {
}