import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelOrderDAOImpl {
	
	private static HSSFWorkbook workBook;
	private static HSSFSheet sheet;	


	public static OrderTO getOrderAt(int idx) throws Exception {
	    ExcelCache excelCache = new ExcelCache();
		String symbol = excelCache.parseCellAtAsString(sheet, 0, idx);
		String side = excelCache.parseCellAtAsString(sheet, 1, idx);
		String orderType = excelCache.parseCellAtAsString(sheet, 2, idx);
		Double price = excelCache.parseCellAtAsNumeric(sheet, 3, idx);
		Long timestamp = excelCache.parseCellAtAsNumeric(sheet, 4, idx).longValue();
		return new OrderTO(idx, idx + "", symbol, orderType, price, 1, timestamp, side);
	}
	
	public static int getRowNumber() throws Exception {
	    ExcelCache excelCache = new ExcelCache();
	    String absolutePath = Paths.get(ExcelOrderDAOImpl.class.getClassLoader().getResource("orders.csv").toURI()).toFile().getAbsolutePath();
		workBook = excelCache.loadExcelFile(absolutePath);
		sheet = workBook.getSheetAt(0);
		return (sheet.getLastRowNum() + 1);
	}

	public static Map<String, SimpleMatchingEngine> getOrderbookMap() throws Exception {
		Map<String, SimpleMatchingEngine> ret = new HashMap<String, SimpleMatchingEngine>();
	    ExcelCache excelCache = new ExcelCache();
	    String absolutePath = Paths.get(ExcelOrderDAOImpl.class.getClassLoader().getResource("symbols.csv").toURI()).toFile().getAbsolutePath();
	    HSSFSheet sheet = excelCache.loadExcelFile(absolutePath).getSheetAt(0);
		for(int i=1; i<sheet.getLastRowNum()+1; i++) {
			ret.put(excelCache.parseCellAtAsString(sheet, 0, i), new SimpleMatchingEngine(excelCache.parseCellAtAsString(sheet, 0, i), sheet.getRow(i).getCell(1).getBooleanCellValue()));
		}
		return ret;
	}

}
