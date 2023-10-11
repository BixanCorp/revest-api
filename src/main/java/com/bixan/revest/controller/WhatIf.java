package com.bixan.revest.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bixan.revest.core.Constant.Default;
import com.bixan.revest.core.Constant.Retirement;
import com.bixan.revest.core.Constant.SimulationConf;
import com.bixan.revest.data.AccountYear;
import com.bixan.revest.data.DataException;
import com.bixan.revest.data.HouseholdRetirement;
import com.bixan.revest.data.Person;
import com.bixan.revest.data.RetirementYear;
import com.bixan.revest.market.simulation.Simulation;
import com.bixan.revest.market.simulation.SimulationRun;
import com.bixan.revest.retirement.Projection;
import com.bixan.revest.stat.DailyMarket;

/**
 * @author sambit.bixan@gmail.com
 *
 */
@RestController
@RequestMapping("/whatif")
@CrossOrigin
public class WhatIf {
	// for debug
	private File csv = null;
	private FileWriter fw = null;
	StringBuilder csvContent = null;
	
	@Autowired
	DailyMarket dailyMarket;
	
	@Autowired
	Simulation simul;
	
	private static final Logger log = LogManager.getLogger(WhatIf.class);
	/*
	Input - 
	{
		"persons": [
			            {
			            	"name": "John Doe",				// placeholder name
			            	"dob": "29836800",				// dob in UTC
			            	"retireAge": "62",				// retirement age
			            	"retireWithdraw": "100000.00";	// withdrawal at the first year of retirement
			            },
			            {
			            	"name": "Jane Doe",
			            	"dob": "84870000",
			            	"retireAge": "62",
			            	"retireWithdraw": "50000.00";	// withdrawal at the first year of retirement
			            }
		            ];
		"accounts": [
						{
							"name": "ira1",			// placeholder name
							"owner": "John Doe",
							"balance": "563045.76",	// in dollars
							"retirement": "true";		// if it is under ira rule
						},
						{
							"name": "ira2",			// placeholder name
							"owner": "Jane Doe",
							"balance": "7894007.23",// in dollars
							"retirement": "true";		// if it is under ira rule
						},
						{
							"name": "brokerage1",	// placeholder name
							"balance": "123238.53", // in dollars
							"retirement": "false";		// if it is under ira rule
						}
					];
		"rate": {
					"inflation": "3.5",		// yearly percentage
					"earlyYear": "7.5",		// yearly percentage
					"midYear": "5.5",		// yearly percentage
					"endYear": "4.5";		// yearly percentage
				};
		"withdrawals": [// any other withdrawal, assumed at the beginning of the year
						{
							"year": "2022",								// which year this withdrawal happens
							"amount": "60000.00",
							"name": "College year 1 for daughter 1";	// Placeholder 
						},
						{
							"year": "2023",								// which year this withdrawal happens
							"amount": "60000.00",
							"name": "College year 2 for daughter 1";	// Placeholder 
						}
					]
	}
	*/
	
	
	public void finalize() {
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@PostMapping(value = "/scenario", 
			consumes = "application/json",
			produces = "application/json")
	public String scenario(@RequestBody HouseholdRetirement input) throws IOException {
		JSONObject json = new JSONObject();
		
		Projection project = new Projection();
		RetirementYear ry = null;
		Person[] persons = input.getPersons();
		
		try {
			ry = project.project(input);
		} catch (DataException e) {
			log.error(e.getMessage());
		}
		
		if (log.isDebugEnabled()) {
			//log.debug(System.lineSeparator() + project.toString());
		}
		
		int lastFundedYear = 0;
		int lastFundedAge = 0;
		int age = 0;
		double finalBalance = 0.00d;
		JSONArray years = new JSONArray();
		while (null != ry) {
			JSONObject yearData = new JSONObject();
			years.put(yearData);
			int year = ry.getYear().getYear();
			yearData.put("year", year);
			JSONArray pArr = new JSONArray();
			yearData.put("persons", pArr);
			for (Person p : persons) {
				JSONObject pObj = new JSONObject();
				pObj.put("name", p.getName());
				pObj.put("age", age = p.getAge(year));
				pArr.put(pObj);
			}
			
			if (0 == lastFundedYear && 0 == ry.getEndingBalance()) {
				lastFundedYear = year - 1;
				lastFundedAge = age - 1;
			}
			
			JSONObject summary = new JSONObject();
			summary.put("beginBalance", ry.getBeginningBalance());
			summary.put("endBalance", finalBalance = ry.getEndingBalance());
			summary.put("targetWithdrawal", ry.getTargetWithdrawal());
			summary.put("actualWithdrawal", ry.getActualWithdrawal());
			summary.put("deposit", ry.getDeposit());
			summary.put("rate", ry.getYearlyRate());
			yearData.put("summary", summary);
			
			ArrayList<AccountYear> acctYears = ry.getAccountYears();
			JSONArray acctArray = new JSONArray();
			yearData.put("accounts", acctArray);
			for (AccountYear ay : acctYears) {
				JSONObject acctData = new JSONObject();
				acctArray.put(acctData);
				acctData.put("name", ay.getAccount().getName());
				acctData.put("beginBalance", String.format("%.2f", ay.getBeginBalance()));
				acctData.put("withdrawal", String.format("%.2f", ay.getWithdrawal()));
				acctData.put("deposit", String.format("%.2f", ay.getDeposit()));
				acctData.put("endBalance", String.format("%.2f", ay.getEndBalance()));
			}
			
			ry = ry.getNext();
		}
		
		JSONObject meta = new JSONObject();
		meta.put("lastFundedYear", lastFundedYear);
		meta.put("lastFundedAge", lastFundedAge);
		meta.put("finalBalance", String.format("%.2f", finalBalance));
		meta.put("maxAge", (int)Retirement.EXPIRY_AGELIMIT);
		
		json.put("meta", meta);
		json.put("years", years);
		
		return json.toString();
	}
	
	@PostMapping(value = "/simulate",
			consumes = "application/json",
			produces = "application/json")
	public String simulate(@RequestBody HouseholdRetirement input) throws IOException {
		JSONObject json = new JSONObject();
				
		//Simulation simul = new Simulation(input);
		simul.setHouseholdRetirement(input);
		SimulationRun[] runs = null;
		
		try {
			runs = simul.simulate(SimulationConf.DEFAULT_RUN_COUNT);
		} catch (DataException | InterruptedException e) {
			log.error(e.getMessage());
		}
		
		if (log.isDebugEnabled()) {
			//log.debug(System.lineSeparator() + project.toString());
		}
		
		DailyMarket dm = dailyMarket.getDailyMarket(Default.MARKET);
		//createRateCsv();
		int success = 0;
		JSONArray runArr = new JSONArray();
		for (int i = 0; i < runs.length; i++) {
			SimulationRun sr = runs[i];
			JSONObject jRun = new JSONObject();
			jRun.put("index", sr.getRunIndex());
			jRun.put("fundedYear", sr.getLastWithdrawYearIndex());
			jRun.put("endBalance", String.format("%.2f", sr.getEndBalance()));
			jRun.put("averageRate", sr.getAverageRate());
			
			/*
			printSimulations(sr.getRunIndex(), sr.getLastWithdrawYearIndex(), sr.getEndBalance(), 
					sr.getMeanPredictedDailyRate(), dm.getMeanReturn(), sr.getFitnessMeasure(),
					sr.getFinalPriceCloseness(), (i == runs.length - 1));
			*/
			if (sr.getEndBalance() > 0) {
				success++;
			}
			
			runArr.put(jRun);
		}
		
		JSONObject meta = new JSONObject();
		meta.put("run", SimulationConf.DEFAULT_RUN_COUNT);
		meta.put("market", Default.MARKET);
		meta.put("marketAverage", dm.getMeanReturn() * SimulationConf.DAYS_IN_YEAR);
		meta.put("success", success);
		meta.put("maxAge", (int)Retirement.EXPIRY_AGELIMIT);
		
		json.put("meta", meta);
		json.put("runs", runArr);
		
		return json.toString();
	}
	
	private void printSimulations(int run, int fundedYear, double endBalance, 
			double averageRate, double meanMarketReturn, double fitness, double closeness, boolean isFinalCall) {	
		if (null == csv || null == fw) {
			log.error("Cannot write to file");
			return;
		}
		
		csvContent.append(run).append(",")
		.append(fundedYear).append(",")
		.append(String.format("%.2f", endBalance)).append(",")
		.append(String.format("%.5f", averageRate * SimulationConf.DAYS_IN_YEAR)).append(",")
		.append(String.format("%.5f", meanMarketReturn * SimulationConf.DAYS_IN_YEAR)).append(",")
		.append(String.format("%.2f%%", fitness * 100)).append(",")
		.append(String.format("%.2f%%", closeness * 100)).append(",")
		.append(System.lineSeparator());
		
		if (csvContent.length() >= 512 || isFinalCall) {
			try {
				fw.append(csvContent.toString());
			} catch (IOException e) {
				log.error(e.getMessage());
				return;
			}
			csvContent = new StringBuilder();
		}
		
		try {
			fw.flush();
		} catch (IOException e) {
			// ignore
		}
	}
	
	private void createRateCsv() {		
		this.csv = new File("/tmp/simulatedRate_" + Default.MARKET + "_" + 
				String.valueOf(System.currentTimeMillis()).substring(9) + ".csv");
		if (csv.exists()) {
			csv.delete();
		}
			
		try {
			this.csv.createNewFile();
			this.fw = new FileWriter(csv);
			csvContent = new StringBuilder();
			
			csvContent.append("run, fundedYear, endBalance, averageRate, actualRate, fitness, last price closeness")
			.append(System.lineSeparator());
		}
		catch(IOException ex) {
			log.error(ex.getMessage());
			if (csv.exists()) {
				csv.delete();
			}
			
			csv = null;
			fw = null;
		}
	}
}
