/**
 * 
 */
package com.bixan.revest.market;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bixan.revest.core.Constant.Default;
import com.bixan.revest.core.Constant.MarketIndex;
import com.bixan.revest.core.Constant.SimulationConf;
import com.bixan.revest.core.Constant.StatDist;
import com.bixan.revest.stat.DailyMarket;

import lombok.NoArgsConstructor;


/**
 * Using Laplace  came from 
 * https://seekingalpha.com/article/3959933-predicting-stock-market-returns-lose-normal-switch-laplace?page=3
 * 
 * This article shows the daily S&P return follows Laplace distribution better
 * 
 * The MLE (maximum likelihood estimator) for location (mu) and scale (beta) came from
 * https://www.itl.nist.gov/div898/handbook/eda/section3/eda366c.htm
 * 
 * @author sambit
 *
 */
@NoArgsConstructor
@Service
public class SimulatedReturn {
	private static final Logger log = LogManager.getLogger(SimulatedReturn.class);
	private static final RandomDataGenerator daddyRand = new RandomDataGenerator();
		
	private MarketIndex mktIdx = null;
	private DailyMarket dailyMkt = null;
	private double dailyMean = 0.0005715194046;
	private double dailyVar = 0.009612002463;
	private double dailyMedian = 0.0008807983703;
	
	private double dailySD = Math.sqrt(dailyVar);
	private double dailyMu = 0.00d;					// location parameter for Laplace Dist
	private double dailyBeta = 0.00d;				// scale parameter for Laplace Dist
	//private double daily_beta = 0.006498297897;
	
	
	private StatDist dist = null;
	private RandomDataGenerator rand = null; 
	private LaplaceDistribution laplace = null;
	
	private Vector<Double> dailyRates = new Vector<Double>();
	
	@Autowired
	private DailyMarket dailyMarket;
	
	
	static {
		daddyRand.reSeed(System.currentTimeMillis());
	}
	
	public void setupSimulatedReturn(StatDist dist, MarketIndex idx) throws IOException {
		this.mktIdx = idx;
		this.dist = dist;
		DailyMarket dailyMarket = new DailyMarket();
		this.dailyMkt = dailyMarket.getDailyMarket(mktIdx, dist);
		dailyMu = dailyMkt.getLocationMLE();
		dailyBeta = dailyMkt.getScaleMLE();
		log.info("Location MLE: " + dailyMu);
		log.info("Scale MLE: " + dailyBeta);
		
		switch(dist) {
		case GAUSSIAN:
			rand = new RandomDataGenerator();
			long seed = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(9)) + 
					daddyRand.nextLong(0, Long.MAX_VALUE - 10000);
			rand.reSeed(seed);
			break;
		case LAPLACE:
			laplace = new LaplaceDistribution(dailyMu, dailyBeta);
			break;
		}
	}
	
	public double next() {
		switch(dist) {
		case GAUSSIAN:
			return nextGaussian();
		case LAPLACE:
			return nextLaplace();
		}
		
		return 0.00d;
	}
	
	private double nextGaussian() {
		double yearly_rate = 0.00d;
		
		for (int i = 0; i < SimulationConf.DAYS_IN_YEAR; i++) {
			double gSample = rand.nextGaussian(dailyMu, dailyBeta);
			dailyRates.add(gSample);
			yearly_rate += gSample;
		}
		
		return yearly_rate;
	}
	
	private double nextLaplace() {
		double yearly_rate = 0.00d;
		
		for (int i = 0; i < SimulationConf.DAYS_IN_YEAR; i++) {
			double lSample = laplace.sample();
			dailyRates.add(lSample);
			yearly_rate += lSample;
		}
		
		return yearly_rate;
	}
	
	public double[] getPredictedyDailyRates() {
		double[] r = new double[dailyRates.size()];
		
		for (int i = 0; i < dailyRates.size(); i++) {
			r[i] = dailyRates.get(i);
		}
		
		return r;
	}
	
	public DailyMarket getDailyMarket() {
		return this.dailyMkt;
	}
}
