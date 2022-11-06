/**
 * 
 */
package com.bixan.revest.data;

/**
 * @author sambit.bixan@gmail.com
 *
 */
public class MarketData {
	private int year = 0;
	private double endDollar = 0.00D;
	private double change = 0.00D;
	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}
	/**
	 * @return the endDollar
	 */
	public double getEndDollar() {
		return endDollar;
	}
	/**
	 * @param endDollar the endDollar to set
	 */
	public void setEndDollar(double endDollar) {
		this.endDollar = endDollar;
	}
	/**
	 * @return the change
	 */
	public double getChange() {
		return change;
	}
	/**
	 * @param change the change to set
	 */
	public void setChange(double change) {
		this.change = change;
	}
}
