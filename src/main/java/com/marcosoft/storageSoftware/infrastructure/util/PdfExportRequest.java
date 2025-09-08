package com.marcosoft.storageSoftware.infrastructure.util;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class PdfExportRequest {
    // Getters
    private final String clientName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<String[]> summaryData;
    private final List<String[]> salesData;
    private final List<String[]> expensesData;
    private final String fileName;

    public PdfExportRequest(String clientName, LocalDate startDate, LocalDate endDate,
                            List<String[]> summaryData, List<String[]> salesData,
                            List<String[]> expensesData, String fileName) {
        this.clientName = clientName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.summaryData = summaryData;
        this.salesData = salesData;
        this.expensesData = expensesData;
        this.fileName = fileName;
    }

}