/**
 * 
 */
package com.bixan.revest.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bixan.revest.util.FileUtil;

/**
 * @author sambit.bixan@gmail.com
 *
 */
@Component
public class Inflation {
	private static final String YEAR_HEADER_NAME = "YEAR";
	private static final String RETURN_HEADER_NAME = "INFLATION";
	
	private static final Hashtable<DATA_TYPE, ArrayList<MarketData>> DATA_TABLE = new Hashtable<DATA_TYPE, ArrayList<MarketData>>();
	
	public enum DATA_TYPE {
		INFLATION
	};
	
	private static int yearIdx = -1;
	private static int inflationIdx = -1;
	private ArrayList<MarketData> data = new ArrayList<MarketData>();
	
	@Autowired
	FileUtil fileUtil;
	
	private Inflation() {
		
	}
	
	private static void loadData(DATA_TYPE dataKey, String csvPath) {
		File f = new File(csvPath);
		if (!f.exists() || !f.isFile()) {
			System.err.println(String.format("Cannot access %s", csvPath));
			System.exit(1);
		}
		
		CSVParser csv = null;
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
		
		ArrayList<MarketData> table = iterate(csv);
		if (null != table) {
			DATA_TABLE.put(dataKey, table);
		}
		
		if (null != csv && !csv.isClosed()) {
			try {
				csv.close();
			} catch (IOException e) {
				System.err.println("Could not close CSV parser. Ignoring");
			}
			csv = null;
		}
	}
	
	private static boolean isValid(Map<String, Integer> headerMap) {
		Set<String> keys = headerMap.keySet();
		Iterator<String> it = keys.iterator();
		boolean yearFound = false;
		boolean returnFound = false;
		while (it.hasNext()) {
			String k = it.next();
			if (k.toUpperCase().equals(YEAR_HEADER_NAME)) {
				yearFound = true;
				yearIdx = headerMap.get(k);
			}
			
			if (k.toUpperCase().equals(RETURN_HEADER_NAME)) {
				returnFound = true;
				inflationIdx = headerMap.get(k);
			}
		}
		
		return yearFound && returnFound;
	}
	
	private static ArrayList<MarketData> iterate(CSVParser csv) {
		Iterator<CSVRecord> iter = csv.iterator();
		int nCnt = 0;
		int line = 0;
		ArrayList<MarketData> table = new ArrayList<MarketData>();
		
		while (iter.hasNext()) {
			CSVRecord record = iter.next();
			line++;
			String returnStr = record.get(inflationIdx);
			float returnVal = 0/0f;
			int year = -1;
			if (returnStr.endsWith("%")) {
				int percentPos = returnStr.indexOf("%");
				returnVal = Float.parseFloat(returnStr.substring(0, percentPos));
			}
			
			try {
				year = Integer.parseInt(record.get(yearIdx));
			}
			catch(NumberFormatException ex) {
				System.err.println(String.format("Cannot read id at line %d", line + 1));
				continue;
			}
			
			MarketData d = new MarketData();
			d.setYear(year);
			d.setChange(returnVal);
			d.setEndDollar(0);
			table.add(d);
			nCnt++;
		}
		
		return table;
	}
	
	public ArrayList<MarketData> getData(DATA_TYPE dataType) throws IOException {
		ArrayList<MarketData> data = DATA_TABLE.get(dataType);
		
		if (null == data) {
			//loadData(dataType, Configurator.getDataPath() + File.separator + dataType.toString().toLowerCase() + ".csv");
			
			File csv = fileUtil.getFileFromResource(dataType.toString().toLowerCase() + ".csv");
			loadData(dataType, csv.getAbsolutePath());
		}
		
		return DATA_TABLE.get(dataType);
	}
}
