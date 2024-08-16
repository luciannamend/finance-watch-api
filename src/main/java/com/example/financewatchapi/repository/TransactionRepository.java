package com.example.financewatchapi.repository;

import com.example.financewatchapi.model.TransactionModel;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Id> {
    Optional<TransactionModel> findById(Integer id);

    @Query("""
            SELECT t FROM TransactionModel t
            WHERE t.transactionDate BETWEEN :startDate AND :endDate
            """)
    List<TransactionModel> findByTransactionDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
