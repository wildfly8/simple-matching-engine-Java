import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class ExcelCache {

	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private HSSFRow row;
	private HSSFCell cell;
	private int rowindex;          
	private int columnIndex;	  
	private int sheetNum; 	


	//Absolute path as parameter "fileName"
	public synchronized HSSFWorkbook loadExcelFile(String fileName) throws Exception {
		try(FileInputStream fis = new FileInputStream(fileName)) {
			return new HSSFWorkbook(new POIFSFileSystem(fis));			
		}
	}

	public synchronized void writeWorkbookWithCurrentSheet(String fileName) throws Exception {
		//protect the current sheet
		sheet.protectSheet("forecast602060a");
		//auto column size of the current sheet, it's slow on large sheet, 
		//so this should normally only be called once per column, at the end of your processing.
		short minColIx = sheet.getRow(0).getFirstCellNum();
		short maxColIx = sheet.getRow(0).getLastCellNum();
		for(short colIx=minColIx; colIx<maxColIx; colIx++) {
			sheet.autoSizeColumn(colIx);
		}
		//write this workbook with the current sheet to file system via FileOutputStream
		try(FileOutputStream fos = new FileOutputStream(fileName)) {
			wb.write(fos);	        	
		}
		System.out.println("Saved to " + fileName);
	}

	public synchronized void writeWorkbookWithCurrentSheetWithoutPassword(String fileName) throws Exception {
		//auto column size of the current sheet, it's slow on large sheet, 
		//so this should normally only be called once per column, at the end of your processing.
		short minColIx = sheet.getRow(0).getFirstCellNum();
		short maxColIx = sheet.getRow(0).getLastCellNum();
		for(short colIx=minColIx; colIx<maxColIx; colIx++) {
			sheet.autoSizeColumn(colIx);
		}
		//write this workbook with the current sheet to file system via FileOutputStream
		try(FileOutputStream fos = new FileOutputStream(fileName)) {
			wb.write(fos);
		}
		System.out.println("Saved to " + fileName);
	}

	public void setWb(HSSFWorkbook wb) {
		this.wb = wb;
	}

	public void setSheet(HSSFSheet sheet) {
		this.sheet = sheet;
	}

	public void setRowindex(int rowindex) {
		this.rowindex = rowindex;
	}

	public void setSheetNum(int sheetNum) {
		this.sheetNum = sheetNum;
	}

	public List<List<Object>> parseColumnsWithNamesAs(HSSFSheet hssfsheet, List<String> columnNames) throws Exception {
		HSSFRow row;
		HSSFCell cell;
		List<List<Object>> results = new ArrayList<List<Object>>();
		List<Integer> columnsToBeParsed = new ArrayList<Integer>();		
		//0 should be always the header row
		int firstRow = 0; 	
		row = hssfsheet.getRow(firstRow);		
		short minColIx = row.getFirstCellNum();
		short maxColIx = row.getLastCellNum();		
		for(short colIx=minColIx; colIx<maxColIx; colIx++) {
			cell = row.getCell((new Short(colIx)).intValue());
			if(cell == null)
				continue;
			if(columnNames.contains(cell.getStringCellValue().trim()))
				columnsToBeParsed.add(cell.getColumnIndex());
		}		
		for(Integer colIndex : columnsToBeParsed) {
			results.add(parseColumnAt(hssfsheet, colIndex));
		}
		return results;
	}

	public List<Object> parsePartialColumnFrom(HSSFSheet hssfsheet, int columnIdx, int rowIdx) throws Exception {
		int lastRow = hssfsheet.getLastRowNum();
		List<Object> results = new ArrayList<Object>();		
		for(int i=rowIdx; i<=lastRow; i++) {
			results.add(parseCellAt(hssfsheet, columnIdx, i));
		}	
		return results;
	}

	public Object parseCellAt(HSSFSheet hssfsheet, int columnIdx, int rowIdx) { 
		Object result = null;		
		try {
			HSSFRow row = hssfsheet.getRow(rowIdx);
			HSSFCell cell = row.getCell(columnIdx);     
			if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {       
				try {
					result = cell.getStringCellValue().trim();
				} catch(Exception e) {
					result = "";
				}
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC && columnIdx == 0) {        
				try {
					result = cell.getDateCellValue();
				} catch(Exception e) {
					result = new Date(0);
				}
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC && columnIdx != 0) {        
				try {
					result = cell.getNumericCellValue();
				} catch(Exception e) {
					result = 0.0;
				}
			}
		} catch (Exception e) {
			return result;
		}           
		return result;
	}

	public String parseCellAtAsString(HSSFSheet hssfsheet, int columnIdx, int rowIdx) { 
		String result = null;		
		try {
			HSSFRow row = hssfsheet.getRow(rowIdx);
			HSSFCell cell = row.getCell(columnIdx);     
			if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {       
				try {
					result = cell.getStringCellValue().trim();
				} catch(Exception e) {
					return null;
				}
			} else {
				return null;
			}
		} catch(Exception e) {
			return null;
		}           
		return result;
	}

	public Date parseCellAtAsDate(HSSFSheet hssfsheet, int columnIdx, int rowIdx) { 
		Date result = null;		
		try {
			HSSFRow row = hssfsheet.getRow(rowIdx);
			HSSFCell cell = row.getCell(columnIdx);           
			if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {      
				try {
					result = cell.getDateCellValue();
				} catch(Exception e) {
					return null;
				}
			} else {
				return null;
			}
		} catch(Exception e) {
			return null;
		}           
		return result;
	}

	public Double parseCellAtAsNumeric(HSSFSheet hssfsheet, int columnIdx, int rowIdx) { 
		Double result = null;		
		try {
			HSSFRow row = hssfsheet.getRow(rowIdx);
			HSSFCell cell = row.getCell(columnIdx);           
			if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {      
				try {
					result = cell.getNumericCellValue();
				} catch(Exception e) {
					return null;
				}
			} else {
				return null;
			}
		} catch(Exception e) {
			return null;
		}           
		return result;
	}

	public HSSFSheet addSheet(String sheetName) throws Exception {
		sheet = wb.createSheet();
		wb.setSheetName(sheetNum, sheetName);
		sheetNum++;
		rowindex = 0;				
		return sheet;
	}

	public void addNewRow() throws Exception {
		row = sheet.createRow(rowindex);
		rowindex++;
		columnIndex = 0;
	}

	public void addCell(Object value) throws Exception {
		cell = row.createCell(columnIndex);		
		if(value != null) {
			if (value instanceof Double) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				double dValue = ((Double)value).doubleValue(); 
				cell.setCellValue(Double.isNaN(dValue) ? 0 : dValue);
			} else if (value instanceof Integer) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				double dValue2 = ((Integer) value).doubleValue();
				cell.setCellValue(Double.isNaN(dValue2) ? 0 : dValue2);
			} else if (value instanceof Long) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				double dValue3 = ((Long) value).doubleValue();
				cell.setCellValue(Double.isNaN(dValue3) ? 0 : dValue3);
			} else if(value instanceof Calendar || value instanceof Date) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue((java.util.Calendar) value);
			} else {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(value.toString());
			}
		}	
		columnIndex++;
	}

	public void updateCell(int columnIdx, int rowIdx, Object value) throws Exception {
		if(row == null)
			row = sheet.getRow(rowIdx);
		cell = row.getCell(columnIdx);
		if (value != null) {
			if (value instanceof Double) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				double dValue = ((Double)value).doubleValue(); 
				cell.setCellValue(Double.isNaN(dValue) ? 0 : dValue);
			} else if (value instanceof Integer) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				double dValue2 = ((Integer) value).doubleValue();
				cell.setCellValue(Double.isNaN(dValue2) ? 0 : dValue2);
			} else if (value instanceof Long) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				double dValue3 = ((Long) value).doubleValue();
				cell.setCellValue(Double.isNaN(dValue3) ? 0 : dValue3);
			} else if(value instanceof Calendar || value instanceof Date) {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue((java.util.Calendar) value);
			} else {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(value.toString());
			}
		}
	}

	private List<Object> parseColumnAt(HSSFSheet hssfsheet, int columnIdx) throws Exception {
		//0 should be always the header row
		int firstRow = 0; 
		int lastRow = hssfsheet.getLastRowNum();
		List<Object> results = new ArrayList<Object>();	
		for(int i=firstRow+1; i<=lastRow; i++) {
			results.add(parseCellAt(hssfsheet, columnIdx, i));
		}		
		return results;
	}

}
