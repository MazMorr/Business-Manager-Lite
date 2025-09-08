package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Expense;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ExpenseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Lazy
@Controller
public class BalanceViewController {
    private Client client;
    @Setter
    @Getter
    private static LocalDate startDate;
    @Setter
    @Getter
    private static LocalDate endDate;
    @Setter
    @Getter
    private Currency currency;
    private double totalExpense;
    private double totalProfit;

    private final SceneSwitcher sceneSwitcher;
    private final UserLogged userLogged;
    private final SellRegistryServiceImpl sellRegistryService;
    private final CurrencyServiceImpl currencyService;
    private final ExpenseServiceImpl expenseService;
    private final PdfGenerator pdfGenerator;
    private final ExcelGenerator excelGenerator;
    private final DisplayAlerts displayAlerts;

    public BalanceViewController(
            UserLogged userLogged, SceneSwitcher sceneSwitcher, ExpenseServiceImpl expenseService,
            SellRegistryServiceImpl sellRegistryService, CurrencyServiceImpl currencyService, PdfGenerator pdfGenerator, ExcelGenerator excelGenerator, DisplayAlerts displayAlerts
    ) {
        this.sceneSwitcher = sceneSwitcher;
        this.currencyService = currencyService;
        this.expenseService = expenseService;
        this.sellRegistryService = sellRegistryService;
        this.userLogged = userLogged;
        this.pdfGenerator = pdfGenerator;
        this.excelGenerator = excelGenerator;
        this.displayAlerts = displayAlerts;
    }

    @FXML
    private Label lblTimeLapse, lblTotalExpense, lblProductExpense, lblRentExpense, lblTotalProfit, lblServiceExpense,
            lblProductProfit, lblPublicityExpense, lblSalaryExpense, lblNetUtilityNumber, lblClientName, lblNetUtility;

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

        // Separador
        mbDateRange.getItems().add(new SeparatorMenuItem());

        // Rangos predefinidos
        List<DateRangeOption> dateOptions = List.of(
                new DateRangeOption("Hoy", DateRangeType.DAILY, 0),
                new DateRangeOption("Ayer", DateRangeType.DAILY, 1),

                new DateRangeOption("Esta Semana (L-D)", DateRangeType.WEEKLY, 0),
                new DateRangeOption("Semana Anterior", DateRangeType.WEEKLY, 1),

                new DateRangeOption("Mes en Curso", DateRangeType.MONTHLY, 0),
                new DateRangeOption("Mes Anterior", DateRangeType.MONTHLY, 1),

                new DateRangeOption("Trimestre en Curso", DateRangeType.QUARTERLY, 0),
                new DateRangeOption("Trimestre Anterior", DateRangeType.QUARTERLY, 1),

                new DateRangeOption("Semestre en Curso", DateRangeType.SEMESTERLY, 0),
                new DateRangeOption("Semestre Anterior", DateRangeType.SEMESTERLY, 1),

                new DateRangeOption("Año en Curso", DateRangeType.YEARLY, 0),
                new DateRangeOption("Año Anterior", DateRangeType.YEARLY, 1)
        );

        dateOptions.forEach(option -> {
            MenuItem menuItem = new MenuItem(option.getLabel());
            menuItem.setOnAction(e -> setDateRange(option));
            mbDateRange.getItems().add(menuItem);
        });
    }

    private void setDateRange(DateRangeOption option) {
        LocalDate[] dateRange = calculateDateRange(option);
        setStartDate(dateRange[0]);
        setEndDate(dateRange[1]);
        lblTimeLapse.setText(startDate + " / " + endDate);
        refreshBalance();
    }

    private LocalDate[] calculateDateRange(DateRangeOption option) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        switch (option.getType()) {
            case DAILY:
                startDate = today.minusDays(option.getOffset());
                endDate = startDate;
                break;

            case WEEKLY:
                // Calcular lunes de la semana
                LocalDate baseDate = today.minusWeeks(option.getOffset());
                startDate = baseDate.with(DayOfWeek.MONDAY);
                endDate = startDate.plusDays(6);
                break;

            case MONTHLY:
                startDate = today.minusMonths(option.getOffset()).withDayOfMonth(1);
                endDate = startDate.plusMonths(1).minusDays(1);
                break;

            case QUARTERLY:
                int currentQuarter = (today.getMonthValue() - 1) / 3;
                int targetQuarter = (currentQuarter - option.getOffset() + 4) % 4;
                int startMonth = targetQuarter * 3 + 1;
                startDate = LocalDate.of(today.getYear(), startMonth, 1).minusMonths(option.getOffset() * 3L);
                endDate = startDate.plusMonths(3).minusDays(1);
                break;

            case SEMESTERLY:
                int currentSemester = (today.getMonthValue() - 1) / 6;
                int targetSemester = (currentSemester - option.getOffset() + 2) % 2;
                int semesterStartMonth = targetSemester * 6 + 1;
                startDate = LocalDate.of(today.getYear(), semesterStartMonth, 1).minusMonths(option.getOffset() * 6L);
                endDate = startDate.plusMonths(6).minusDays(1);
                break;

            case YEARLY:
                startDate = LocalDate.of(today.getYear() - option.getOffset(), 1, 1);
                endDate = LocalDate.of(today.getYear() - option.getOffset(), 12, 31);
                break;

            default:
                startDate = today.minusMonths(1);
                endDate = today;
        }

        return new LocalDate[]{startDate, endDate};
    }

    // Nuevas clases de apoyo
    @Getter
    private static class DateRangeOption {
        private final String label;
        private final DateRangeType type;
        private final int offset;

        public DateRangeOption(String label, DateRangeType type, int offset) {
            this.label = label;
            this.type = type;
            this.offset = offset;
        }
    }

    private enum DateRangeType {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, SEMESTERLY, YEARLY
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
        lblNetUtilityNumber.setText(netProfit + " " + currencyName);
        if (netProfit < 0) {
            lblNetUtilityNumber.setStyle("-fx-text-fill: #e40000");
            lblNetUtility.setText("Pérdidas");
        } else if (netProfit > 0) {
            lblNetUtilityNumber.setStyle("-fx-text-fill: #00ae03");
            lblNetUtility.setText("Utilidad Neta");
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
    private void exportToExcel() {
        // Preparar los datos para las hojas
        List<String[]> summaryData = getSummaryDataForXls();
        List<String[]> salesData = getSalesData();
        List<String[]> expensesData = getExpensesData();

        // Definir estilos para el resumen
        ExcelExportRequest request = getExcelExportRequest(summaryData, salesData, expensesData);

        // Llamar al generador
        excelGenerator.exportToExcel(request);
    }

    private static ExcelExportRequest getExcelExportRequest(List<String[]> summaryData, List<String[]> salesData, List<String[]> expensesData) {
        SheetConfig summarySheet = getSheetConfig(summaryData);
        SheetConfig salesSheet = new SheetConfig("Ventas", salesData, 5, null, null);
        SheetConfig expensesSheet = new SheetConfig("Gastos", expensesData, 4, null, null);

        List<SheetConfig> sheets = List.of(summarySheet, salesSheet, expensesSheet);

        // Crear el request
        String fileName = "Reporte_" + startDate + "_a_" + endDate + ".xlsx";
        return new ExcelExportRequest(sheets, fileName);
    }

    private static SheetConfig getSheetConfig(List<String[]> summaryData) {
        SheetConfig.StylesCreator summaryStyles = workbook -> {
            Map<String, CellStyle> styles = new HashMap<>();
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            styles.put("header", headerStyle);
            return styles;
        };

        // Usa la nueva interfaz StyleApplier
        StyleApplier summaryStyleApplier = (cell, rowData, styles) -> {
            if (rowData.length > 0 &&
                    (rowData[0].equals("GANANCIAS") ||
                            rowData[0].equals("GASTOS") ||
                            rowData[0].equals("GANANCIA NETA"))) {
                cell.setCellStyle(styles.get("header"));
            }
        };

        return new SheetConfig("Resumen", summaryData, 2, summaryStyleApplier, summaryStyles);
    }

    private List<String[]> getExpensesData() {
        // Obtener los gastos del servicio
        List<Expense> expenses = expenseService.getExpensesInDateRange(client, startDate, endDate);
        List<String[]> expensesData = new ArrayList<>();
        // Encabezados
        expensesData.add(new String[]{"Fecha", "Tipo", "Precio", "Monto"});
        for (Expense expense : expenses) {
            expensesData.add(new String[]{
                    expense.getReceivedDate().toString(),
                    expense.getExpenseType(),
                    expense.getExpensePrice() + " " + expense.getCurrency().getCurrencyName(),
                    String.valueOf(expense.getAmount())
            });
        }
        return expensesData;
    }

    private List<String[]> getSalesData() {
        // Obtener las ventas del servicio
        List<SellRegistry> sales = sellRegistryService.getSalesInDateRange(client, startDate, endDate);
        List<String[]> salesData = new ArrayList<>();
        // Encabezados
        salesData.add(new String[]{"Fecha", "Producto", "Cantidad", "Precio"});
        for (SellRegistry sale : sales) {
            salesData.add(new String[]{
                    sale.getSellDate().toString(),
                    sale.getProductName(),
                    String.valueOf(sale.getProductAmount()),
                    sale.getSellPrice() + " " + sale.getSellCurrency()
            });
        }
        return salesData;
    }

    private List<String[]> getSummaryDataForXls() {
        return List.of(
                new String[]{"Cliente:", client.getClientName()},
                new String[]{"Rango de Fechas:", startDate + " - " + endDate},
                new String[]{"", ""}, // Fila vacía como separador
                new String[]{"GANANCIAS", ""}, // Encabezado principal
                new String[]{"Ganancia por Productos:", lblProductProfit.getText()},
                new String[]{"Total de Ganancias:", lblTotalProfit.getText()},
                new String[]{"", ""}, // Fila vacía como separador
                new String[]{"GASTOS", ""}, // Encabezado principal
                new String[]{"Alquiler:", lblRentExpense.getText()},
                new String[]{"Salarios:", lblSalaryExpense.getText()},
                new String[]{"Publicidad:", lblPublicityExpense.getText()},
                new String[]{"Productos:", lblProductExpense.getText()},
                new String[]{"Servicios:", lblServiceExpense.getText()},
                new String[]{"Total de Gastos:", lblTotalExpense.getText()},
                new String[]{"", ""}, // Fila vacía como separador
                new String[]{"GANANCIA NETA", lblNetUtilityNumber.getText()} // Encabezado principal
        );
    }
    private List<String[]> getSummaryDataForPDF() {
        return List.of(
                new String[]{"GANANCIAS", ""}, // Encabezado principal
                new String[]{"Ganancia por Productos:", lblProductProfit.getText()},
                new String[]{"Total de Ganancias:", lblTotalProfit.getText()},
                new String[]{"GASTOS", ""}, // Encabezado principal
                new String[]{"Alquiler:", lblRentExpense.getText()},
                new String[]{"Salarios:", lblSalaryExpense.getText()},
                new String[]{"Publicidad:", lblPublicityExpense.getText()},
                new String[]{"Productos:", lblProductExpense.getText()},
                new String[]{"Servicios:", lblServiceExpense.getText()},
                new String[]{"Total de Gastos:", lblTotalExpense.getText()},
                new String[]{"GANANCIA NETA", ""},
                new String[]{"Ganancia Neta:", lblNetUtilityNumber.getText()}
        );
    }

    @FXML
    public void exportToPdf() {
        try {
            // Preparar los datos (los mismos que para Excel)
            List<String[]> summaryData = getSummaryDataForPDF();
            PdfExportRequest request = getPdfExportRequest(summaryData);

            // Llamar al generador de PDF
            pdfGenerator.exportToPdf(request);
        } catch (Exception e) {
            displayAlerts.showError("Error al preparar datos para PDF: " + e.getMessage());
        }
    }

    private PdfExportRequest getPdfExportRequest(List<String[]> summaryData) {
        List<String[]> salesData = getSalesData();
        List<String[]> expensesData = getExpensesData();

        // Crear request para PDF
        String fileName = "Reporte_" + startDate + "_a_" + endDate + ".pdf";
        return new PdfExportRequest(
                client.getClientName(),
                startDate,
                endDate,
                summaryData,
                salesData,
                expensesData,
                fileName
        );
    }


    @FXML
    public void displayCurrencyValues() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Valor de Monedas", "/images/lc_logo.png", "/views/currencyValuesView.fxml");
    }
}
