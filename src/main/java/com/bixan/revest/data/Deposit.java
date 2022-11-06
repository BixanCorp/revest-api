/**
 * 
 */
package com.bixan.revest.data;

/**
 * @author sambit
 *
 */
public class Deposit {
	private HouseholdRetirement house = null;
	private String name = null;
	private int year = 0;
	private String accountName = null;
	private double amount = 0d;
	
	// no-argument constructor
	public Deposit() {
		
	}
	/**
	 * Copy constructor
	 * @param dep
	 */
	public Deposit(Deposit dep) {
		this.setHouseholdRetirement(dep.house);
		this.setName(dep.getName());
		this.setYear(dep.getYear());
		this.setAccountName(dep.getAccountName());
		this.setAmount(dep.getAmount());
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
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}
	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
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
	
	public Account getAccount() {
		return house.getAccountByName(this.accountName);
	}
}
