package com.example.financewatchapi.repository;

import com.example.financewatchapi.model.TransactionModel;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Id> {
}
