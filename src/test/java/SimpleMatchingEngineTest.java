import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SimpleMatchingEngineTest {
	
	private Map<String, SimpleMatchingEngine> map = null;

	@Before
	public void init() throws Exception {
		//input data preparation for first 20 samples
		map = ExcelOrderDAOImpl.getOrderbookMap();
		int rowNum = ExcelOrderDAOImpl.getRowNumber();
		//core matching engine algo
		for(int i=1; i<rowNum; i++) {
			OrderTO order = ExcelOrderDAOImpl.getOrderAt(i);
			SimpleMatchingEngine engine = map.get(order.getSymbol());
			if(engine.isHalted()) {
				//pwRejected.println("Order # "+ (i+1) + " : rejected because " + order.getSymbol() + " is halted!");
			} else {
				engine.acceptOrder(order);				
			}
		}
	}

	@Test
	public void testMatchingAlgo() {
		//test matching result for first 20 samples
		for(String key : map.keySet()) {
			if(!map.get(key).isHalted()) {
				int confirm_volume = 0;
				for(ExchangeParty pty : map.get(key).getParties()) {
					confirm_volume += pty.getTotalVolume();
				}
				System.out.println(key + ": trading volume is " + map.get(key).getVolume());
				System.out.println(key + ": confirm trading volume is :" + confirm_volume/2.0);
				assertEquals(map.get(key).getVolume().intValue(), confirm_volume/2);
			}	
		}
	}
}
