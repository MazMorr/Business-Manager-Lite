package com.marcosoft.storageSoftware.infrastructure.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class PdfGenerator {
    private final DisplayAlerts displayAlerts;

    public PdfGenerator(DisplayAlerts displayAlerts) {
        this.displayAlerts = displayAlerts;
    }

    public void exportToPdf(PdfExportRequest request) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte PDF");
            fileChooser.setInitialFileName(request.getFileName());
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file.getAbsolutePath()));
                Document document = new Document(pdfDoc);
                document.setMargins(50, 50, 50, 50);

                createPdfContent(document, request);

                document.close();
                displayAlerts.showAlert("Reporte PDF exportado correctamente");
            }
        } catch (Exception e) {
            displayAlerts.showError("Error al exportar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createPdfContent(Document document, PdfExportRequest request) {
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

            // Información del cliente y fechas
            document.add(new Paragraph("Cliente: " + request.getClientName()).setFont(font));
            document.add(new Paragraph("Rango de Fechas: " + request.getStartDate() + " - " + request.getEndDate()).setFont(font));
            document.add(new Paragraph("\n"));

            // Resumen (similar a la hoja de Excel)
            document.add(new Paragraph("RESUMEN").setFont(boldFont).setFontSize(16));
            addSummaryTable(document, request.getSummaryData(), font, boldFont);
            document.add(new Paragraph("\n"));

            // Ventas
            document.add(new Paragraph("DETALLES DE VENTAS").setFont(boldFont).setFontSize(16));
            addDataTable(document, request.getSalesData(), font, boldFont);
            document.add(new Paragraph("\n"));

            // Gastos
            document.add(new Paragraph("DETALLES DE GASTOS").setFont(boldFont).setFontSize(16));
            addDataTable(document, request.getExpensesData(), font, boldFont);

        } catch (IOException e) {
            displayAlerts.showError("Error al crear el PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSummaryTable(Document document, List<String[]> data, PdfFont font, PdfFont boldFont) {
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));

        for (String[] row : data) {
            // Para filas que son encabezados principales (GANANCIAS, GASTOS, GANANCIA NETA)
            if (row[0].equals("GANANCIAS") || row[0].equals("GASTOS") || row[0].equals("GANANCIA NETA")) {
                Cell headerCell = new Cell(1, 2) // Esta celda abarcará 1 fila y 2 columnas
                        .add(new Paragraph(row[0]))
                        .setFont(boldFont)
                        .setBackgroundColor(new DeviceRgb(220, 220, 220))
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(headerCell);
            }
            // Para filas vacías (separadores)
            else if (row[0].isEmpty() && (row.length == 1 || row[1].isEmpty())) {
                Cell emptyCell = new Cell(1, 2) // Celda que abarca 2 columnas
                        .add(new Paragraph(""))
                        .setHeight(10); // Altura mínima para el separador
                table.addCell(emptyCell);
            }
            // Para filas normales con dos columnas
            else if (row.length == 2) {
                for (int i = 0; i < row.length; i++) {
                    Cell cell = new Cell().add(new Paragraph(row[i] != null ? row[i] : ""));

                    // Aplicar estilo a la primera columna (etiquetas)
                    if (i == 0) {
                        cell.setFont(boldFont);
                    } else {
                        cell.setFont(font);
                    }

                    table.addCell(cell);
                }
            }
        }

        document.add(table);
    }

    private void addDataTable(Document document, List<String[]> data, PdfFont font, PdfFont boldFont) {
        if (data.isEmpty()) {
            document.add(new Paragraph("No hay datos disponibles").setFont(font));
            return;
        }

        // Crear tabla con el número correcto de columnas
        Table table = new Table(data.getFirst().length);
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        for (String header : data.getFirst()) {
            table.addHeaderCell(
                    new Cell()
                            .add(new Paragraph(header))
                            .setFont(boldFont)
                            .setBackgroundColor(new DeviceRgb(200, 200, 200))
                            .setTextAlignment(TextAlignment.CENTER)
            );
        }

        // Datos
        for (int i = 1; i < data.size(); i++) {
            for (String value : data.get(i)) {
                table.addCell(
                        new Cell()
                                .add(new Paragraph(value != null ? value : ""))
                                .setFont(font)
                                .setTextAlignment(TextAlignment.LEFT)
                );
            }
        }

        document.add(table);
    }
}