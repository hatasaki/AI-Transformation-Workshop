package com.example.travelrequest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TravelRequest {

    private UUID id;

    @NotBlank
    private String title;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotBlank
    private String purpose;

    @NotBlank
    private String city;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal estimatedCost;

    public TravelRequest() {
    }

    public TravelRequest(UUID id, String title, LocalDate startDate, LocalDate endDate,
                         String purpose, String city, BigDecimal estimatedCost) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.purpose = purpose;
        this.city = city;
        this.estimatedCost = estimatedCost;
    }

    public static TravelRequest withGeneratedId(String title, LocalDate startDate, LocalDate endDate,
                                                String purpose, String city, BigDecimal estimatedCost) {
        return new TravelRequest(UUID.randomUUID(), title, startDate, endDate, purpose, city, estimatedCost);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
}
