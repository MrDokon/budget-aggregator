package com.project.dokon.budgetaggregator.importing.api.dto;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionCsvDto implements TransactionSourceDto {

    @NotBlank(message = "Transaction ID is required")
    @CsvBindByName(column = "transactionId")
    private String transactionId;

    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "^[A-Z]{2}\\d{2,30}$", message = "Invalid IBAN format")
    @CsvBindByName(column = "iban")
    private String iban;

    @NotNull(message = "Amount is required")
    @CsvBindByName(column = "amount")
    private String amount;

    @NotBlank(message = "Currency is required")
    @CsvBindByName(column = "currency")
    private String currency;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in format yyyy-MM-dd")
    @CsvBindByName(column = "date")
    private String date;

    @CsvBindByName(column = "category")
    private String category;

    @Override
    public BigDecimal getParsedAmount() {
        return new BigDecimal(this.amount);
    }

    @Override
    public LocalDate getParsedDate() {
        return LocalDate.parse(this.date);
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}