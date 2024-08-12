package com.example.financewatchapi;

import com.example.financewatchapi.dto.TransactionRecordDto;
import com.example.financewatchapi.model.TransactionModel;
import com.example.financewatchapi.model.Type;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class PDFReader {
    private static final Pattern SPECIFIC_LINE_PATTERN = Pattern.compile(
            "^\\d+\\s+\\w{3}\\s+\\d+\\s+\\w{3}\\s+\\d+\\s+.*\\s+\\d+.*\\.\\d{2}-?$");
    public static final String GENERAL_LINE_PATTERN = "^\\d+\\s+\\w{3}\\s+\\d+\\s+\\w{3}\\s+\\d+.*";
    public static final String LINE_BREAK_PATTERN = "\\r?\\n";
    public static final String SPACE_REGEX = "\\s+";
    public static final String COMMA = ",";
    public static final String NEGATIVE = "-";
    public static final String EMPTY_STRING = "";
    public static final String SPACE_STRING = " ";
    public static final String DATE_PATTERN = "MMM d yyyy";
    public static final String YEAR = " 2024";

    public String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(document);
        }
    }

    public List<TransactionRecordDto> processText(String text) {

        List<TransactionRecordDto> recordDtoList = new ArrayList<>();
        String[] lines = text.split(LINE_BREAK_PATTERN);
        StringBuilder entry = new StringBuilder();

        for (String line : lines) {
            checkToSkipOrAppend(line, entry);

            if (isCompleteEntry(entry.toString())) {
                String transaction = entry.toString();
                String[] data = transaction.split(SPACE_REGEX);
                StringBuilder buildTransactionDate = new StringBuilder();
                StringBuilder buildPostDate = new StringBuilder();
                StringBuilder buildDescription = new StringBuilder();
                String transactionNumber = "";
                int lineNumber = 1;

                TransactionModel transactionModel = new TransactionModel();

                for (String info : data) {
                    if (lineNumber == 1) {
                        transactionNumber = setTransactionNumber(info);
                    } else if (lineNumber == 2 || lineNumber == 3) {
                        buildTransactionDate.append(info).append(SPACE_STRING);
                    } else if (lineNumber == 4 || lineNumber == 5) {
                        buildPostDate.append(info).append(SPACE_STRING);
                    } else if (lineNumber > 5 && lineNumber < data.length) {
                        buildDescription.append(info).append(SPACE_STRING);
                    } else if (lineNumber == data.length) {
                        processTypeAndAmount(transactionModel, info, buildDescription.toString());
                    }
                    lineNumber += 1;
                }

                processDate(transactionModel, buildTransactionDate, buildPostDate);
                processDescription(transactionModel, buildDescription);

                Type type = transactionModel.getType();
                String description = transactionModel.getName();
                BigDecimal amount = transactionModel.getAmount();
                LocalDate transactionDate = transactionModel.getTransactionDate();
                LocalDate postDate = transactionModel.getPostDate();

                TransactionRecordDto newRecord = createTransactionRecord(transactionNumber, type, description, amount,
                        transactionDate, postDate);

                recordDtoList.add(newRecord);

                entry.setLength(0);
            }
        }
        return recordDtoList;
    }

    public static TransactionRecordDto createTransactionRecord(String transactionNumber, Type type, String description,
                                                               BigDecimal amount, LocalDate transactionDate,
                                                               LocalDate postDate) {
        return new TransactionRecordDto(transactionNumber, type, description, amount, transactionDate, postDate);
    }

    public static String setTransactionNumber(String transactionNumber) {
        System.out.println("1- TRANSACTION: " + transactionNumber);
        return transactionNumber;
    }

    public static String processDescription(TransactionModel transactionModel, StringBuilder buildDescription) {
        String description = buildDescription.toString().trim();
        System.out.println("6- DESCRIPTION: " + description + "\n");
        transactionModel.setName(description);
        return description;
    }

    public static void processDate(TransactionModel transactionModel, StringBuilder buildTransactionDate,
                                   StringBuilder buildPostDate) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_PATTERN);

        String transactionDate = buildTransactionDate.toString().trim() + YEAR;
        LocalDate transactionDateFormatted = LocalDate.parse(transactionDate, dateFormat);
        System.out.println("4- TRANSACTION DATE: " + transactionDateFormatted);
        transactionModel.setTransactionDate(transactionDateFormatted);

        String postDate = buildPostDate.toString().trim() + YEAR;
        LocalDate postDateFormatted = LocalDate.parse(postDate, dateFormat);
        System.out.println("5- POST DATE: " + postDateFormatted);
        transactionModel.setPostDate(postDateFormatted);
    }

    public static void checkToSkipOrAppend(String line, StringBuilder entry) {
        if (entry.length() == 0 && shouldSkipLine(line)) {
            return;
        }
        if (entry.length() != 0) {
            entry.append(" ").append(line.trim());
        } else {
            entry.append(line.trim());
        }
    }

    public static boolean shouldSkipLine(String line) {
        return !line.matches(GENERAL_LINE_PATTERN);
    }

    public static boolean isCompleteEntry(String entry) {
        return SPECIFIC_LINE_PATTERN.matcher(entry).matches();
    }

    public static void processTypeAndAmount(TransactionModel transactionModel, String info, String description) {
        String cleanedAmount = info;
        System.out.print("2- TYPE: ");

        if (info.contains(NEGATIVE)) {
            System.out.print(Type.RETURN);
            transactionModel.setType(Type.RETURN);

            if (description.contains("MB-CREDIT")) {
                transactionModel.setType(Type.CREDIT_CARD_PAYMENT);
                System.out.print(Type.CREDIT_CARD_PAYMENT);
            }
            cleanedAmount = cleanAmount(info, '-', EMPTY_STRING);

            if (cleanedAmount.contains(COMMA)) {
                cleanedAmount = cleanAmount(cleanedAmount, ',', EMPTY_STRING);
            }
        } else {
            System.out.print(Type.EXPENSE);
            transactionModel.setType(Type.EXPENSE);

            if (info.contains(COMMA)) {
                cleanedAmount = cleanAmount(info, ',', EMPTY_STRING);
            }
        }
        BigDecimal amount = new BigDecimal(cleanedAmount);
        System.out.println("\n3- AMOUNT: " + amount);
        transactionModel.setAmount(amount);
    }

    public static String cleanAmount(String info, char replace, String replacement) {
        return info.replace(Character.toString(replace), replacement);
    }
}
