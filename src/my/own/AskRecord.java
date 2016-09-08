package my.own;

import my.util.BidUtil;
import org.apache.ignite.cache.query.annotations.QuerySqlField;


public class AskRecord implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2429769253245743208L;

//	@QuerySqlField(index=false, orderedGroups={
//		@QuerySqlField.Group(name = "ask_idx", order = 20)})
	@QuerySqlField(index=false)
	private Long bid;
	
	@QuerySqlField(index = false)
	private Integer race;
	
	@QuerySqlField(index = false)
	private Integer horse;
	
	@QuerySqlField(orderedGroups={
		@QuerySqlField.Group(name = "ask_idx", order = 7)})
	private Double win;
	
	@QuerySqlField(orderedGroups={
		@QuerySqlField.Group(name = "ask_idx", order = 8)})
	private Double place;
	
	@QuerySqlField
    final public Long bidLevel;	
	
	@QuerySqlField(orderedGroups={
		@QuerySqlField.Group(name = "ask_idx", order = 1)})
	private Double amount;
	
	@QuerySqlField(orderedGroups={
		@QuerySqlField.Group(name = "ask_idx", order = 2, descending=true)})
	private Double limitWin;
	
	@QuerySqlField(orderedGroups={
		@QuerySqlField.Group(name = "ask_idx", order = 3, descending=true)})
	private Double limitPlace;
	
	@QuerySqlField(index=false, orderedGroups={
		@QuerySqlField.Group(name = "ask_idx", order = 9)})
	private Integer live;

	@QuerySqlField(index = false)
	private String uid_a;
	private String currency;
	
	@QuerySqlField(index = false)
	private String raceType;
	
	@QuerySqlField(index = false)
	private String raceDate;
	private String ratio;
	private String rebateRatio;
	private Double sgWin;
	private Double sgPlace;
	private Integer ptType;
	private String opp_ratio;
	private Integer location;
	private String ip;
	private String ptSetting;
	private String ptMax;
	
	@QuerySqlField(index=false, orderedGroups={
		@QuerySqlField.Group(name = "ask_idx", order = 4)})
	private Long dateA;
	private Double time_5;
	private Double time_10;
	private Double time_15;
	private Double time_20;
	private Double time_25;
	private Double time_30;
	private Double time_60;
	private Double time_9999;

	/**
	 * @param bid
	 * @param race
	 * @param horse
	 * @param win
	 * @param place
	 * @param amount
	 * @param limitWin
	 * @param limitPlace
	 * @param uid_a
	 * @param currency
	 * @param raceType
	 * @param raceDate
	 * @param sgWin
	 * @param sgPlace
	 * @param date_a
	 */
	public AskRecord(Long bid, int race, int horse, double win, double place, double amount, double limitWin,
		double limitPlace, String uid_a, String currency, String raceType, String raceDate, double sgWin,
		double sgPlace, Long date_a, int live) {
		super();
		this.bid = bid;
		this.race = race;
		this.horse = horse;
		this.win = win;
		this.place = place;

		this.amount = amount;
		this.limitWin = limitWin;
		this.limitPlace = limitPlace;
		this.uid_a = uid_a;
		this.currency = currency;
		this.raceType = raceType;
		this.raceDate = raceDate;
		this.sgWin = sgWin;
		this.sgPlace = sgPlace;
		this.dateA = date_a;
		this.live = live;
		this.bidLevel = BidUtil.formatWinPlcLimitKey(amount, limitWin, limitPlace);
	}
	
	/**
	 * @param bid
	 * @param race
	 * @param horse
	 * @param win
	 * @param place
	 * @param amount
	 * @param limitWin
	 * @param limitPlace
	 * @param uid_a
	 * @param currency
	 * @param raceType
	 * @param raceDate
	 * @param sgWin
	 * @param sgPlace
	 * @param opp_ratio
	 * @param dateA
	 */
	public AskRecord(Long bid) {
		super();
		this.bid = bid;
		this.bidLevel = 0L;
	}
	

	/**
	 * @return the auctionId
	 */
	public Long getAuctionId() {
		return bid;
	}

	/**
	 * @return the bid
	 */
	public Long getBid() {
		return bid;
	}

	/**
	 * @param bid
	 *            the bid to set
	 */
	public void setBid(Long bid) {
		this.bid = bid;
	}

	/**
	 * @return the race
	 */
	public int getRace() {
		return race;
	}

	/**
	 * @param race
	 *            the race to set
	 */
	public void setRace(int race) {
		this.race = race;
	}

	/**
	 * @return the horse
	 */
	public int getHorse() {
		return horse;
	}

	/**
	 * @param horse
	 *            the horse to set
	 */
	public void setHorse(int horse) {
		this.horse = horse;
	}

	/**
	 * @return the win
	 */
	public double getWin() {
		return win;
	}

	/**
	 * @param win
	 *            the win to set
	 */
	public void setWin(Double win) {
		this.win = win;
	}

	/**
	 * @return the place
	 */
	public double getPlace() {
		return place;
	}

	public void setPlace(Double p) {
		assert p > 0 : "place <=0 " + p;
		this.place = p;
	}

	public void resetTicket(double w, double p) {
		assert w > 0 : "win <=0 : " + w;
		assert p > 0 : "place <=0 : " + p;
		this.win = w;
		this.place = p;
	}

	/**
	 * @return the amount
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	/**
	 * @return the limitWin
	 */
	public double getLimitWin() {
		return limitWin;
	}

	/**
	 * @param limitWin
	 *            the limitWin to set
	 */
	public void setLimitWin(double limitWin) {
		this.limitWin = limitWin;
	}

	/**
	 * @return the limitPlace
	 */
	public double getLimitPlace() {
		return limitPlace;
	}

	/**
	 * @param limitPlace
	 *            the limitPlace to set
	 */
	public void setLimitPlace(double limitPlace) {
		this.limitPlace = limitPlace;
	}

	/**
	 * @return the uid_a
	 */
	public String getUid_a() {
		return uid_a;
	}

	/**
	 * @param uid_a
	 *            the uid_a to set
	 */
	public void setUid_a(String uid_a) {
		this.uid_a = uid_a;
	}

	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * @param currency
	 *            the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * @return the raceType
	 */
	public String getRaceType() {
		return raceType;
	}

	/**
	 * @param raceType
	 *            the raceType to set
	 */
	public void setRaceType(String raceType) {
		this.raceType = raceType;
	}

	/**
	 * @return the raceDate
	 */
	public String getRaceDate() {
		return raceDate;
	}

	/**
	 * @param raceDate
	 *            the raceDate to set
	 */
	public void setRaceDate(String raceDate) {
		this.raceDate = raceDate;
	}

	/**
	 * @return the ratio
	 */
	public String getRatio() {
		return ratio;
	}

	/**
	 * @param ratio
	 *            the ratio to set
	 */
	public void setRatio(String ratio) {
		this.ratio = ratio;
	}

	/**
	 * @return the rebateRatio
	 */
	public String getRebateRatio() {
		return rebateRatio;
	}

	/**
	 * @param rebateRatio
	 *            the rebateRatio to set
	 */
	public void setRebateRatio(String rebateRatio) {
		this.rebateRatio = rebateRatio;
	}

	/**
	 * @return the sgWin
	 */
	public double getSgWin() {
		return sgWin;
	}

	/**
	 * @param sgWin
	 *            the sgWin to set
	 */
	public void setSgWin(double sgWin) {
		this.sgWin = sgWin;
	}

	/**
	 * @return the sgPlace
	 */
	public double getSgPlace() {
		return sgPlace;
	}

	/**
	 * @param sgPlace
	 *            the sgPlace to set
	 */
	public void setSgPlace(double sgPlace) {
		this.sgPlace = sgPlace;
	}

	/**
	 * @return the ptType
	 */
	public int getPtType() {
		return ptType;
	}

	/**
	 * @param ptType
	 *            the ptType to set
	 */
	public void setPtType(int ptType) {
		this.ptType = ptType;
	}

	/**
	 * @return the opp_ratio
	 */
	public String getOpp_ratio() {
		return opp_ratio;
	}

	/**
	 * @param opp_ratio
	 *            the opp_ratio to set
	 */
	public void setOpp_ratio(String opp_ratio) {
		this.opp_ratio = opp_ratio;
	}

	/**
	 * @return the location
	 */
	public int getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(int location) {
		this.location = location;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip
	 *            the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the ptSetting
	 */
	public String getPtSetting() {
		return ptSetting;
	}

	/**
	 * @param ptSetting
	 *            the ptSetting to set
	 */
	public void setPtSetting(String ptSetting) {
		this.ptSetting = ptSetting;
	}

	/**
	 * @return the ptMax
	 */
	public String getPtMax() {
		return ptMax;
	}

	/**
	 * @param ptMax
	 *            the ptMax to set
	 */
	public void setPtMax(String ptMax) {
		this.ptMax = ptMax;
	}

	/**
	 * @return the date_a
	 */
	public Long getDateA() {
		return dateA;
	}


	/**
	 * @return the time_5
	 */
	public double getTime_5() {
		return time_5;
	}

	/**
	 * @param time_5
	 *            the time_5 to set
	 */
	public void setTime_5(double time_5) {
		this.time_5 = time_5;
	}

	/**
	 * @return the time_10
	 */
	public double getTime_10() {
		return time_10;
	}

	/**
	 * @param time_10
	 *            the time_10 to set
	 */
	public void setTime_10(double time_10) {
		this.time_10 = time_10;
	}

	/**
	 * @return the time_15
	 */
	public double getTime_15() {
		return time_15;
	}

	/**
	 * @param time_15
	 *            the time_15 to set
	 */
	public void setTime_15(double time_15) {
		this.time_15 = time_15;
	}

	/**
	 * @return the time_20
	 */
	public double getTime_20() {
		return time_20;
	}

	/**
	 * @param time_20
	 *            the time_20 to set
	 */
	public void setTime_20(double time_20) {
		this.time_20 = time_20;
	}

	/**
	 * @return the time_25
	 */
	public double getTime_25() {
		return time_25;
	}

	/**
	 * @param time_25
	 *            the time_25 to set
	 */
	public void setTime_25(double time_25) {
		this.time_25 = time_25;
	}

	/**
	 * @return the time_30
	 */
	public double getTime_30() {
		return time_30;
	}

	/**
	 * @param time_30
	 *            the time_30 to set
	 */
	public void setTime_30(double time_30) {
		this.time_30 = time_30;
	}

	/**
	 * @return the time_60
	 */
	public double getTime_60() {
		return time_60;
	}

	/**
	 * @param time_60
	 *            the time_60 to set
	 */
	public void setTime_60(double time_60) {
		this.time_60 = time_60;
	}

	/**
	 * @return the time_9999
	 */
	public double getTime_9999() {
		return time_9999;
	}

	/**
	 * @param time_9999
	 *            the time_9999 to set
	 */
	public void setTime_9999(double time_9999) {
		this.time_9999 = time_9999;
	}


	
	/**
	 * @return the live
	 */
	public int getLive() {
		return live;
	}

	
	/**
	 * @param live the live to set
	 */
	public void setLive(int live) {
		this.live = live;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
			.format(
				"BidRecord [bid=%s, race=%s, horse=%s, win=%s, place=%s, amount=%s, limitWin=%s, limitPlace=%s, live=%s, uid_a=%s, currency=%s, raceType=%s, raceDate=%s, date_a=%s]",
				bid, race, horse, win, place, amount, limitWin, limitPlace, live, uid_a, currency, raceType,
				raceDate, dateA);
	}

	

	/**
	 * @param dateA the dateA to set
	 */
	public void setDateA(Long dateA) {
		this.dateA = dateA;
	}
	

	

}
