/**
 * 
 */
package com.bixan.revest.market.simulation;

import org.apache.commons.math3.stat.StatUtils;

import com.bixan.revest.stat.DailyMarket;

/**
 * @author sambit
 *
 */
public class SimulationRun {
	private int runIdx = 0;
	private int lastWithdrawYearIdx = 0;
	private double endBalance = 0.00d;
	private double avRate = 0.00d;
	private double[] predictedDailyRates = null;
	private DailyMarket dailyMarket = null;
	private double finalPriceCloseness = 0.00d;		// closeness between last actual price and last predicted price
													// after this simulation run
	/**
	 * @return the dailyMarket
	 */
	public DailyMarket getDailyMarket() {
		return dailyMarket;
	}
	/**
	 * @param dailyMarket the dailyMarket to set
	 */
	public void setDailyMarket(DailyMarket dailyMarket) {
		this.dailyMarket = dailyMarket;
	}
	/**
	 * @return the runIdx
	 */
	public int getRunIndex() {
		return runIdx;
	}
	/**
	 * @param runIdx the runIdx to set
	 */
	public void setRunIndex(int runIdx) {
		this.runIdx = runIdx;
	}
	/**
	 * @return the lastWithdrawYearIdx
	 */
	public int getLastWithdrawYearIndex() {
		return lastWithdrawYearIdx;
	}
	/**
	 * @param lastWithdrawYearIdx the lastWithdrawYearIdx to set
	 */
	public void setLastWithdrawYearIndex(int lastWithdrawYearIdx) {
		this.lastWithdrawYearIdx = lastWithdrawYearIdx;
	}
	/**
	 * @return the endBalance
	 */
	public double getEndBalance() {
		return endBalance;
	}
	/**
	 * @param endBalance the endBalance to set
	 */
	public void setEndBalance(double endBalance) {
		this.endBalance = endBalance;
	}
	/**
	 * @return the avRate
	 */
	public double getAverageRate() {
		return avRate;
	}
	/**
	 * @param avRate the avRate to set
	 */
	public void setAverageRate(double avRate) {
		this.avRate = avRate;
	}
	/**
	 * @return the predictedDailyRates
	 */
	public double[] getPredictedDailyRates() {
		return predictedDailyRates;
	}
	/**
	 * 
	 */
	public double getMeanPredictedDailyRate() {
		double mean = 0.00d;
		if (null != predictedDailyRates) {
			mean = StatUtils.mean(predictedDailyRates);
		}
		return mean;
	}
	/**
	 * @param predictedDailyRates the predictedDailyRates to set
	 */
	public void setPredictedDailyRates(double[] predictedDailyRates) {
		this.predictedDailyRates = predictedDailyRates;
	}	
	/**
	 * @return the finalPriceCloseness
	 */
	public double getFinalPriceCloseness() {
		return finalPriceCloseness;
	}
	/**
	 * Returns how good is the predicted values to historical values. Useful only if
	 * we are using Random rates
	 */
	public double getFitnessMeasure() {
		return getOrdinaryLeastSquare();
	}
	
	/**
	 * Ordinary Least Square 
	 * https://stats.idre.ucla.edu/other/mult-pkg/faq/general/faq-what-are-pseudo-r-squareds/
	 * @return
	 */
	private double getOrdinaryLeastSquare() {
		if (null == predictedDailyRates) {
			return 0.00d;
		}
		
		double[] actualPrice = dailyMarket.getActualPrices();
		double meanPrice = dailyMarket.getMeanPrice();
		double meanDiffSumSqrd = 0.00f;
		double predDiffSumSqrd = 0.00f;
		int size = Math.min(actualPrice.length, predictedDailyRates.length);
		double predictedPrice = 0.00d;
		
		// for i = 0, there is no predicted price
		for (int i = 1; i < size; i++) {
			meanDiffSumSqrd += Math.pow((actualPrice[i] - meanPrice), 2);
			predictedPrice = actualPrice[i - 1] * (1 + predictedDailyRates[i]);
			predDiffSumSqrd += Math.pow((actualPrice[i] - predictedPrice), 2);
		}
		
		finalPriceCloseness = (predictedPrice - actualPrice[size - 1]) / actualPrice[size - 1];
		double meanSquare = 1 - (predDiffSumSqrd / meanDiffSumSqrd);
		return meanSquare;
	}
	
	/**
	 * Root Mean Square
	 * https://en.wikipedia.org/wiki/Root-mean-square_deviation
	 * @return
	 */
	private double getRootMeanSquare() {
		if (null == predictedDailyRates) {
			return 0.00d;
		}
		
		double[] actualPrice = dailyMarket.getActualPrices();
		double diffSqrdSum = 0.00f;
		int size = Math.min(actualPrice.length, predictedDailyRates.length);
		double predictedPrice = 0.00d;
		
		// for i = 0, there is no predicted price
		for (int i = 1; i < size; i++) {
			predictedPrice = actualPrice[i - 1] * (1 + predictedDailyRates[i]);
			diffSqrdSum += Math.pow((predictedPrice - actualPrice[i]), 2);
		}
		
		double meanSquare = Math.sqrt(diffSqrdSum / size);
		return meanSquare;
	}
	
	/**
	 * Normalized Root Mean Square
	 * https://en.wikipedia.org/wiki/Root-mean-square_deviation
	 * @return
	 */
	private double getNormalizedRMS() {
		double meanPrice = dailyMarket.getMeanPrice();
		return getRootMeanSquare() / meanPrice;
	}
	
}
