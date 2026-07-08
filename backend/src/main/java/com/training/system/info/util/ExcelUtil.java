package com.training.system.info.util;

import com.training.system.info.annotation.ExcelColumn;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Excel导入导出工具类
 */
public class ExcelUtil {

    public static <T> List<T> readXlsx(MultipartFile file, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<>();
        Map<Integer, Field> fieldMap = getFieldMap(clazz);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                T obj = clazz.getDeclaredConstructor().newInstance();
                boolean hasData = false;

                for (Map.Entry<Integer, Field> entry : fieldMap.entrySet()) {
                    Cell cell = row.getCell(entry.getKey());
                    if (cell != null) {
                        Field field = entry.getValue();
                        field.setAccessible(true);
                        setFieldValue(obj, field, getCellValue(cell));
                        hasData = true;
                    }
                }
                if (hasData) list.add(obj);
            }
        }
        return list;
    }

    public static <T> void writeXlsx(List<T> data, Class<T> clazz, OutputStream outputStream, String sheetName) throws Exception {
        Map<Integer, Field> fieldMap = getFieldMap(clazz);
        TreeMap<Integer, Field> sortedFields = new TreeMap<>(fieldMap);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Sheet1");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            int col = 0;
            for (Map.Entry<Integer, Field> entry : sortedFields.entrySet()) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(entry.getValue().getAnnotation(ExcelColumn.class).header());
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowIdx++);
                col = 0;
                for (Field field : sortedFields.values()) {
                    field.setAccessible(true);
                    Object value = field.get(item);
                    Cell cell = row.createCell(col++);
                    if (value != null) cell.setCellValue(value.toString());
                }
            }
            workbook.write(outputStream);
        }
    }

    private static <T> Map<Integer, Field> getFieldMap(Class<T> clazz) {
        Map<Integer, Field> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) map.put(annotation.index(), field);
        }
        return map;
    }

    private static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) return String.valueOf((long) val);
                return String.valueOf(val);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private static void setFieldValue(Object obj, Field field, String value) {
        try {
            if (field.getType() == String.class) {
                field.set(obj, value);
            } else if (field.getType() == Integer.class || field.getType() == int.class) {
                try { field.set(obj, Integer.parseInt(value)); } catch (NumberFormatException e) { field.set(obj, 0); }
            }
        } catch (IllegalAccessException ignored) {}
    }
}
