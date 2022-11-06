/**
 * 
 */
package com.bixan.revest.test;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bixan.revest.core.Constant.MarketIndex;
import com.bixan.revest.data.Investment;
import com.bixan.revest.data.Market;
import com.bixan.revest.data.MarketData;
import com.bixan.revest.market.Return;
import com.bixan.revest.market.simulation.Simulation;

/**
 * @author sambit.bixan@gmail.com
 *
 */
@Component
public class TestMarket {	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public void test() throws IOException {
		
		MarketIndex market = MarketIndex.SP500;
		int yearRange = 13;
		float startDollar = 1200000.00f;
		float addDollar = 42000.00f;
		int iteration = 5000;
		
		Return p = new Return(market);
		System.out.println(String.format("%.2f after Best %d years: ", startDollar, yearRange));
		Investment inv = new Investment();
		inv.setPeriod(yearRange);
		inv.setStartAmount(startDollar);
		inv.setAdditionalAmount(addDollar);
		ArrayList<MarketData> best = p.getBestReturn(inv);
		printData(best);
		
		System.out.println("");
		System.out.println(String.format("%.2f after Worst %d years: ", startDollar, yearRange));
		inv = new Investment();
		inv.setPeriod(yearRange);
		inv.setStartAmount(startDollar);
		inv.setAdditionalAmount(addDollar);
		ArrayList<MarketData> worst = p.getWorstReturn(inv);
		printData(worst);
		
		System.out.println("");
		System.out.println(String.format("%.2f after every %d years: ", startDollar, yearRange));
		inv = new Investment();
		inv.setPeriod(yearRange);
		inv.setStartAmount(startDollar);
		inv.setAdditionalAmount(addDollar);
		ArrayList<MarketData> result = p.getReturn(inv);
		printData(result);
		
		/*
		System.out.println("");
		Simulation simul = new Simulation(yearRange, startDollar, addDollar, market);
		Simulation.Stat stat = simul.simulate(iteration);
		System.out.println(String.format("Median return $%.2f if $%.2f invested for %d years (%d iterations): ", 
				stat.getMedian(), startDollar, yearRange, iteration));
		System.out.println(String.format("highest return: %.2f", stat.getMax()));
		System.out.println(String.format("lowest return: %.2f", stat.getMin()));
		System.out.println(String.format("mean return: %.2f", stat.getMean()));
		System.out.println(String.format("average return: %.2f%%", ReturnData.getCAGR(startDollar, 
				(float)stat.getMedian(), yearRange)));
		System.out.println(String.format("SD: %.2f", stat.getStandardDeviation()));
		//ArrayList<Return> result = simul.getSimulatedData();
		//printData(result);
		*/
		
		/*
		System.out.println(String.format("$%.2f invested for %d years", 
				startDollar, yearRange));
		System.out.println(String.format("#, Median return"));
		for (int iter = 0; iter < iteration; iter++) {
			stat = simul.simulate(iter + 1);
			System.out.println(String.format("%d, $%.2f ", iter + 1, stat.getMedian()));
		}
		*/
	}
	
	private static void printData(ArrayList<MarketData> d) {
		System.out.println(String.format("Year, Return"));
		for (int cnt = 0; cnt < d.size(); cnt++) {
			MarketData r = d.get(cnt);
			System.out.println(String.format("%d, %.2f%%, %.2f", r.getYear(), r.getChange(), r.getEndDollar()));
		}
	}
	
	private static void printUsage() {
		String usage = "This program takes the full path to the Review CSV file and "
				+ "takes another path to write the output. The CSV file must have "
				+ "the column headers in the first line."
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Usage: java -jar GenerateReviewSQL <path_to_CSV> <path to output>";
		
		System.out.println("<Usage here>");
	}
}
