public class OrderTO implements Comparable<OrderTO> {

	private int id;
	private String party;
	private String orderType;
	private Double price;
	private int qty;
	private long timeStamp;
	private String side;
	private String ordStatus;
	private String symbol;;

	
	public OrderTO(int id, String party, String symbol, String orderType, Double price, int qty, long timeStamp, String side) {
		this.id = id;
		this.party = party;
		this.orderType = orderType;
		this.price = price;
		this.qty = qty;
		this.timeStamp = timeStamp;
		this.side = side;
		this.symbol = symbol;
		this.setOrdStatus("New");
	}
	
	public int compareTo(OrderTO elo) {
		int result = 0;
		if(side.equals("buy")) {
			if(price > elo.getPrice() || (price == elo.getPrice() && qty > elo.getQty()) || (price == elo.getPrice() && qty == elo.getQty() && timeStamp < elo.getTimeStamp())) {
				result = -1;
			} else {
				result = 1;
			}
		} else if (side.equals("sell")) {
			if(price < elo.getPrice() || (price == elo.getPrice() && qty > elo.getQty()) || (price == elo.getPrice() && qty == elo.getQty() && timeStamp < elo.getTimeStamp())) {
				result = -1;
			} else {
				result = 1;
			}
		} else {
			result = 0;
		}
		
		return result;
	}
	
	public String toString() {
		String res = " OrderId=" + id;
		res += " Price=" + price;
		res += " Side=" + side;
		res += " Timestamp=" + timeStamp;
		return res;	
	}
	
	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getParty() {
		return party;
	}
	
	public void setParty(String party) {
		this.party = party;
	}
	
	public Double getPrice() {
		return price;
	}
	
	public void setPrice(Double price) {
		this.price = price;
	}
	
	public int getQty() {
		return qty;
	}
	
	public void setQty(int qty) {
		this.qty = qty;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public String getSide() {
		return side;
	}
	
	public void setSide(String side) {
		this.side = side;
	}

	public void setOrdStatus(String ordStatus) {
		this.ordStatus = ordStatus;
	}

	public String getOrdStatus() {
		return ordStatus;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
}
