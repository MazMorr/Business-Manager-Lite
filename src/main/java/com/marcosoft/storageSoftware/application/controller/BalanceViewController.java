package com.marcosoft.storageSoftware.application.controller;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ExpenseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Lazy
@Controller
public class BalanceViewController {
    private Client client;
    @Setter
    @Getter
    private LocalDate startDate;
    @Setter
    @Getter
    private LocalDate endDate;
    @Setter
    @Getter
    private Currency currency;
    private double totalExpense;
    private double totalProfit;

    private final SceneSwitcher sceneSwitcher;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final SellRegistryServiceImpl sellRegistryService;
    private final CurrencyServiceImpl currencyService;
    private final ExpenseServiceImpl expenseService;

    public BalanceViewController(
            DisplayAlerts displayAlerts, UserLogged userLogged, SceneSwitcher sceneSwitcher, ExpenseServiceImpl expenseService,
            SellRegistryServiceImpl sellRegistryService, CurrencyServiceImpl currencyService
    ) {
        this.sceneSwitcher = sceneSwitcher;
        this.currencyService = currencyService;
        this.expenseService = expenseService;
        this.sellRegistryService = sellRegistryService;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
    }

    @FXML
    private Label lblTimeLapse, lblTotalExpense, lblProductExpense, lblRentExpense, lblTotalProfit, lblServiceExpense,
            lblProductProfit, lblPublicityExpense, lblSalaryExpense, lblNetProfit, lblClientName;

    @FXML
    private MenuButton mbDateRange;

    public Label getLabelTimeLapse() {
        return lblTimeLapse;
    }

    @FXML
    private void initialize() {
        initDefaultValues();
        Platform.runLater(() -> {
            initMbDateRange();
            refreshBalance();
        });
    }

    private void initMbDateRange() {
        mbDateRange.getItems().clear();

        // Primer ítem (Establecer Fechas)
        MenuItem customDateItem = new MenuItem("Establecer Fechas");
        customDateItem.setOnAction(e -> {
            try {
                sceneSwitcher.displayWindow("Establecer Fechas", "/images/lc_logo.png", "/views/establishBalanceDateView.fxml");
            } catch (SceneSwitcher.WindowLoadException ex) {
                throw new RuntimeException(ex);
            }
        });
        mbDateRange.getItems().add(customDateItem);

        // Ítems de rangos predefinidos
        List<DateRangeOption> dateOptions = List.of(
                new DateRangeOption("Hoy", Period.ZERO),
                new DateRangeOption("Última Semana", Period.ofWeeks(1)),
                new DateRangeOption("Último Mes", Period.ofMonths(1)),
                new DateRangeOption("Último Trimestre", Period.ofMonths(3)),
                new DateRangeOption("Último Semestre", Period.ofMonths(6)),
                new DateRangeOption("Último Año", Period.ofYears(1))
        );

        dateOptions.forEach(option -> {
            MenuItem menuItem = new MenuItem(option.getLabel());
            menuItem.setOnAction(e -> setDateRange(option));
            mbDateRange.getItems().add(menuItem);
        });
    }

    private void setDateRange(DateRangeOption option) {
        setEndDate(LocalDate.now());
        setStartDate(endDate.minus(option.getPeriod()));
        lblTimeLapse.setText(startDate + " / " + endDate);
        refreshBalance();
    }

    // Clase de apoyo para encapsular la lógica de rangos de fecha
    @Getter
    @AllArgsConstructor
    private static class DateRangeOption {
        private final String label;
        private final Period period;

    }

    private void initDefaultValues() {
        startDate = LocalDate.now().minusMonths(1);
        endDate = LocalDate.now();
        currency = currencyService.getCurrencyByName("CUP");
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
        lblTimeLapse.setText(startDate + " / " + endDate);
    }

    public void refreshBalance() {
        initProfitLabels();
        initExpenseLabels();
        initNetProfit();

    }

    private void initExpenseLabels() {
        try {
            double rent = expenseService.getTotalRentExpense(client, startDate, endDate, currency);
            double salary = expenseService.getTotalSalaryExpense(client, startDate, endDate, currency);
            double publicity = expenseService.getTotalPublicityExpense(client, startDate, endDate, currency);
            double product = expenseService.getTotalProductExpense(client, startDate, endDate, currency);
            double service = expenseService.getTotalServiceExpense(client, startDate, endDate, currency);

            totalExpense = rent + salary + publicity + product + service;

            lblRentExpense.setText(String.format("$ %.2f", rent));
            lblSalaryExpense.setText(String.format("$ %.2f", salary));
            lblPublicityExpense.setText(String.format("$ %.2f", publicity));
            lblProductExpense.setText(String.format("$ %.2f", product));
            lblServiceExpense.setText(String.format("$ %.2f", service));
            lblTotalExpense.setText(String.format("$ %.2f", totalExpense));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    private void initProfitLabels() {
        try {
            double productProfit = sellRegistryService.getTotalProductProfit(client, startDate, endDate, currency);

            totalProfit = productProfit;

            lblProductProfit.setText(String.format("$ %.2f", productProfit));
            lblTotalProfit.setText(String.format("$ %.2f", totalProfit));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    private void initNetProfit() {
        double netProfit = totalProfit - totalExpense;
        String currencyName = currency.getCurrencyName();
        lblNetProfit.setText(netProfit + " " + currencyName);
        if (netProfit < 0) {
            lblNetProfit.setStyle("-fx-text-fill: #e40000");
        } else if (netProfit > 0) {
            lblNetProfit.setStyle("-fx-text-fill: #00ae03");
        }
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }

    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    @FXML
    public void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/expenseView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    @FXML
    public void exportToExcel() {
        try {
            // Crear libro de trabajo y hoja
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Balance");

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Datos para exportar
            String[][] data = {
                    {"Cliente:", client.getClientName()},
                    {"Rango de Fechas:", startDate + " - " + endDate},
                    {""},
                    {"GANANCIAS", ""},
                    {"Ganancia por Productos:", lblProductProfit.getText()},
                    {"Total de Ganancias:", lblTotalProfit.getText()},
                    {""},
                    {"GASTOS", ""},
                    {"Alquiler:", lblRentExpense.getText()},
                    {"Salarios:", lblSalaryExpense.getText()},
                    {"Publicidad:", lblPublicityExpense.getText()},
                    {"Productos:", lblProductExpense.getText()},
                    {"Servicios:", lblServiceExpense.getText()},
                    {"Total de Gastos:", lblTotalExpense.getText()},
                    {""},
                    {"GANANCIA NETA", lblNetProfit.getText()}
            };

            // Llenar la hoja con datos
            int rowNum = 0;
            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                for (String value : rowData) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(colNum++);
                    cell.setCellValue(value);

                    // Aplicar estilo a encabezados
                    if (value.equals("GANANCIAS") ||
                            value.equals("GASTOS") ||
                            value.equals("GANANCIA NETA")) {
                        cell.setCellStyle(headerStyle);
                    }
                }
            }

            // Autoajustar columnas
            for (int i = 0; i < 2; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte");
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
            workbook.close();
        } catch (Exception e) {
            displayAlerts.showError("Error al exportar: " + e.getMessage());
        }
    }

    @FXML
    public void exportToPdf() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte PDF");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file.getAbsolutePath()));
                Document document = new Document(pdfDoc);
                document.setMargins(50, 50, 50, 50);

                createPdfContent(document);

                document.close();
                displayAlerts.showAlert("Reporte PDF exportado correctamente");
            }
        } catch (Exception e) {
            displayAlerts.showError("Error al exportar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createPdfContent(Document document) {
        try {
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Título
            Paragraph title = new Paragraph("REPORTE DE BALANCE")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Información del cliente
            document.add(new Paragraph("Cliente: " + client.getClientName()).setFont(font));
            document.add(new Paragraph("Rango de Fechas: " + startDate + " - " + endDate).setFont(font));
            document.add(new Paragraph("\n"));

            // Tabla de ganancias
            document.add(new Paragraph("GANANCIAS").setFont(boldFont));
            Table profitsTable = new Table(2);
            profitsTable.addCell(new Cell().add(new Paragraph("Ganancia por Productos:").setFont(font)));
            profitsTable.addCell(new Cell().add(new Paragraph(lblProductProfit.getText()).setFont(font)));
            profitsTable.addCell(new Cell().add(new Paragraph("Total de Ganancias:").setFont(font)));
            profitsTable.addCell(new Cell().add(new Paragraph(lblTotalProfit.getText()).setFont(font)));
            document.add(profitsTable);
            document.add(new Paragraph("\n"));

            // Tabla de gastos
            document.add(new Paragraph("GASTOS").setFont(boldFont));
            Table expensesTable = new Table(2);
            expensesTable.addCell(new Cell().add(new Paragraph("Alquiler:").setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph(lblRentExpense.getText()).setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph("Salarios:").setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph(lblSalaryExpense.getText()).setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph("Publicidad:").setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph(lblPublicityExpense.getText()).setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph("Productos:").setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph(lblProductExpense.getText()).setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph("Servicios:").setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph(lblServiceExpense.getText()).setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph("Total de Gastos:").setFont(font)));
            expensesTable.addCell(new Cell().add(new Paragraph(lblTotalExpense.getText()).setFont(font)));
            document.add(expensesTable);
            document.add(new Paragraph("\n"));

            // Ganancia neta
            document.add(new Paragraph("GANANCIA NETA").setFont(boldFont));
            Table netProfitTable = new Table(2);
            netProfitTable.addCell(new Cell().add(new Paragraph("Total:").setFont(font)));
            netProfitTable.addCell(new Cell().add(new Paragraph(lblNetProfit.getText()).setFont(font)));
            document.add(netProfitTable);

        } catch (IOException e) {
            displayAlerts.showError("Error al crear el PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void displayCurrencyValues() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Valor de Monedas", "/images/lc_logo.png", "/views/currencyValuesView.fxml");
    }
}
