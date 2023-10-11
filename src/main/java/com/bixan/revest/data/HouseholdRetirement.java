/**
 * 
 */
package com.bixan.revest.data;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.springframework.stereotype.Service;

import com.bixan.revest.core.Constant.RatePolicy;
import com.bixan.revest.core.Constant.Retirement;
import com.bixan.revest.data.RetirementYear.Stage;
import com.bixan.revest.stat.DailyMarket;

import lombok.NoArgsConstructor;

/**
 * @author sambit
 *
 */
@NoArgsConstructor
@Service
public class HouseholdRetirement {
	private Person[] persons = null;
	private Account[] accounts = null;
	private Rate rate = null;
	private double[] dailyRates = null;
	private Withdrawal[] withdrawals = null;
	private Deposit[] deposits = null;
	private int earlyYearsLen = 0;
	private int midYearsLen = (int)(Retirement.MIDDLE_RETIREMENT_AGELIMIT - Retirement.EARLY_RETIREMENT_AGELIMIT);
	private int advancedYearsLen = (int)(Retirement.EXPIRY_AGELIMIT - Retirement.MIDDLE_RETIREMENT_AGELIMIT);
	private boolean yearsComputed = false;
	
	/**
	 * Copy constructor
	 * @throws DataException 
	 */
	public HouseholdRetirement(HouseholdRetirement house) throws DataException {
		int idx = 0;
		if (null != house.persons) {
			Person[] localPersons = new Person[house.persons.length];
			for (Person p : house.persons) {
				localPersons[idx++] = new Person(p);
			}
			this.setPersons(localPersons);
		}
		
		idx = 0;
		if (null != house.accounts) {
			Account[] localAccounts = new Account[house.accounts.length];
			for (Account act : house.accounts) {
				localAccounts[idx++] = new Account(act);
			}
			this.setAccounts(localAccounts);
		}
		
		idx = 0;
		if (null != house.deposits) {
			Deposit[] localDeposits = new Deposit[house.deposits.length];
			for (Deposit dep : house.deposits) {
				localDeposits[idx++] = new Deposit(dep);
			}
			this.setDeposits(localDeposits);
		}
		
		idx = 0;
		if (null != house.withdrawals) {
			Withdrawal[] localWithdrawals = new Withdrawal[house.withdrawals.length];
			for (Withdrawal with : house.withdrawals) {
				localWithdrawals[idx++] = new Withdrawal(with);
			}
			this.setWithdrawals(localWithdrawals);
		}
		
		this.setRate(house.getRate());
		this.earlyYearsLen = house.earlyYearsLen;
		this.yearsComputed = house.yearsComputed;
	}
	/**
	 * @return the persons
	 */
	public Person[] getPersons() {
		return persons;
	}
	/**
	 * @param persons the persons to set
	 */
	public void setPersons(Person[] persons) {
		this.persons = persons;
		for (Person p : persons) {
			p.setHouseholdRetirement(this);
		}
	}
	/**
	 * @return the accounts
	 */
	public Account[] getAccounts() {
		return accounts;
	}
	/**
	 * @param accounts the accounts to set
	 */
	public void setAccounts(Account[] accounts) {
		this.accounts = accounts;
		for (Account a : accounts) {
			a.setHouseholdRetirement(this);
		}
	}
	/**
	 * 
	 */
	public Account getAccountByName(String name) {
		Account[] accounts = this.getAccounts();
		for (Account act : accounts) {
			if (act.getName().equalsIgnoreCase(name)) {
				return act;
			}
		}
		
		return null;
	}
	/**
	 * @return the rate
	 */
	public Rate getRate() {
		return rate;
	}
	/**
	 * @param rate the rate to set
	 */
	public void setRate(Rate rate) {
		this.rate = rate;
	}
	/**
	 * Returns array of year-wise return rates
	 * @return
	 * @throws IOException 
	 */
	public double[] getRates() throws IOException {
		this.computeYears();
		if (this.getRate().getInternalPolicyValue() == RatePolicy.RANDOM_RANDOM_RANDOM) {
			this.dailyRates = this.getRate().getPredictedDailyrates();
		}
		return this.getRate().getRates(this.earlyYearsLen, this.midYearsLen, this.advancedYearsLen);
	}
	/**
	 * @return the dailyRates
	 */
	public double[] getDailyRates() {
		return dailyRates;
	}
	/**
	 * @return the withdrawals
	 */
	public Withdrawal[] getWithdrawals() {
		return withdrawals;
	}
	/**
	 * @param withdrawals the withdrawals to set
	 */
	public void setWithdrawals(Withdrawal[] withdrawals) {
		this.withdrawals = withdrawals;
		for (Withdrawal e : withdrawals) {
			e.setHouseholdRetirement(this);
		}
	}
	
	/**
	 * @return the deposits
	 */
	public Deposit[] getDeposits() {
		return deposits;
	}
	/**
	 * @param deposits the deposits to set
	 */
	public void setDeposits(Deposit[] deposits) {
		this.deposits = deposits;
		for (Deposit d : deposits) {
			d.setHouseholdRetirement(this);
		}
	}
	/**
	 * Must be exact match, including case and spaces
	 * @param name
	 * @return
	 */
	public Person getPerson(String name) {
		for(Person p : persons) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		
		return null;
	}
	
	// * Four strategies we employ
	// * 1. Money deposited at the beginning of the year (most optimistic)
	// * 2. Money deposited at the end of the year (most conservative)
	// * 3. Money deposited at the middle of the year (most likely)
	// * 4. Money deposited equally each month (most recommended)
	public int getDepositStrategy() {
		return 4;
	}
	
	private void computeYears() {
		if (this.yearsComputed) {
			return;
		}
		
		OffsetDateTime now = OffsetDateTime.now();
		int yearToRetire = Integer.MAX_VALUE;
		int yearToMid = 0;
		int y = 0;
		
		for (Person p : persons) {
			int age = p.getAge(now.getYear());
			yearToRetire = p.getRetireAge() - age;
			yearToMid = 
					yearToMid > (y = (int)Math.ceil(yearToRetire + (Retirement.EARLY_RETIREMENT_AGELIMIT -  p.getRetireAge()))) ?
							yearToMid : y;
		}
		
		this.earlyYearsLen = yearToMid + 1; 	// +1 is to add the current year
		this.yearsComputed = true;
	}
}
