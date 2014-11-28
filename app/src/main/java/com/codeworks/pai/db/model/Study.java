package com.codeworks.pai.db.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class Study implements Serializable {

	private static final long	serialVersionUID	= -1275103175237753227L;
	// Status Bit Map
	public static int STATUS_NO_PRICE = 1;
	public static int STATUS_DELAYED_PRICE = 2;
	public static int STATUS_INSUFFICIENT_HISTORY = 4;
	
	public static String format(double value) {
		if (value != Double.NaN) {
			return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
		}
		return "";
	}
	String						symbol;
	String						name;
	MaType						maType;
	double						price;
    double                      extMarketPrice;
	double						open;
	double						high;
	double						low;
	double						lastClose;
	double						priceLastWeek;
	double						priceLastMonth;
	double						averageTrueRange;
	double						stochasticK;
	double						stochasticD;
	double						emaMonth;
	double						emaWeek;
	double						emaLastWeek;
	double						emaLastMonth;
	double						emaStddevWeek;
	double						emaStddevMonth;
	double						smaWeek;
	double						smaMonth;
	double						smaLastWeek;
	double						smaLastMonth;
	double						smaStddevWeek;
	double						smaStddevMonth;
	long						securityId;
	int							portfolioId;
	Notice						notice;
	Date						noticeDate;
	int							contracts;

	Date						priceDate = new Date();
	int							statusMap;
	
	boolean						historyReloaded = false;

	public Study(String symbol) {
		this.symbol = symbol;
	}

	public double getAverageTrueRange() {
		return averageTrueRange;
	}

	public int getContracts() {
		return contracts;
	}

	public double getHigh() {
		return high;
	}

	public double getLastClose() {
		return lastClose;
	}

	public double getLow() {
		return low;
	}

	public double getEmaLastMonth() {
		return emaLastMonth;
	}

	public double getEmaLastWeek() {
		return emaLastWeek;
	}

	public double getEmaMonth() {
		return emaMonth;
	}

	public MaType getMaType() {
		return maType;
	}

	public double getEmaWeek() {
		return emaWeek;
	}

	public double getMovingAverage(Period period) {
		if (MaType.E.equals(maType)) {
			if (Period.Week.equals(period)) {
				return getEmaWeek();
			} else {
				return getEmaMonth();
			}
		} else {
			if (Period.Week.equals(period)) {
				return getSmaWeek();
			} else {
				return getSmaMonth();
			}
		}
	}

	public String getName() {
		return name;
	}

	
	public Notice getNotice() {
		return notice;
	}
	
	public Date getNoticeDate() {
		return noticeDate;
	}

	public double getOpen() {
		return open;
	}

	public int getPortfolioId() {
		return portfolioId;
	}

	public double getPrice() {
		return price;
	}

	public Date getPriceDate() {
		return priceDate;
	}

	public double getPriceLastMonth() {
		return priceLastMonth;
	}

	public double getPriceLastWeek() {
		return priceLastWeek;
	}

	public long getSecurityId() {
		return securityId;
	}

	public double getSmaLastMonth() {
		return smaLastMonth;
	}

	public double getSmaLastWeek() {
		return smaLastWeek;
	}

	public double getSmaMonth() {
		return smaMonth;
	}

	public double getSmaStddevMonth() {
		return smaStddevMonth;
	}

	public double getSmaStddevWeek() {
		return smaStddevWeek;
	}

	public double getSmaWeek() {
		return smaWeek;
	}

	public int getStatusMap() {
		return statusMap;
	}

	public double getEmaStddevMonth() {
		return emaStddevMonth;
	}

	public double getEmaStddevWeek() {
		return emaStddevWeek;
	}

	public String getSymbol() {
		return symbol;
	}

	public boolean hasDelayedPrice() {
		return ((statusMap & STATUS_DELAYED_PRICE) != 0);
	}

	public boolean hasInsufficientHistory() {
		return ((statusMap & STATUS_INSUFFICIENT_HISTORY) != 0);
	}

	public boolean hasNoPrice() {
		return ((statusMap & STATUS_NO_PRICE) != 0);
	}

	/**
	 * is valid good data
	 * @return
	 */
	public boolean isValid() {
		return (isValidMonth() && isValidWeek());
	}

	public boolean isValidMonth() {
		return (getEmaMonth() != 0) && (getSmaMonth() != 0);
		
	}

	public boolean isValidWeek() {
		return (getEmaWeek() != 0) && (getSmaWeek() != 0);
	}

	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public void setAverageTrueRange(double averageTrueRange) {
		this.averageTrueRange = averageTrueRange;
	}

	public void setContracts(int contracts) {
		this.contracts = contracts;
	}

	public void setDelayedPrice(boolean delayed) {
		if (delayed) {
			statusMap = statusMap | STATUS_DELAYED_PRICE;
		} else {
			statusMap = statusMap & ~STATUS_DELAYED_PRICE;
		}
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public void setHistoryReloaded(boolean historyReloaded) {
		this.historyReloaded = historyReloaded;
	}

	public void setInsufficientHistory(boolean value) {
		if (value) {
			statusMap = statusMap | STATUS_INSUFFICIENT_HISTORY;
		} else {
			statusMap = statusMap & ~STATUS_INSUFFICIENT_HISTORY;
		}
	}

	public void setLastClose(double lastClose) {
		this.lastClose = lastClose;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public void setEmaLastMonth(double maLastMonth) {
		this.emaLastMonth = maLastMonth;
	}

	public void setEmaLastWeek(double maLastWeek) {
		this.emaLastWeek = maLastWeek;
	}

	public void setEmaMonth(double ma_month) {
		this.emaMonth = ma_month;
	}

	public void setMaType(MaType maType) {
		this.maType = maType;
	}

	public void setEmaWeek(double maWeek) {
		this.emaWeek = maWeek;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNoPrice(boolean value) {
		if (value) {
			statusMap = statusMap | STATUS_NO_PRICE;
		} else {
			statusMap = statusMap & ~STATUS_NO_PRICE;
		}
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

	public void setNoticeDate(Date noticeDate) {
		this.noticeDate = noticeDate;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public void setPortfolioId(int portfolioId) {
		this.portfolioId = portfolioId;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setPriceDate(Date priceDate) {
		this.priceDate = priceDate;
	}

	public void setPriceDate(String priceDateStr) throws ParseException {
		if (priceDateStr == null) {
			this.priceDate = null;
		} else {
			this.priceDate = StudyTable.priceDateFormat.parse(priceDateStr);
		}
	}

	public void setPriceLastMonth(double priceLastMonth) {
		this.priceLastMonth = priceLastMonth;
	}

	public void setPriceLastWeek(double priceLastWeek) {
		this.priceLastWeek = priceLastWeek;
	}

	public void setSecurityId(long seurityId) {
		this.securityId = seurityId;
	}

	public void setSmaLastMonth(double smaLastMonth) {
		this.smaLastMonth = smaLastMonth;
	}

	public void setSmaLastWeek(double smaLastWeek) {
		this.smaLastWeek = smaLastWeek;
	}
	
	public void setSmaMonth(double smaMonth) {
		this.smaMonth = smaMonth;
	}
	
	public void setSmaStddevMonth(double s_stddevMonth) {
		this.smaStddevMonth = s_stddevMonth;
	}
	
	public void setSmaStddevWeek(double smaStddevWeek) {
		this.smaStddevWeek = smaStddevWeek;
	}
	
	public void setSmaWeek(double smaWeek) {
		this.smaWeek = smaWeek;
	}

	public void setStatusMap(int statusMap) {
		this.statusMap = statusMap;
	}

	public void setEmaStddevMonth(double stddev_month) {
		this.emaStddevMonth = stddev_month;
	}

	public void setEmaStddevWeek(double stdDeviation) {
		this.emaStddevWeek = stdDeviation;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getStochasticK() {
		return stochasticK;
	}

	public double getStochasticD() {
		return stochasticD;
	}

	public void setStochasticK(double stochastic_k) {
		this.stochasticK = stochastic_k;
	}

	public void setStochasticD(double stochastic_d) {
		this.stochasticD = stochastic_d;
	}

    public double getExtMarketPrice() {
        return extMarketPrice;
    }

    public void setExtMarketPrice(double extMarketPrice) {
        this.extMarketPrice = extMarketPrice;
    }


    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Symbol=");
		sb.append(symbol);
		sb.append(" ema=");
		sb.append(format(this.getEmaWeek()));
		sb.append(" PLW=" + format(priceLastWeek));
		sb.append(" maLM=" + format(emaLastMonth));
		sb.append(" PLM=" + format(priceLastMonth));
		sb.append(" map="+ statusMap);
		return sb.toString();
	}

	public boolean wasHistoryReloaded() {
		return historyReloaded;
	}
}
