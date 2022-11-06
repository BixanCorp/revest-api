/**
 * 
 */
package com.bixan.revest.market;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bixan.revest.core.Constant.MarketIndex;
import com.bixan.revest.data.Investment;
import com.bixan.revest.data.Market;
import com.bixan.revest.data.MarketData;
import com.bixan.revest.util.DataUtil;

import lombok.NoArgsConstructor;

/**
 * @author sambit.bixan@gmail.com
 *
 */
public class Return {
	private static final double START_DOLLAR = 1000.00D;
	private ArrayList<MarketData> data = null;

	
	public Return(MarketIndex dataType) throws IOException {
		Market market = new Market();
		data = market.getData(dataType);
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<MarketData> getSuperlativeReturn(Investment inv, Comparator comp) {
		MarketData ret = new MarketData();
		ret.setYear(data.get(0).getYear());
		ret.setChange(data.get(0).getChange());
		ret.setEndDollar(inv.getStartAmount());
		if (inv.getPeriod() > data.size()) {
			return null;
		}
		
		double total = 0.0D;
		if (inv.getPeriod() == data.size()) {
			for (int cnt = 0; cnt < data.size() - 1; cnt++) {
				total += data.get(cnt).getChange();
			}
			
			double avg = total / (data.size() - 1);
			return this.data;
		}
		
		int maxIdx = data.size() - 1;
		ArrayList<MarketData> winner = null;
		ArrayList<MarketData> result = null;
		double winDollar = 0.00D;
		double currentDollar = 0.00D;
		for (int start = 0; start <= maxIdx - inv.getPeriod() + 1; start++) {
			result = this.getReturnByIdx(start, inv);
			
			if (start == 0) {
				winDollar = result.get(result.size() - 1).getEndDollar();
				winner = result;
				continue;
			}
			
			if (comp.compare(currentDollar = (result.get(result.size() - 1).getEndDollar()), winDollar) > 0) {
				winDollar = currentDollar;
				winner = result;
			}
		}
		
		return winner;
	}
	
	public ArrayList<MarketData> getReturnByStartYear(int startYear, Investment inv) {
		// startYear is beyond range
		if (startYear < data.get(0).getYear() || startYear > data.get(data.size() - 1).getYear()) {
			return null;
		}
		
		// doesn't have enough years starting from start year
		if (startYear + inv.getPeriod() > data.get(data.size() - 1).getYear()) {
			return null;
		}
		
		int yearIdx = startYear - data.get(0).getYear() + 1;
		return this.getReturnByIdx(yearIdx, inv);
	}
	
	public ArrayList<MarketData> getReturnByIdx(int startIdx, Investment inv) {
		ArrayList<MarketData> result = new ArrayList<MarketData>();
		double total = 0.0D;
		double currentDollar = inv.getStartAmount();
		for (int cnt = startIdx; cnt < startIdx + inv.getPeriod(); cnt++) {
			total += data.get(cnt).getChange();
			currentDollar = currentDollar * (1 + data.get(cnt).getChange() / 100) + inv.getAdditionalAmount();
			currentDollar = currentDollar < 0.00 ? 0.00D : currentDollar;
			MarketData line = new MarketData();
			line.setYear(data.get(cnt).getYear());
			line.setChange(data.get(cnt).getChange());
			line.setEndDollar(currentDollar);
			result.add(line);
		}
		
		return result;
	}
	
	public ArrayList<MarketData> getReturn(Investment inv) {
		int idx = data.size() - 1;
		ArrayList<MarketData> result = new ArrayList<MarketData>();
		ArrayList<MarketData> returned = null;
		double endDollar = 0.0D;
		double cagr = 0.00D;
		
		for (int i = 0; i < idx - inv.getPeriod() + 2; i++) {
			returned = getReturnByIdx(i, inv);
			if (null != returned) {
				endDollar = returned.get(returned.size() - 1).getEndDollar();
				cagr = Market.getCAGR(inv.getStartAmount(), endDollar, inv.getPeriod());
				MarketData line = new MarketData();
				line.setYear(data.get(i).getYear());
				line.setChange(cagr * 100);
				line.setEndDollar(endDollar);
				result.add(line);
			}
		}
		
		return result;
	}
	
	public ArrayList<MarketData> getBestReturn(int yearRange) {
		Investment inv = new Investment();
		inv.setPeriod(yearRange);
		inv.setStartAmount(START_DOLLAR);
		return this.getBestReturn(inv);
	}
	
	public ArrayList<MarketData> getBestReturn(Investment inv) {
		return this.getSuperlativeReturn(inv, new Better());
	}
	
	public ArrayList<MarketData> getWorstReturn(int yearRange) {
		Investment inv = new Investment();
		inv.setPeriod(yearRange);
		inv.setStartAmount(START_DOLLAR);
		return this.getWorstReturn(inv);
	}

	public ArrayList<MarketData> getWorstReturn(Investment inv) {
		return this.getSuperlativeReturn(inv, new Worse());
	}
	
	/**
	 * Returns the best <code>num</code> in terms of return sorted in descending order. If num is more than the
	 * total number of records, the available years are returned.
	 * @param num
	 * @return
	 */
	public ArrayList<MarketData> getBestYears(int num) {
		data.sort(new BetterReturn());
		
		if (num > data.size()) {
			return data;
		}
		
		return DataUtil.subList(data, 0, num);
	}
	
	
	/**
	 * Returns the worst <code>num</code> in terms of return sorted in ascending order. If num is more than the
	 * total number of records, the available years are returned.
	 * @param num
	 * @return
	 */
	public ArrayList<MarketData> getWorstYears(int num) {
		data.sort(new WorseReturn());
		
		if (num > data.size()) {
			return data;
		}
		
		return DataUtil.subList(data, 0, num);
	}
	
	public double getAverageReturn() {
		double total = 0.00D;
		int i = 0;
		for (i = 0; i < data.size(); i++) {
			MarketData md = data.get(i);
			total += md == null ? 0.00D : md.getChange();
		}
		
		return (total / i);
	}
	
	public double getStdDevReturn() {		
		double[] val = new double[data.size()];
		
		for (int i = 0; i < data.size(); i++) {
			val[i] = data.get(i).getChange();
		}
		
		return Math.sqrt(StatUtils.variance(val));
	}
	
	public int getMarketDataSize() {
		if (null == data) return 0;
		
		return data.size();
	}
	
	private static class Better implements Comparator<Double> {
		public int compare(Double o1, Double o2) {
			return o1.compareTo(o2);
		}
	}
	
	private static class BetterReturn implements Comparator<MarketData> {
		public int compare(MarketData r1, MarketData r2) {
			return new Better().compare(r2.getChange(), r1.getChange());
		}
	}
	
	private static class Worse implements Comparator<Double> {
		public int compare(Double o1, Double o2) {
			return o2.compareTo(o1);
		}
	}
	
	private static class WorseReturn implements Comparator<MarketData> {
		public int compare(MarketData r1, MarketData r2) {
			return new Worse().compare(r2.getChange(), r1.getChange());
		}
	}
}
