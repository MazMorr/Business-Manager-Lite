package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@Component
public class ExcelGenerator {
    private final DisplayAlerts displayAlerts;

    public ExcelGenerator(DisplayAlerts displayAlerts) {
        this.displayAlerts = displayAlerts;
    }

    public void exportToExcel(ExcelExportRequest request) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Crear todas las hojas especificadas
            for (SheetConfig sheetConfig : request.sheets()) {
                Sheet sheet = workbook.createSheet(sheetConfig.getSheetName());
                Map<String, CellStyle> styles = sheetConfig.createStyles(workbook);
                writeDataToSheet(sheet, sheetConfig.getData(), styles, sheetConfig.getStyleApplier());
                autoSizeColumns(sheet, sheetConfig.getColumnCount());
            }

            // Guardar archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte");
            fileChooser.setInitialFileName(request.defaultFileName());
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }
                displayAlerts.showAlert("Reporte exportado correctamente");
            }
        } catch (Exception e) {
            displayAlerts.showError("Error al exportar: " + e.getMessage());
        }
    }

    private void writeDataToSheet(Sheet sheet,
                                  List<String[]> data,
                                  Map<String, CellStyle> styles,
                                  StyleApplier styleApplier) { // Cambiado el parámetro
        int rowNum = 0;
        for (String[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String value : rowData) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(value);
                if (styleApplier != null) {
                    styleApplier.apply(cell, rowData, styles); // Pasa styles como parámetro
                }
            }
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}