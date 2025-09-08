package com.marcosoft.storageSoftware.infrastructure.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import java.util.Map;

@FunctionalInterface
public interface StyleApplier {
    void apply(Cell cell, String[] rowData, Map<String, CellStyle> styles);
}