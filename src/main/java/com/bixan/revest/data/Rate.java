/**
 * 
 */
package com.bixan.revest.data;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bixan.revest.core.Constant.Default;
import com.bixan.revest.core.Constant.MarketIndex;
import com.bixan.revest.core.Constant.RatePolicy;
import com.bixan.revest.market.Return;
import com.bixan.revest.market.SimulatedReturn;

import lombok.NoArgsConstructor;

/**
 * @author sambit
 *
 */
@NoArgsConstructor
public class Rate {
	private double inflation = 0.0D;
	private double earlyYear = 0.0D;
	private double midYear = 0.0D;
	private double advancedYear = 0.0D;
	private int ratePolicy = RatePolicy.CUSTOM;
	private MarketIndex mkt = MarketIndex.SP500;
	private double[] dailyPredictedRates = null;
	
	
	/**
	 * @return the inflation
	 */
	public double getInflation() {
		return inflation;
	}
	/**
	 * @param inflation the rate in percent per year
	 */
	public void setInflation(double inflation) {
		this.inflation = inflation / 100;
	}
	/**
	 * @return the earlyYear
	 */
	public double getEarlyYear() {
		return earlyYear;
	}
	/**
	 * @param earlyYear the rate in percent per year
	 */
	public void setEarlyYear(double earlyYear) {
		this.earlyYear = earlyYear / 100;
	}
	/**
	 * @return the midYear
	 */
	public double getMidYear() {
		return midYear;
	}
	/**
	 * @param midYear the rate in percent per year
	 */
	public void setMidYear(double midYear) {
		this.midYear = midYear / 100;
	}
	/**
	 * @return the endYear
	 */
	public double getAdvancedYear() {
		return advancedYear;
	}
	/**
	 * @param endYear the rate in percent per year
	 */
	public void setAdvancedYear(double endYear) {
		this.advancedYear = endYear / 100;
	}
	public int getInternalPolicyValue() {
		return ratePolicy;
	}
	/**
	 * @return the policy
	 */
	public String getPolicy() {
		switch(this.ratePolicy) {
		case RatePolicy.BEST_BEST_BEST:
			return "BBB";
		case RatePolicy.BEST_BEST_WORST:
			return "BBW";
		case RatePolicy.BEST_WORST_WORST:
			return "BWW";
		case RatePolicy.BEST_WORST_BEST:
			return "BWB";
		case RatePolicy.WORST_WORST_WORST:
			return "WWW";
		case RatePolicy.WORST_WORST_BEST:
			return "WWB";
		case RatePolicy.WORST_BEST_WORST:
			return "WBW";
		case RatePolicy.WORST_BEST_BEST:
			return "WBB";
		case RatePolicy.AVERAGE:
			return "AVERAGE";
		case RatePolicy.CUSTOM:
			return "CUSTOM";
		case RatePolicy.RANDOM_RANDOM_RANDOM:
			return "RANDOM";
		default:
			return "NONE";
		}
	}
	/**
	 * @param policy the policy to set
	 */
	public void setPolicy(String policy) {
		if (null == policy || policy.trim().equals("")) {
			return;
		}
		
		switch(policy.toUpperCase()) {
		case "BBB":
			this.ratePolicy = RatePolicy.BEST_BEST_BEST;
			break;
		case "BBW":
			this.ratePolicy = RatePolicy.BEST_BEST_WORST;
			break;
		case "BWW":
			this.ratePolicy = RatePolicy.BEST_WORST_WORST;
			break;
		case "BWB":
			this.ratePolicy = RatePolicy.BEST_WORST_BEST;
			break;
		case "WWW":
			this.ratePolicy = RatePolicy.WORST_WORST_WORST;
			break;
		case "WWB":
			this.ratePolicy = RatePolicy.WORST_WORST_BEST;
			break;
		case "WBW":
			this.ratePolicy = RatePolicy.WORST_BEST_WORST;
			break;
		case "WBB":
			this.ratePolicy = RatePolicy.WORST_BEST_BEST;
			break;
		case "AVERAGE":
			this.ratePolicy = RatePolicy.AVERAGE;
			break;
		case "CUSTOM":
			this.ratePolicy = RatePolicy.CUSTOM;
			break;
		case "RANDOM":
			this.ratePolicy = RatePolicy.RANDOM_RANDOM_RANDOM;
			break;
		default:
			this.ratePolicy = RatePolicy.NONE;
			break;
		}
	}
	/**
	 * Returns an array of yearly returns best on the rate policy
	 * @param earlyYears
	 * @param midYears
	 * @param advancedYears
	 * @return
	 * @throws IOException 
	 */
	public double[] getRates(int earlyYears, int midYears, int advancedYears) throws IOException {
		double[] rates = null;
		
		switch(this.ratePolicy) {
		case RatePolicy.BEST_BEST_BEST:
			rates = this.getBBBRates(earlyYears, midYears, advancedYears);
			break;
		case RatePolicy.WORST_WORST_WORST:
			rates = this.getWWWRates(earlyYears, midYears, advancedYears);
			break;
		case RatePolicy.CUSTOM:
			rates = this.getCustomRates(earlyYears, midYears, advancedYears);
			break;
		case RatePolicy.RANDOM_RANDOM_RANDOM:
			rates = this.getRRRRates(earlyYears, midYears, advancedYears);
			break;
		case RatePolicy.AVERAGE:
		case RatePolicy.NONE:
		default:
			rates = this.getAverageRates(earlyYears, midYears, advancedYears);
			break;
		}
		
		return rates;
	}
	
	private double[] getBBBRates(int earlyYears, int midYears, int advancedYears) throws IOException {
		double[] rates = new double[earlyYears + midYears + advancedYears];
		
		Return r = new Return(mkt);
		ArrayList<MarketData> best = r.getBestReturn(earlyYears);
		for (int e = 0; e < earlyYears && null != best; e++) {
			MarketData md = best.get(e);
			rates[e] = md == null ? 0 : md.getChange() / 100;
		}
		
		best = r.getBestReturn(midYears);
		for (int m = 0; m < midYears && null != best; m++) {
			MarketData md = best.get(m);
			rates[earlyYears + m] = md == null ? 0 : md.getChange() / 100;
		}
		
		best = r.getBestReturn(advancedYears);
		for (int a = 0; a < advancedYears && null != best; a++) {
			MarketData md = best.get(a);
			rates[earlyYears + midYears + a] = md == null ? 0 : md.getChange() / 100;
		}
		
		return rates;
	}
	
	private double[] getWWWRates(int earlyYears, int midYears, int advancedYears) throws IOException {
		double[] rates = new double[earlyYears + midYears + advancedYears];
		
		Return r = new Return(mkt);
		ArrayList<MarketData> worst = r.getWorstReturn(earlyYears);
		for (int e = 0; e < earlyYears && null != worst; e++) {
			MarketData md = worst.get(e);
			rates[e] = md == null ? 0 : md.getChange() / 100;
		}
		
		worst = r.getWorstReturn(midYears);
		for (int m = 0; m < midYears && null != worst; m++) {
			MarketData md = worst.get(m);
			rates[earlyYears + m] = md == null ? 0 : md.getChange() / 100;
		}
		
		worst = r.getWorstReturn(advancedYears);
		for (int a = 0; a < advancedYears && null != worst; a++) {
			MarketData md = worst.get(a);
			rates[earlyYears + midYears + a] = md == null ? 0 : md.getChange() / 100;
		}
		
		return rates;
	}
	
	private double[] getRRRRates(int earlyYears, int midYears, int advancedYears) throws IOException {
		double[] rates = new double[earlyYears + midYears + advancedYears];
		
		SimulatedReturn simulRet = new SimulatedReturn();
		simulRet.setupSimulatedReturn(Default.STAT_DIST, Default.MARKET);
		
		for (int e = 0; e < earlyYears; e++) {
			rates[e] = simulRet.next();
		}
		
		for (int m = 0; m < midYears; m++) {
			rates[earlyYears + m] = simulRet.next();
		}
		
		for (int a = 0; a < advancedYears; a++) {
			rates[earlyYears + midYears + a] = simulRet.next();
		}
		
		dailyPredictedRates = simulRet.getPredictedyDailyRates();
		return rates;
	}
	
	private double[] getCustomRates(int earlyYears, int midYears, int advancedYears) {
		double[] rates = new double[earlyYears + midYears + advancedYears];
		
		for (int e = 0; e < earlyYears; e++) {
			rates[e] = this.earlyYear;
		}
		
		for (int m = 0; m < midYears; m++) {
			rates[earlyYears + m] = this.midYear;
		}
		
		for (int a = 0; a < advancedYears; a++) {
			rates[earlyYears + midYears + a] = this.advancedYear;
		}
		
		return rates;
	}
	
	private double[] getAverageRates(int earlyYears, int midYears, int advancedYears) throws IOException {
		Return r = new Return(mkt);
		double rate = r.getAverageReturn();
		double[] rates = new double[earlyYears + midYears + advancedYears]; 
		
		for (int i = 0; i < earlyYears + midYears + advancedYears; i++) {
			rates[i] = rate / 100;
		}
		
		return rates;
	}
	
	/**
	 * For Random policy
	 * @return
	 */
	public double[] getPredictedDailyrates() {
		return this.dailyPredictedRates;
	}
}
