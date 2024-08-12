package com.example.financewatchapi.dto;

import com.example.financewatchapi.model.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRecordDto(@NotBlank String transactionNumber,
                                   @NotNull Type type,
                                   @NotBlank String name,
                                   @NotNull BigDecimal amount,
                                   @NotNull LocalDate transactionDate,
                                   @NotNull LocalDate postDate) {
}