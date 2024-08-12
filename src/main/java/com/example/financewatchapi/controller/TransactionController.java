package com.example.financewatchapi.controller;

import com.example.financewatchapi.service.PDFReader;
import com.example.financewatchapi.dto.TransactionRecordDto;
import com.example.financewatchapi.model.TransactionModel;
import com.example.financewatchapi.model.Type;
import com.example.financewatchapi.repository.TransactionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class TransactionController {

    public static final String FILE_PATH = "C:\\Users\\Lucia\\Desktop\\GITHUB\\PDF\\Statements.pdf";
    private final TransactionRepository transactionRepository;
    private final PDFReader pdfReader;

    @Autowired
    public TransactionController(TransactionRepository transactionRepository, PDFReader pdfReader) {
        this.transactionRepository = transactionRepository;
        this.pdfReader = pdfReader;
    }

/*
    @PostMapping("/transactions")
    public ResponseEntity<TransactionModel> saveTransaction(@RequestBody @Valid TransactionRecordDto transactionRecordDto) {

        try {
            String text = pdfReader.extractTextFromPDF(FILE_PATH);
            pdfReader.processText(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var transactionModel = new TransactionModel();
        BeanUtils.copyProperties(transactionRecordDto, transactionModel); //dto -> model
        final Type type = Type.valueOf(String.valueOf(transactionRecordDto.type()));
        transactionModel.setType(type);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionRepository.save(transactionModel));
    }
*/

    @PostMapping("/transactions/upload")
    public ResponseEntity<List<TransactionModel>> saveTransactionFromPDF() throws IOException {
        List<TransactionRecordDto> recordList = null;
        List<TransactionModel> transactionModelList = new ArrayList<>();
        // read and extract transaction information from pdf
        try {
            String text = pdfReader.extractTextFromPDF(FILE_PATH);
            recordList = pdfReader.processText(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert recordList != null : "ERROR: Record list is null";
        //add each record from trans record list into a trans model list
        for (TransactionRecordDto record : recordList) {
            var transaction = new TransactionModel();
            transaction.setTransactionNumber(record.transactionNumber());
            transaction.setType(record.type());
            transaction.setName(record.name());
            transaction.setAmount(record.amount());
            transaction.setTransactionDate(record.transactionDate());
            transaction.setPostDate(record.postDate());

            transactionModelList.add(transaction);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionRepository.saveAll(transactionModelList));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionModel>> getAllTransactions() {
        return ResponseEntity.status(HttpStatus.OK).body(transactionRepository.findAll());
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Object> getOneTransaction(@PathVariable(value = "id") Integer id) {
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        if (transaction.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TRANSACTION NOT FOUND.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(transaction.get());
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<Object> updateTransaction(@PathVariable(value = "id") Integer id,
                                                    @RequestBody @Valid TransactionRecordDto transactionRecordDto) {
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        if (transaction.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TRANSACTION NOT FOUND.");
        }
        var transactionModel = transaction.get();
        BeanUtils.copyProperties(transactionRecordDto, transactionModel);
        return ResponseEntity.status(HttpStatus.OK).body(transactionRepository.save(transactionModel));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Object> deleteTransaction(@PathVariable(value = "id") Integer id) {
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        if (transaction.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TRANSACTION NOT FOUND");
        }
        transactionRepository.delete(transaction.get());
        return ResponseEntity.status(HttpStatus.OK).body("TRANSACTION DELETED SUCCESSFULLY.");
    }

}
