/**
 * 
 */
package com.bixan.revest.stat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bixan.revest.core.Constant.Data;
import com.bixan.revest.core.Constant.Default;
import com.bixan.revest.core.Constant.MarketIndex;
import com.bixan.revest.core.Constant.StatDist;
import com.bixan.revest.market.SimulatedReturn;
import com.bixan.revest.util.FileUtil;

import lombok.NoArgsConstructor;

/**
 * @author sambit
 *
 */
@Component
@NoArgsConstructor
public class DailyMarket {
	private static final String DATE_HEADER_NAME = "DATE";
	private static final String PRICE_HEADER_NAME = "ADJ CLOSE";
	private static final int DEFAULT_SAMPLE_SIZE = 100;
	
	private MarketIndex mktIdx = null;
	private String dailyFileName = null;
	private CSVParser csv = null;
	
	private int sampleSize = DEFAULT_SAMPLE_SIZE;
	private int dateIdx = -1;
	private int priceIdx = -1;
	private double[] prices = null;
	private double[] sample = null;
	private double[] returns = null;
	private double meanPrice = 0.00d;
	private double meanReturn = 0.00d;
	private double mleLocation = 0.0d;	// mu
	private double mleScale = 0.0d;		// beta
	private StatDist dist = null;
	
	private static Hashtable<String, DailyMarket> mktTable = new Hashtable<String, DailyMarket>();
	
	
	private DailyMarket(MarketIndex mkt, StatDist dist) throws IOException {
		this.mktIdx = mkt;
		
		switch(mkt) {
		case SP500:
			dailyFileName = Data.SP500_DAILY_FILE;
			break;
		case DOWJONES:
			dailyFileName = Data.DOWJONES_DAILY_FILE;
			break;
		case RUSSELL2000:
			dailyFileName = Data.RUSSELL2000_DAILY_FILE;
			break;
		case RUSSELL3000:
			dailyFileName = Data.RUSSELL3000_DAILY_FILE;
			break;
		}
		
		loadAndParse();
		this.calculateMLE(dist);
	}
	
	public DailyMarket getDailyMarket(MarketIndex mkt, StatDist dist) throws IOException {
		DailyMarket dm = mktTable.get(mkt + "-" + dist);
		
		if (null == dm) {
			dm = new DailyMarket(mkt, dist);
			mktTable.put(mkt + "-" + dist, dm);
		}
		
		dm.calculateMLE(dist);
		return dm;
	}
	
	public DailyMarket getDailyMarket(MarketIndex mkt) throws IOException {
		return getDailyMarket(mkt, Default.STAT_DIST);
	}
	
	private void calculateMLE(StatDist dist) {
		doSampling();
		
		switch(dist) {
		case GAUSSIAN:
			doGaussianMLE();
			break;
		case LAPLACE:
			doLaplaceMLE();
			break;
		default:
			doDefaultMLE();
		}
	}
	
	private void loadAndParse() throws IOException {
		File csvFile = FileUtil.getFileFromResource(this.dailyFileName);
		// String csvPath = Configurator.getDataPath() + File.separatorChar + this.dailyFileName;
		String csvPath = csvFile.getAbsolutePath();
		
		File f = new File(csvPath);
		if (!f.exists() || !f.isFile()) {
			System.err.println(String.format("Cannot access %s", csvPath));
			System.exit(1);
		}
		
		try {
			csv = CSVParser.parse(f, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames());
		} catch (IOException e) {
			System.err.println(String.format("Cannot read CSV file %s", csvPath));
			e.printStackTrace();
			System.exit(-1);
		}
		
		Map<String, Integer> headerMap = csv.getHeaderMap();		
		if (null == headerMap) {
			System.err.println(String.format("Could not read the header map for %s", csvPath));
			System.exit(1);
		}
		
		if (!isValid(headerMap)) {
			System.err.println(String.format("The file %s is not valid", csvPath));
			System.exit(1);
		}
		
		iterate(csv);
		
		if (null != csv && !csv.isClosed()) {
			try {
				csv.close();
			} catch (IOException e) {
				System.err.println("Could not close CSV parser. Ignoring");
			}
			csv = null;
		}
	}
	
	private boolean isValid(Map<String, Integer> headerMap) {
		Set<String> keys = headerMap.keySet();
		Iterator<String> it = keys.iterator();
		boolean yearFound = false;
		boolean returnFound = false;
		while (it.hasNext()) {
			String k = it.next();
			if (k.toUpperCase().equals(DATE_HEADER_NAME)) {
				yearFound = true;
				dateIdx = headerMap.get(k);
			}
			
			if (k.toUpperCase().equals(PRICE_HEADER_NAME)) {
				returnFound = true;
				priceIdx = headerMap.get(k);
			}
		}
		
		return yearFound && returnFound;
	}
	
	private void iterate(CSVParser csv) {
		Iterator<CSVRecord> iter = csv.iterator();
		Vector<Double> priceTable = new Vector<Double>();
		Vector<Double> returnTable = new Vector<Double>();
		double totalPrice = 0.00d;
		double totalReturn = 0.00d;
		double prevPrice = 0.00d;
		
		
		while (iter.hasNext()) {
			CSVRecord record = iter.next();
			String priceStr = record.get(priceIdx);
			double priceVal = 0.00d;
			
			try {
				priceVal = Double.parseDouble(priceStr);
			}
			catch(RuntimeException ex) {
				continue;
			}
			
			totalPrice += priceVal;
			double returnVal = prevPrice == 0 ? 0 : (priceVal - prevPrice) / prevPrice;
			totalReturn += returnVal;
			priceTable.add(priceVal);
			returnTable.add(returnVal);
			prevPrice = priceVal;
		}
		
		int size = priceTable.size();
		meanPrice = size == 0 ? 0 : totalPrice / size;
		meanReturn = size == 0 ? 0 : totalReturn / (size - 1);
		
		prices = new double[size];
		for (int i = 0; i < size; i++) {
			prices[i] = priceTable.get(i);
		}
		
		size = returnTable.size();
		returns = new double[size];
		for (int i = 0; i < size; i++) {
			returns[i] = returnTable.get(i);
		}
	}
	
	private void doSampling() {
		sampleSize = 100;
		JDKRandomGenerator rand = new JDKRandomGenerator((int)System.currentTimeMillis());
		sample = new double[sampleSize];
		
		int idx = 0;
		int length = prices.length;
		int bound = length - 1;
		for (int i = 0; i < sampleSize; i++) {
			idx = rand.nextInt(bound);
			while (0 == idx) {
				idx = rand.nextInt(bound);
			}
			sample[i] = (prices[(length - bound) + idx] - prices[(length - bound) + idx - 1]) / prices[(length - bound) + idx - 1];
			/*
			sample[i] = (prices[bound - i] - prices[bound - i -1]) / prices[bound - i -1];
			*/
		}
	}
	
	private void doDefaultMLE() {
		doGaussianMLE();
	}
	
	private void doGaussianMLE() {
		double mean = StatUtils.mean(sample);
		double var = StatUtils.variance(sample);
		
		mleLocation = mean;
		mleScale = var;
	}
	
	private void doLaplaceMLE() {
		double mean = StatUtils.mean(sample);
		double absSum = 0.0d;
		
		for (int i = 0; i < sampleSize; i++) {
			absSum += Math.abs(sample[i] - mean);
		}
		
		mleLocation = mean;
		mleScale = absSum / sampleSize;
	}
	
	public MarketIndex getMarket() {
		return this.mktIdx;
	}
	
	public double getLocationMLE() {
		return mleLocation;
	}
	
	public double getScaleMLE() {
		return mleScale;
	}
	
	public double[] getActualPrices() {
		return prices;
	}
	
	public double[] getActualReturns() {
		return returns;
	}
	
	public double getMeanPrice() {
		return meanPrice;
	}
	
	public double getMeanReturn() {
		return meanReturn;
	}
}
