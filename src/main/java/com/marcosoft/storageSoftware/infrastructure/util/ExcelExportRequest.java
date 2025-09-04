package com.marcosoft.storageSoftware.infrastructure.util;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public record ExcelExportRequest(List<SheetConfig> sheets, String defaultFileName) {

}

