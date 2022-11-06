/**
 * 
 */
package com.bixan.revest.market;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import com.bixan.revest.core.Constant.MarketIndex;
import com.bixan.revest.data.Inflation;
import com.bixan.revest.data.Investment;
import com.bixan.revest.data.Market;
import com.bixan.revest.data.MarketData;

/**
 * @author sambit.bixan@gmail.com
 *
 */
public class Fund {
	private ArrayList<MarketData> data = null;
	private ArrayList<MarketData> inflationList = null;
	
	@Autowired
	Market market;
	
	@Autowired
	Inflation inflation;
	
	public Fund(MarketIndex dataType) throws IOException {
		data = market.getData(dataType);
		inflationList = inflation.getData(Inflation.DATA_TYPE.INFLATION);
	}
	
	public ArrayList<Investment> getFundOverTime(Investment initInv, int yearRange, int startYear) {
		int sYear = Integer.max(Integer.max(startYear, data.get(0).getYear()), inflationList.get(0).getYear());
		
		int yRange = Integer.min(Integer.min(yearRange, (data.get(data.size() - 1).getYear() - data.get(0).getYear() + 1)),
				(inflationList.get(inflationList.size() - 1).getYear() - inflationList.get(0).getYear() + 1));
		
		Investment inv = new Investment();
		inv.setCurrency(initInv.getCurrency());
		inv.setPeriodType(initInv.getPeriodType());
		inv.setStartAmount(initInv.getStartAmount());
		inv.setAdditionalAmount(initInv.getAdditionalAmount());
		inv.setStartDepositPeriod(initInv.getStartDepositPeriod());
		for (int idx = 0; idx < yRange; idx++) {
			
		}
		
		return null;
	}
}
