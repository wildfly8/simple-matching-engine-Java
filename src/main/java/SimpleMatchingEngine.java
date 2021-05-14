import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class SimpleMatchingEngine {

	private String symbol;
	private boolean isHalted;
	private PriorityQueue<OrderTO> bidBook   = new PriorityQueue<OrderTO>();
	private PriorityQueue<OrderTO> offerBook = new PriorityQueue<OrderTO>();
	private List<ExchangeParty> parties 	 = new ArrayList<ExchangeParty>();
	private ExchangeParty lastParty;
	private ExchangeParty lastCounterParty;
	private Double lastPx;
	private Integer volume					 = 0;
	private static PrintWriter pwRejected;
	private static PrintWriter pwTrades;
	private static PrintWriter pwOrderbook;

	static {
		try {
			pwRejected = new PrintWriter(new FileWriter("rejected.txt"));
			pwTrades = new PrintWriter(new FileWriter("trades.txt"));
			pwOrderbook = new PrintWriter(new FileWriter("orderbook.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	
	public SimpleMatchingEngine(String symbol, boolean isHalted) {
		this.symbol = symbol;
		this.isHalted = isHalted;
	}
	
	public static void main(String[] args) throws Exception {
		//input data preparation
		Map<String, SimpleMatchingEngine> map = ExcelOrderDAOImpl.getOrderbookMap();		
		int rowNum = ExcelOrderDAOImpl.getRowNumber();
		//core matching engine algo accepting limit orders sequentially with capability of partial fills
		long start = System.currentTimeMillis();
		for(int i=1; i<rowNum; i++) {
			OrderTO order = ExcelOrderDAOImpl.getOrderAt(i);
			SimpleMatchingEngine engine = map.get(order.getSymbol());
			if(engine.isHalted) {
				pwRejected.println("Order # "+ (i+1) + " : rejected because " + order.getSymbol() + " is halted!");
			} else {
				engine.acceptOrder(order);				
			}
		}
		pwRejected.close();
		pwTrades.close();
		System.out.println("total time spent in matching all orders = " + (System.currentTimeMillis() - start) + " milliseconds");
		//result orderbook print
		for(String key : map.keySet()) {
			if(!map.get(key).isHalted) {
				Object[] bidOrders = map.get(key).getBidBook().toArray();
				Object[] offerOrders = map.get(key).getOfferBook().toArray();
				String ordersLine = key + " bidBook: [";
				if(bidOrders.length > 0) {
					for(Object bidOrder : bidOrders) {
						ordersLine += ((OrderTO) bidOrder).toString() + ", ";
					}
				}
				ordersLine += "], offerBook: [";
				if(offerOrders.length > 0) {
					for(Object offerOrder : offerOrders) {
						ordersLine += ((OrderTO) offerOrder).toString();
					}
				}
				pwOrderbook.println(ordersLine + "]");
				/*for(ExchangeParty pty : map.get(key).getParties()) {
					pty.calculatePnL(map.get(key).getLastPx() == null? 0 : map.get(key).getLastPx());
				}*/
			}	
		}
		pwOrderbook.close();
	}
	
	public void acceptOrder(OrderTO elo) throws Exception {
		addParty(elo);
		processOrderRecursively(elo);
	}
	
	public ExchangeParty findPartyByName(String partyName) throws Exception {
		for(ExchangeParty party : parties) {
			if(partyName.equals(party.getPartyName())) {
				return party;
			}
		}
		throw new Exception("No party exists on the order book yet as " + partyName);
	}
	
	public PriorityQueue<OrderTO> getBidBook() {
		return bidBook;
	}

	public void setBidBook(PriorityQueue<OrderTO> bidBook) {
		this.bidBook = bidBook;
	}

	public PriorityQueue<OrderTO> getOfferBook() {
		return offerBook;
	}

	public void setOfferBook(PriorityQueue<OrderTO> offerBook) {
		this.offerBook = offerBook;
	}

	public List<ExchangeParty> getParties() {
		return parties;
	}

	public ExchangeParty getLastParty() {
		return lastParty;
	}

	public ExchangeParty getLastCounterParty() {
		return lastCounterParty;
	}

	public Double getLastPx() {
		return lastPx;
	}
	
	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Integer getVolume() {
		return volume;
	}
	
	private void addParty(OrderTO elo) {
		String lastPartyName = elo.getParty();
		for(ExchangeParty party : parties) {
			if(lastPartyName.equals(party.getPartyName())) {
				lastParty = party;
				return;
			}
		}
		lastParty = new ExchangeParty(lastPartyName);
		parties.add(lastParty);
	}
	
	/**Possibly process the incoming order recursively, until it's totally filled or hits/lifts all bids/offers
	 * or is not matched anymore.
	 * 
	 * @param OrderTO elo
	 * @throws Exception 
	 */
	private void processOrderRecursively(OrderTO elo) throws Exception {
		if(matchOrder(elo)) {
			fillOrder(elo);
			if(elo.getOrdStatus().equals("Filled")) {
				return;
			} else if(elo.getOrdStatus().equals("Partially filled")) {
				processOrderRecursively(elo);
				return;
			} else {
				throw new Exception("Matched order cannot be filled!");
			}
		} else {
			placeOrder(elo);
			return;
		}
	}

	private boolean matchOrder(OrderTO elo) {
		boolean isMatched = false;
		//peek exchange BBO
		OrderTO bb = bidBook.peek();
		OrderTO ob = offerBook.peek();
		//apply matching logic
		String side = elo.getSide();
		if(side.equals("buy") && ob != null) {
			if("market".equals(elo.getOrderType())) {
				return true;
			} else {
				if(elo.getPrice() != null && elo.getPrice() >= ob.getPrice()) {
					isMatched = true;
				}
			}
		} else if(side.equals("sell") && bb != null) {
			if("market".equals(elo.getOrderType())) {
				return true;
			} else {
				if(elo.getPrice() != null && elo.getPrice() <= bb.getPrice()) {
					isMatched = true;
				}
			}
		} 
		return isMatched;
	}
	
	private void fillOrder(OrderTO elo) throws Exception {
		String side = elo.getSide();
		int qty = elo.getQty();
		if(side.equals("buy")) {
			OrderTO ob = offerBook.peek();
			lastCounterParty = findPartyByName(ob.getParty());
			lastPx = ob.getPrice();
			int counter_qty = ob.getQty();
			lastParty.setNumOfBuyFills(lastParty.getNumOfBuyFills() + 1);
			lastCounterParty.setNumOfSellFills(lastCounterParty.getNumOfSellFills() + 1);
			if(qty < counter_qty) {
				volume += qty;
				//order party
				lastParty.setNetPosition(lastParty.getNetPosition() + qty);
				lastParty.setDeltaCash(lastParty.getDeltaCash() - qty * lastPx);
				lastParty.setTotalVolume(lastParty.getTotalVolume() + qty);
				elo.setOrdStatus("Filled");
				//counter party
				lastCounterParty.setNetPosition(lastCounterParty.getNetPosition() - qty);
				lastCounterParty.setDeltaCash(lastCounterParty.getDeltaCash() + qty * lastPx);
				lastCounterParty.setTotalVolume(lastCounterParty.getTotalVolume() + qty);
				ob.setQty(ob.getQty() - qty);
				ob.setOrdStatus("Partially filled");
			} else if(qty == counter_qty) {
				volume += qty;
				//order party
				lastParty.setNetPosition(lastParty.getNetPosition() + qty);
				lastParty.setDeltaCash(lastParty.getDeltaCash() - qty * lastPx);
				lastParty.setTotalVolume(lastParty.getTotalVolume() + qty);
				elo.setOrdStatus("Filled");
				//counter party
				lastCounterParty.setNetPosition(lastCounterParty.getNetPosition() - qty);
				lastCounterParty.setDeltaCash(lastCounterParty.getDeltaCash() + qty * lastPx);
				lastCounterParty.setTotalVolume(lastCounterParty.getTotalVolume() + qty);
				ob.setOrdStatus("Filled");
				offerBook.poll();
			} else {
				volume += counter_qty;
				//order party
				lastParty.setNetPosition(lastParty.getNetPosition() + counter_qty);
				lastParty.setDeltaCash(lastParty.getDeltaCash() - counter_qty * lastPx);
				lastParty.setTotalVolume(lastParty.getTotalVolume() + counter_qty);
				elo.setQty(elo.getQty() - counter_qty);
				elo.setOrdStatus("Partially filled");
				//counter party
				lastCounterParty.setNetPosition(lastCounterParty.getNetPosition() - counter_qty);
				lastCounterParty.setDeltaCash(lastCounterParty.getDeltaCash() + counter_qty * lastPx);
				lastCounterParty.setTotalVolume(lastCounterParty.getTotalVolume() + counter_qty);
				ob.setOrdStatus("Filled");
				offerBook.poll();
			}
		} else if(side.equals("sell")) {
			OrderTO bb = bidBook.peek();
			lastCounterParty = findPartyByName(bb.getParty());
			lastPx = bb.getPrice();
			int counter_qty = bb.getQty();
			lastParty.setNumOfSellFills(lastParty.getNumOfSellFills() + 1);
			lastCounterParty.setNumOfBuyFills(lastCounterParty.getNumOfBuyFills() + 1);
			if(qty < counter_qty) {
				volume += qty;
				//order party
				lastParty.setNetPosition(lastParty.getNetPosition() - qty);
				lastParty.setDeltaCash(lastParty.getDeltaCash() + qty * lastPx);
				lastParty.setTotalVolume(lastParty.getTotalVolume() + qty);
				elo.setOrdStatus("Filled");
				//counter party
				lastCounterParty.setNetPosition(lastCounterParty.getNetPosition() + qty);
				lastCounterParty.setDeltaCash(lastCounterParty.getDeltaCash() - qty * lastPx);
				lastCounterParty.setTotalVolume(lastCounterParty.getTotalVolume() + qty);
				bb.setQty(bb.getQty() - qty);
				bb.setOrdStatus("Partially filled");
			} else if(qty == counter_qty) {
				volume += qty;
				//order party
				lastParty.setNetPosition(lastParty.getNetPosition() - qty);
				lastParty.setDeltaCash(lastParty.getDeltaCash() + qty * lastPx);
				lastParty.setTotalVolume(lastParty.getTotalVolume() + qty);
				elo.setOrdStatus("Filled");
				//counter party
				lastCounterParty.setNetPosition(lastCounterParty.getNetPosition() + qty);
				lastCounterParty.setDeltaCash(lastCounterParty.getDeltaCash() - qty * lastPx);
				lastCounterParty.setTotalVolume(lastCounterParty.getTotalVolume() + qty);
				bb.setOrdStatus("Filled");
				bidBook.poll();
			} else {
				volume += counter_qty;
				//order party
				lastParty.setNetPosition(lastParty.getNetPosition() - counter_qty);
				lastParty.setDeltaCash(lastParty.getDeltaCash() + counter_qty * lastPx);
				lastParty.setTotalVolume(lastParty.getTotalVolume() + counter_qty);
				elo.setQty(elo.getQty() - counter_qty);
				elo.setOrdStatus("Partially filled");
				//counter party
				lastCounterParty.setNetPosition(lastCounterParty.getNetPosition() + counter_qty);
				lastCounterParty.setDeltaCash(lastCounterParty.getDeltaCash() - counter_qty * lastPx);
				lastCounterParty.setTotalVolume(lastCounterParty.getTotalVolume() + counter_qty);
				bb.setOrdStatus("Filled");
				bidBook.poll();
			}
		}
		pwTrades.println("trade: " + elo.getSymbol() + " " + lastPx + " " + elo.getTimeStamp());
	}

	private void placeOrder(OrderTO elo) {
		if("market".equals(elo.getOrderType())) {
			pwRejected.println("Market Order # "+ (elo.getId()+1) + " : rejected because " + elo.getSymbol() + " has no match by now!");
			return;
		} else {
			if(elo.getPrice() == null) {
				pwRejected.println("Limit Order # "+ (elo.getId()+1) + " : rejected because " + elo.getSymbol() + " has no price!");
				return;
			}
			String side = elo.getSide();
			if(side.equals("buy")) {
				bidBook.add(elo);
			} else if(side.equals("sell")) {
				offerBook.add(elo);
			}
			//System.out.println("Incoming order placed. " + elo.toString());
		}
	}
	
	public boolean isHalted() {
		return isHalted;
	}
	
}
