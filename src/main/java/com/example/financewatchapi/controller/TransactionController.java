package com.example.financewatchapi.controller;

import com.example.financewatchapi.dto.TransactionRecordDto;
import com.example.financewatchapi.model.TransactionModel;
import com.example.financewatchapi.model.Type;
import com.example.financewatchapi.repository.TransactionRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TransactionController {

    @Autowired //acesso aos metodos JPA
    TransactionRepository transactionRepository;

    @PostMapping("/transactions")
    public ResponseEntity<TransactionModel> saveTransaction(@RequestBody @Valid TransactionRecordDto transactionRecordDto){
        var transactionModel = new TransactionModel();
        BeanUtils.copyProperties(transactionRecordDto, transactionModel); //dto -> model
        final Type type = Type.valueOf(transactionRecordDto.type().toUpperCase());
        transactionModel.setType(type);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionRepository.save(transactionModel));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionModel>> getAllTransactions(){
        return ResponseEntity.status(HttpStatus.OK).body(transactionRepository.findAll());
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Object> getOneTransaction(@PathVariable(value = "id") Integer id){
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        if(transaction.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TRANSACTION NOT FOUND.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(transaction.get());
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<Object> updateTransaction(@PathVariable(value = "id") Integer id,
                                                    @RequestBody @Valid TransactionRecordDto transactionRecordDto){
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        if(transaction.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TRANSACTION NOT FOUND.");
        }
        var transactionModel = transaction.get();
        BeanUtils.copyProperties(transactionRecordDto, transactionModel);
        return ResponseEntity.status(HttpStatus.OK).body(transactionRepository.save(transactionModel));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Object> deleteTransaction(@PathVariable(value = "id") Integer id){
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        if(transaction.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TRANSACTION NOT FOUND");
        }
        transactionRepository.delete(transaction.get());
        return ResponseEntity.status(HttpStatus.OK).body("TRANSACTION DELETED SUCCESSFULLY.");
    }



}
