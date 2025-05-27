package com.cibertec.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.cibertec.models.Transacciones;

@Service
public class ExportarExcelService {
	private Workbook workbook;

	public ByteArrayOutputStream exportTransaccionesToExcel(List<Transacciones> transacciones) throws IOException {
		workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Transacciones");

		// Estilos
		CellStyle titleStyle = createTitleStyle();
		CellStyle headerStyle = createHeaderStyle();
		CellStyle dateStyle = createDateStyle();
		CellStyle moneyStyle = createMoneyStyle();
		CellStyle evenRowStyle = createEvenRowStyle();
		CellStyle oddRowStyle = createOddRowStyle();
		CellStyle totalStyle = createTotalStyle();

		// Título
		createTitle(sheet, titleStyle);

		// Cabeceras
		createHeaders(sheet, headerStyle);

		// Datos y formato condicional
		int rowNum = fillData(sheet, transacciones, dateStyle, moneyStyle, evenRowStyle, oddRowStyle);

		// Totales
		addTotals(sheet, rowNum, totalStyle, moneyStyle);

		// Pie de página
		addFooter(sheet, rowNum + 6);

		// Ajustes finales
		adjustColumnWidths(sheet);
		sheet.setAutoFilter(new CellRangeAddress(1, rowNum - 1, 0, 3));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();
		return outputStream;
	}

	private CellStyle createTitleStyle() {
		Font titleFont = workbook.createFont();
		titleFont.setFontName("Calibri");
		titleFont.setFontHeightInPoints((short) 18);
		titleFont.setBold(true);
		titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());

		CellStyle titleStyle = workbook.createCellStyle();
		titleStyle.setFont(titleFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
		titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return titleStyle;
	}

	private CellStyle createHeaderStyle() {
		Font headerFont = workbook.createFont();
		headerFont.setFontName("Calibri");
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		return headerStyle;
	}

	private CellStyle createDateStyle() {
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy"));
		return dateStyle;
	}

	private CellStyle createMoneyStyle() {
		CellStyle moneyStyle = workbook.createCellStyle();
		moneyStyle.setDataFormat((short) 8); // Built-in accounting format
		return moneyStyle;
	}

	private CellStyle createEvenRowStyle() {
		CellStyle evenRowStyle = workbook.createCellStyle();
		evenRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		evenRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return evenRowStyle;
	}

	private CellStyle createOddRowStyle() {
		return workbook.createCellStyle(); // Estilo por defecto para filas impares
	}

	private CellStyle createTotalStyle() {
		Font totalFont = workbook.createFont();
		totalFont.setFontName("Calibri");
		totalFont.setFontHeightInPoints((short) 12);
		totalFont.setBold(true);
		totalFont.setColor(IndexedColors.WHITE.getIndex());

		CellStyle totalStyle = workbook.createCellStyle();
		totalStyle.setFont(totalFont);
		totalStyle.setAlignment(HorizontalAlignment.RIGHT);
		totalStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		totalStyle.setBorderTop(BorderStyle.THIN);
		totalStyle.setBorderBottom(BorderStyle.THIN);
		totalStyle.setBorderLeft(BorderStyle.THIN);
		totalStyle.setBorderRight(BorderStyle.THIN);
		return totalStyle;
	}

	private void createTitle(Sheet sheet, CellStyle titleStyle) {
		Row titleRow = sheet.createRow(0);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("CoinTrack - Reporte de Transacciones");
		titleCell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
	}

	private void createHeaders(Sheet sheet, CellStyle headerStyle) {
		Row headerRow = sheet.createRow(1);
		String[] headers = { "Descripción", "Monto", "Tipo", "Fecha" };
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}
	}

	private int fillData(Sheet sheet, List<Transacciones> transacciones, CellStyle dateStyle, CellStyle moneyStyle,
			CellStyle evenRowStyle, CellStyle oddRowStyle) {
		int rowNum = 2;
		for (Transacciones transaccion : transacciones) {
			Row row = sheet.createRow(rowNum++);

			Cell descCell = row.createCell(0);
			descCell.setCellValue(transaccion.getDescripcion());

			Cell montoCell = row.createCell(1);
			montoCell.setCellValue(transaccion.getMonto().doubleValue());
			montoCell.setCellStyle(moneyStyle);

			Cell tipoCell = row.createCell(2);
			tipoCell.setCellValue(transaccion.getTipo().toString());
			tipoCell.setCellStyle(dateStyle);

			Cell fechaCell = row.createCell(3);
			fechaCell.setCellValue(transaccion.getFecha());
			fechaCell.setCellStyle(dateStyle);

			// Aplicar estilo alternado de filas
			CellStyle rowStyle = (rowNum % 2 == 0) ? evenRowStyle : oddRowStyle;
			descCell.setCellStyle(rowStyle);
			montoCell.setCellStyle(createCombinedStyle(moneyStyle, rowStyle));
			fechaCell.setCellStyle(createCombinedStyle(dateStyle, rowStyle));

			// Formato condicional para ingresos y gastos
			CellStyle ingresoStyle = workbook.createCellStyle();
			ingresoStyle.cloneStyleFrom(moneyStyle);
			ingresoStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			ingresoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle gastoStyle = workbook.createCellStyle();
			gastoStyle.cloneStyleFrom(moneyStyle);
			gastoStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
			gastoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			if (transaccion.getTipo().toString().equals("INGRESO")) {
				montoCell.setCellStyle(ingresoStyle);
			} else {
				montoCell.setCellStyle(gastoStyle);
			}
		}
		return rowNum;
	}

	private void addTotals(Sheet sheet, int rowNum, CellStyle totalStyle, CellStyle moneyStyle) {
		rowNum += 2;

		Row totalIngresosRow = sheet.createRow(rowNum++);
		totalIngresosRow.createCell(0).setCellValue("Total Ingresos");
		totalIngresosRow.getCell(0).setCellStyle(totalStyle);
		Cell totalIngresosCell = totalIngresosRow.createCell(1);
		totalIngresosCell.setCellFormula("SUMIF(C3:C" + (rowNum - 1) + ",\"INGRESO\",B3:B" + (rowNum - 1) + ")");
		totalIngresosCell.setCellStyle(moneyStyle);

		Row totalGastosRow = sheet.createRow(rowNum++);
		totalGastosRow.createCell(0).setCellValue("Total Gastos");
		totalGastosRow.getCell(0).setCellStyle(totalStyle);
		Cell totalGastosCell = totalGastosRow.createCell(1);
		totalGastosCell.setCellFormula("SUMIF(C3:C" + (rowNum - 2) + ",\"GASTO\",B3:B" + (rowNum - 2) + ")");
		totalGastosCell.setCellStyle(moneyStyle);

		Row balanceRow = sheet.createRow(rowNum++);
		balanceRow.createCell(0).setCellValue("Saldo");
		balanceRow.getCell(0).setCellStyle(totalStyle);
		Cell balanceCell = balanceRow.createCell(1);
		balanceCell.setCellFormula("B" + (rowNum - 2) + "-B" + (rowNum - 1));
		balanceCell.setCellStyle(moneyStyle);
	}

	private void addFooter(Sheet sheet, int rowNum) {
		Row footerRow = sheet.createRow(rowNum);
		Cell footerCell = footerRow.createCell(0);
		footerCell.setCellValue("Reporte generado el "
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

		CellStyle footerStyle = workbook.createCellStyle();
		Font footerFont = workbook.createFont();
		footerFont.setFontName("Calibri");
		footerFont.setFontHeightInPoints((short) 10);
		footerFont.setItalic(true);
		footerStyle.setFont(footerFont);
		footerCell.setCellStyle(footerStyle);

		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));
	}

	private void adjustColumnWidths(Sheet sheet) {
		sheet.setColumnWidth(0, 8000); // Columna descripción
		sheet.setColumnWidth(1, 6000); // Columna monto
		sheet.setColumnWidth(2, 4000); // Columna tipo
		sheet.setColumnWidth(3, 4000); // Columna fecha
	}

	private CellStyle createCombinedStyle(CellStyle style1, CellStyle style2) {
		CellStyle combinedStyle = workbook.createCellStyle();
		combinedStyle.cloneStyleFrom(style1);
		combinedStyle.setFillForegroundColor(style2.getFillForegroundColor());
		combinedStyle.setFillPattern(style2.getFillPattern());
		return combinedStyle;
	}
}