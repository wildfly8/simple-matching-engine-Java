public class ExchangeParty {
	
	private String partyName;
	private Integer netPosition		= 0;
	private Double deltaCash		= 0.0d;
	private Double pnL				= 0.0d;
	private Integer totalVolume   	= 0;
	private Integer numOfBuyFills   = 0;
	private Integer numOfSellFills  = 0;

	
	public ExchangeParty(String partyName) {
		this.partyName = partyName;
	}
	
	public String toString() {
		String res = partyName +", net position = " + netPosition;
		res += ", PnL = " + pnL;
		res += ", TotalVolume = " + totalVolume;
		res += ", NumOfBuyFills = " + numOfBuyFills;
		res += ", NumOfSellFills = " + numOfSellFills;
		res += ", NumOfTotalFills = " + (numOfBuyFills + numOfSellFills);
		return res;
	}
	
	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}
	
	public String getPartyName() {
		return partyName;
	}

	public void setNetPosition(Integer netPosition) {
		this.netPosition = netPosition;
	}

	public Integer getNetPosition() {
		return netPosition;
	}
	
	public Double getPnL() {
		return pnL;
	}

	public void calculatePnL(double lastPx) {
		this.pnL = deltaCash + netPosition * lastPx;
	}

	public void setNumOfBuyFills(Integer numOfBuyFills) {
		this.numOfBuyFills = numOfBuyFills;
	}

	public Integer getNumOfBuyFills() {
		return numOfBuyFills;
	}

	public void setNumOfSellFills(Integer numOfSellFills) {
		this.numOfSellFills = numOfSellFills;
	}

	public Integer getNumOfSellFills() {
		return numOfSellFills;
	}

	public void setDeltaCash(Double deltaCash) {
		this.deltaCash = deltaCash;
	}

	public Double getDeltaCash() {
		return deltaCash;
	}

	public void setTotalVolume(Integer totalVolume) {
		this.totalVolume = totalVolume;
	}

	public Integer getTotalVolume() {
		return totalVolume;
	}

}
