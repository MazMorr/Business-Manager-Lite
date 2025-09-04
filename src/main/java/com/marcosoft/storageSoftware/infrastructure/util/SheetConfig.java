package com.marcosoft.storageSoftware.infrastructure.util;

import lombok.Getter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SheetConfig {
    @Getter
    private String sheetName;
    @Getter
    private List<String[]> data;
    @Getter
    private int columnCount;
    @Getter
    private StyleApplier styleApplier; // Cambiado a la nueva interfaz
    private final StylesCreator stylesCreator;

    public interface StylesCreator {
        Map<String, CellStyle> createStyles(Workbook workbook);
    }

    public SheetConfig(String sheetName, List<String[]> data, int columnCount,
                       StyleApplier styleApplier, StylesCreator stylesCreator) {
        this.sheetName = sheetName;
        this.data = data;
        this.columnCount = columnCount;
        this.styleApplier = styleApplier;
        this.stylesCreator = stylesCreator;
    }

    public Map<String, CellStyle> createStyles(Workbook workbook) {
        return stylesCreator != null ? stylesCreator.createStyles(workbook) : null;
    }
}