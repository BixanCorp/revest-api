/**
 * 
 */
package com.bixan.revest.data;

/**
 * @author sambit
 *
 */
public class Withdrawal {
	private HouseholdRetirement house = null;
	private String name = null;
	private int year = 0;
	private double amount = 0d;
	
	// no-argument constructor
	public Withdrawal() {
		
	}
	/**
	 * Copy constrcutor
	 * @param with
	 */
	public Withdrawal(Withdrawal with) {
		this.setHouseholdRetirement(with.house);
		this.setName(with.getName());
		this.setYear(with.getYear());
		this.setAmount(with.getAmount());
	}
	public void setHouseholdRetirement(HouseholdRetirement house) {
		this.house = house;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
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
	 * @return the amount
	 */
	public double getAmount() {
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}
}
