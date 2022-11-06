/**
 * 
 */
package com.bixan.revest.data;

import org.json.JSONObject;

/**
 * @author sambit.bixan@gmail.com
 *
 */
public class Investment {
	private int period = 0;
	private String currency = "USD";
	private double startAmount = 0.00D;
	private double additionalAmount = 0.00D;
	private double endAmount = 0.00D;
	private String periodType = "Month";
	private int startDepositPeriod = 0;
	private int endDepositPeriod = 0;
	/**
	 * @return the period
	 */
	public int getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(int period) {
		this.period = period;
	}
	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}
	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	/**
	 * @return the startAmount
	 */
	public double getStartAmount() {
		return startAmount;
	}
	/**
	 * @param startAmount the startAmount to set
	 */
	public void setStartAmount(double startAmount) {
		this.startAmount = startAmount;
	}
	/**
	 * @return the additionaAmount
	 */
	public double getAdditionalAmount() {
		return additionalAmount;
	}
	/**
	 * @param additionaAmount the additionaAmount to set
	 */
	public void setAdditionalAmount(double additionaAmount) {
		this.additionalAmount = additionaAmount;
	}
	/**
	 * @return the startAmount
	 */
	public double getEndAmount() {
		return endAmount;
	}
	/**
	 * @param startAmount the startAmount to set
	 */
	public void setEndAmount(double endAmount) {
		this.endAmount = endAmount;
	}
	/**
	 * @return the periodType
	 */
	public String getPeriodType() {
		return periodType;
	}
	/**
	 * @param periodType the periodType to set
	 */
	public void setPeriodType(String periodType) {
		this.periodType = periodType;
	}
	/**
	 * @return the startDepositPeriod
	 */
	public int getStartDepositPeriod() {
		return startDepositPeriod;
	}
	/**
	 * @param startDepositPeriod the startDepositPeriod to set
	 */
	public void setStartDepositPeriod(int startDepositPeriod) {
		this.startDepositPeriod = startDepositPeriod;
	}
	/**
	 * @return the endDepositPeriod
	 */
	public int getEndDepositPeriod() {
		return endDepositPeriod;
	}
	/**
	 * @param endDepositPeriod the endDepositPeriod to set
	 */
	public void setEndDepositPeriod(int endDepositPeriod) {
		this.endDepositPeriod = endDepositPeriod;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		json.put("period", this.period);
		json.put("currency", this.currency);
		json.put("startAmount", this.startAmount);
		json.put("additionalAmount", this.additionalAmount);
		json.put("periodType", this.periodType);
		json.put("startDepositPeriod", this.startDepositPeriod);
		json.put("endDepositPeriod", this.endDepositPeriod);
		
		return json;
	}
}
