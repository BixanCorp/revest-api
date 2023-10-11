/**
 * 
 */
package com.bixan.revest.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bixan.revest.core.Constant.MarketIndex;
import com.bixan.revest.data.Investment;
import com.bixan.revest.data.MarketData;
import com.bixan.revest.market.Return;
/**
 * @author sambit.bixan@gmail.com
 *
 * Investment object fields
	 * <code>
	 * 	"period": "15"
	 * 	"currency": "USD"
	 * 	"startAmount": "100000.00"
	 * 	"additionalAmount": "5000.00"
	 * 	"periodType": "Month"
	 * 	"startDepositPeriod": "0"
	 * 	"endDepositPeriod": "10"
	 * </code>
	 * @param num
	 * @param market
	 * @return
 */
@RestController
@RequestMapping("/account")
@CrossOrigin
public class Scenario {
	@GetMapping(value = "/best/{num}/{market:.*}", produces = "application/json")
	public String bestYears(@PathVariable("num") int num, @PathVariable("market") String market) throws IOException {
		MarketIndex mkt = getMarket(market);
		
		JSONObject json = new JSONObject();
		
		Return p = new Return(mkt);
		ArrayList<MarketData> best = p.getBestYears(num);
		
		json.put("market", mkt.toString());
		json.put("best", jsonifyReturnList(best));
		
		return json.toString();
	}
	
	@GetMapping(value = "/worst/{num}/{market:.*}", produces = "application/json")
	public String worstYears(@PathVariable("num") int num, @PathVariable("market") String market) throws IOException {
		MarketIndex mkt = getMarket(market);
		
		JSONObject json = new JSONObject();
		
		Return p = new Return(mkt);
		ArrayList<MarketData> worst = p.getWorstYears(num);
		
		json.put("market", mkt.toString());
		json.put("worst", jsonifyReturnList(worst));
		
		return json.toString();
	}
	
	@PostMapping(value = "/bestperiod/{market:.*}", 
			consumes = "application/json",
			produces = "application/json")
	public String bestPeriod(@PathVariable("market") String market, @RequestBody Investment inv) throws IOException {		
		MarketIndex mkt = getMarket(market);
		
		JSONObject json = new JSONObject();
		
		Return p = new Return(mkt);
		ArrayList<MarketData> best = p.getBestReturn(inv);
		
		json.put("market", mkt.toString());
		json.put("investment", inv.toJson());
		json.put("best_period", jsonifyReturnList(best));
		
		return json.toString();
	}
	
	@PostMapping(value = "/worstperiod/{market:.*}",
			consumes = "application/json",
			produces = "application/json")
	public String worstPeriod(@PathVariable("market") String market, @RequestBody Investment inv) throws IOException {		
		MarketIndex mkt = getMarket(market);
		
		JSONObject json = new JSONObject();
		
		Return p = new Return(mkt);
		ArrayList<MarketData> worst = p.getWorstReturn(inv);
		
		json.put("market", mkt.toString());
		json.put("investment", inv.toJson());
		json.put("worst_period", jsonifyReturnList(worst));
		
		return json.toString();
	}
	
	private static MarketIndex getMarket(String market) {
		MarketIndex mkt = MarketIndex.SP500;
		
		switch(market.toLowerCase()) {
		case "russel3000":
		case "russel3k":
			mkt = MarketIndex.RUSSELL3000;
			break;
		case "russel2000":
		case "russel2k":
			mkt = MarketIndex.RUSSELL2000;
			break;
		case "dow":
		case "dowjones":
			mkt = MarketIndex.DOWJONES;
			break;
		case "sp":
		case "s&p":
		case "s&p500":
		case "sp500":
		default:
			mkt = MarketIndex.SP500;
		}
		
		return mkt;
	}
	
	private static JSONArray jsonifyReturnList(ArrayList<MarketData> list) {
		JSONArray arr = new JSONArray();
		
		if (null != list) {
			Iterator<MarketData> it = list.iterator();
			while (it.hasNext()) {
				MarketData r = it.next();
				JSONObject o = new JSONObject();
				o.put("Year", r.getYear());
				o.put("Return", r.getChange());
				o.put("endAmount", r.getEndDollar());
				arr.put(o);
			}
		}
		
		
		return arr;
	}
}
