/**
 * 
 */
package com.bixan.revest.data;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.bixan.revest.core.Constant.Retirement;

import lombok.NoArgsConstructor;

/**
 * @author sambit
 *
 */
@Component
@NoArgsConstructor
public class RetirementYear {
	enum Stage {
		WORKING_YEAR,
		EARLY_YEAR,
		MID_YEAR,
		ADVANCED_YEAR;
	}
	
	private RetirementYear prev = null;
	private RetirementYear next = null;
	private OffsetDateTime year = null;
	private HouseholdRetirement householdRetirement = null;
	private ArrayList<AccountYear> accountYears = new ArrayList<AccountYear>();
	private ArrayList<AccountYear> withdrawableAccounts = new ArrayList<AccountYear>();
	private ArrayList<AccountYear> holdingAccounts = new ArrayList<AccountYear>();
	private double totalTargetWithdrawal = 0.00D;
	private double totalActualWithdrawn = 0.00D;
	private double totalBeginningBalance = 0.00D;
	private double totalEndingBalance = 0.00D;
	private double totalDeposit = 0.00D;
	private OffsetDateTime startDate = null;
	private double[] rates = null;
	private double rate = 0.00D;
	int yearIdx = 0;
	
	public RetirementYear(HouseholdRetirement house) throws DataException, IOException {
	//public void setHouseHoldRetirement(HouseholdRetirement house) throws DataException, IOException {
		this.householdRetirement = house;
		startDate = OffsetDateTime.now();
		this.setYear(startDate);
		this.rates = house.getRates();
		this.yearIdx = this.getYear().getYear() - startDate.getYear();
		this.rate = this.rates[this.yearIdx] / 12;
		Account[] accounts = house.getAccounts();
		for (Account act : accounts) {
			AccountYear actYr = new AccountYear(act, this.year.getYear());
			accountYears.add(actYr);
			totalBeginningBalance += actYr.getBeginBalance();
		}
		
		init();
		update();
	}
	
	public RetirementYear(RetirementYear prev) throws DataException {
	//public void setRetirementYear(RetirementYear prev) throws DataException {
		this.prev = prev;
		this.rates = prev.rates;
		this.startDate = prev.startDate;
		this.yearIdx = prev.yearIdx + 1;
		this.rate = this.rates[this.yearIdx] / 12;
		this.setYear(prev.getYear().plusYears(1));
		this.setHouseholdRetirement(prev.getHousehoseRetirement());
		this.prev.setNext(this);
		
		ArrayList<AccountYear> actYrs = this.prev.getAccountYears();
		for (AccountYear actYr : actYrs) {
			AccountYear newActYr = new AccountYear(actYr);
			this.accountYears.add(newActYr);
			totalBeginningBalance += newActYr.getBeginBalance();
		}
		
		update();
	}
	
	@PostConstruct
	private void init() throws DataException {
		// set the total withdrawal value
		for (AccountYear actYr : this.accountYears) {
			this.updateAccountYear(actYr);
		}
	}
	
	private void update() throws DataException {
		/**
		 * 1. Loop through each AccountYear
		 * 2. In the loop
		 *    a) Determine if the account is retirement account
		 *    b) Who owns the account
		 *    c) based on (b) if it is early year, mid year or late year
		 *    d) based on (c)select rate 
		 *    e) based on (a) select withdrawal
		 */
		for (AccountYear actYr : this.accountYears) {
			this.updateAccountYear(actYr);
		}
		
		this.calculateWithdrawal();
		this.makeWithdrawal();
	}
	
	private void updateAccountYear(AccountYear actYr) throws DataException {
		boolean isRetirement = actYr.getAccount().isRetirement();
		
		// get owner's age
		int age = 0;
		
		if (isRetirement) {
			age = actYr.getAccount().getBelongsTo().getAge(actYr.getYear());
		}
		
		if (isRetirement && age > Retirement.MIN_WITHDRAWAL_AGE) {
			withdrawableAccounts.add(actYr);
			actYr.setCanWithdraw(true);
		}
		else if (!isRetirement) {
			withdrawableAccounts.add(actYr);
			actYr.setCanWithdraw(true);
		}
		else {
			holdingAccounts.add(actYr);
			actYr.setCanWithdraw(false);
		}
	}
	
	// assume deposit happens at the end of the year
	private void updateDeposit() {
		// other withdrawals
		Deposit[] deps = this.householdRetirement.getDeposits();
		if (null == deps) {
			return;
		}
		
		for (Deposit d : deps) {
			if (this.year.getYear() == d.getYear()) {
				Account act = d.getAccount();
				for (AccountYear ay : this.accountYears) {
					if (ay.getAccount() == act) {
						this.totalDeposit += d.getAmount();
						ay.addDeposit(d.getAmount());
						ay.setRunningBalance(ay.getRunningBalance() + d.getAmount());
					}
				}
			}
		}
	}
	
	private void calculateWithdrawal() throws DataException {
		int age = 0;
		double inflation = this.householdRetirement.getRate().getInflation();
		// first try to see previous years withdrawal		
		Person[] persons = this.householdRetirement.getPersons();
		
		for (Person p : persons) {
			age = p.getAge(year);
			int at = age - p.getRetireAge();
			
			if (at == 0) {
				// withdrawal starts
				this.totalTargetWithdrawal += p.getRetireWithdraw();
			}
			else if (at > 0){
				this.totalTargetWithdrawal += p.getRetireWithdraw() * Math.pow((1 + inflation), at);
			}
		}
		
		// other withdrawals
		Withdrawal[] ears = this.householdRetirement.getWithdrawals();
		for (Withdrawal e : ears) {
			if (this.year.getYear() == e.getYear()) {
				this.totalTargetWithdrawal += e.getAmount();
			}
		}
	}
	
	// *** We assume that the withdrawal happens at the beginning of the year ***
	private void makeWithdrawal() {
		// by now, we have the total withdrawal for the year
		// We divide withdrawal equally from all accounts as long as there is enough money
		// If there is not enough money, we divide rest of the withdrawal from other 
		// accounts
		// sort the withdrableAccounts in ascending balance
		boolean accountTouched = true;
		withdrawableAccounts.sort(new AccountYearComparator());
		
		while (this.totalTargetWithdrawal - totalActualWithdrawn > 1 && accountTouched) {
			accountTouched = false;
			double withdrawAmount = (this.totalTargetWithdrawal - totalActualWithdrawn) / this.withdrawableAccounts.size();
			for (int i = 0; i < this.withdrawableAccounts.size(); i++) {
				AccountYear ay = this.withdrawableAccounts.get(i);
				if (ay.getAccount().getBalance() > withdrawAmount) {
					totalActualWithdrawn += withdrawAmount;
					ay.setWithdrawal(ay.getWithdrawal() + withdrawAmount);
					ay.setRunningBalance(ay.getAccount().getBalance() - withdrawAmount);
					ay.getAccount().setBalance(ay.getAccount().getBalance() - withdrawAmount);
					accountTouched = true;
				}
				else {
					double actual = ay.getAccount().getBalance();
					totalActualWithdrawn += actual;
					ay.setWithdrawal(ay.getWithdrawal() + actual);
					ay.setRunningBalance(0.00D);
					ay.getAccount().setBalance(0.00D);
					
					if (actual > 0) {
						accountTouched = true;
					}
					
					// need to recalculate withdrawAmount
					withdrawableAccounts.remove(ay);
					i--;
				}
			}
		}
		
		// TODO: calculate ending balance
		// * Four strategies we employ
		// * 1. Money deposited at the beginning of the year (most optimistic)
		// * 2. Money deposited at the end of the year (most conservative)
		// * 3. Money deposited at the middle of the year (most likely)
		// * 4. Money deposited equally each month (most recommended)
		
		switch (this.householdRetirement.getDepositStrategy()) {
		case 1:
			this.updateBalanceStrategy1();
			break;
		case 2:
			this.updateBalanceStrategy2();
			break;
		case 3:
			this.updateBalanceStrategy3();
			break;
		case 4:
		default:
			this.updateBalanceStrategy4();
			break;
		}
			
		// deposit happens at the end of theyear
		this.updateDeposit();
	}
	
	// * 1. Money deposited at the beginning of the year (most optimistic)
	private void updateBalanceStrategy1() {
		for (AccountYear ay : this.accountYears) {
			double beginningBalance = ay.getRunningBalance();
			double deposit = ay.getAccount().getYearlyDeposit() / 12;
			
			if (this.getRetirementStage() != Stage.WORKING_YEAR) {
				deposit = 0;
			}
			
			this.totalDeposit += deposit * 12;
			ay.addDeposit(deposit * 12);
			ay.setRunningBalance((beginningBalance + deposit) * (1 + rate));
		}
	}
	
	// * 2. Money deposited at the end of the year (most conservative)
	private void updateBalanceStrategy2() {
		for (AccountYear ay : this.accountYears) {
			double beginningBalance = ay.getRunningBalance();
			double deposit = ay.getAccount().getYearlyDeposit() / 12;

			if (this.getRetirementStage() != Stage.WORKING_YEAR) {
				deposit = 0;
			}
			
			this.totalDeposit += deposit * 12;
			ay.addDeposit(deposit * 12);
			ay.setRunningBalance(beginningBalance * (1 + rate) + deposit);
		}
	}
	
	// * 3. Money deposited at the middle of the year (most likely)
	private void updateBalanceStrategy3() {
		for (AccountYear ay : this.accountYears) {
			double beginningBalance = ay.getRunningBalance();
			double deposit = ay.getAccount().getYearlyDeposit() / 12;

			if (this.getRetirementStage() != Stage.WORKING_YEAR) {
				deposit = 0;
			}
			
			this.totalDeposit += deposit * 12;
			ay.addDeposit(deposit * 12);
			ay.setRunningBalance(beginningBalance * (1 + rate) + (deposit * (1 + rate / 2)));
		}
	}
	
	// * 4. Money deposited equally each month (most recommended)
	private void updateBalanceStrategy4() {
		for (AccountYear ay : this.accountYears) {
			double beginningBalance = ay.getRunningBalance();
			double deposit = ay.getAccount().getYearlyDeposit() / 12;
			double endBalance = beginningBalance;
			
			if (this.getRetirementStage() != Stage.WORKING_YEAR) {
				deposit = 0;
			}
			
			for (int m = 0; m < 12; m++) {
				endBalance = (endBalance + deposit) * (1 + rate);
			}
			
			this.totalDeposit += deposit * 12;
			ay.addDeposit(deposit * 12);
			ay.setRunningBalance(endBalance);
		}
	}
	
	public Stage getRetirementStage() {
		Person[] persons = this.householdRetirement.getPersons();
		Person eldest = persons[0];
		int maxAge = eldest.getAge(this.year);
		int a = 0;
		for (int i = 1; i < persons.length; i++) {
			Person p = persons[i];
			if ((a = p.getAge(this.year)) > maxAge) {
				eldest = p;
				maxAge = a;
			}
		}
		
		if (maxAge <= eldest.getRetireAge()) {
			return Stage.WORKING_YEAR;
		}
		else if (maxAge < Retirement.EARLY_RETIREMENT_AGELIMIT) {
			return Stage.EARLY_YEAR;
		}
		else if (maxAge < Retirement.MIDDLE_RETIREMENT_AGELIMIT) {
			return Stage.MID_YEAR;
		}
		
		return Stage.ADVANCED_YEAR;
	}
	
	//////////////////////   Setters / Getters   /////////////////////////////////
	/**
	 * @return the prev
	 */
	public RetirementYear getPrev() {
		return prev;
	}
	/**
	 * @param prev the prev to set
	 */
	public void setPrev(RetirementYear prev) {
		this.prev = prev;
	}
	/**
	 * @return the next
	 */
	public RetirementYear getNext() {
		return next;
	}
	/**
	 * @param next the next to set
	 */
	public void setNext(RetirementYear next) {
		this.next = next;
	}
	/**
	 * @return the year
	 */
	public OffsetDateTime getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(OffsetDateTime year) {
		this.year = year;
	}
	/**
	 * @return the house
	 */
	public HouseholdRetirement getHousehoseRetirement() {
		return householdRetirement;
	}

	/**
	 * @param house the house to set
	 */
	public void setHouseholdRetirement(HouseholdRetirement house) {
		this.householdRetirement = house;
	}

	/**
	 * @return the accountYears
	 */
	public ArrayList<AccountYear> getAccountYears() {
		return accountYears;
	}

	/**
	 * @param accountYears the accountYears to set
	 */
	public void setAccountYears(ArrayList<AccountYear> accountYears) {
		this.accountYears = accountYears;
	}
	
	public double getTargetWithdrawal() {
		return this.totalTargetWithdrawal;
	}
	
	public double getActualWithdrawal() {
		return this.totalActualWithdrawn;
	}
	
	public double getBeginningBalance() {
		return this.totalBeginningBalance;
	}
	
	public double getEndingBalance() {
		ArrayList<AccountYear> actYrs = this.accountYears;
		for (AccountYear actYr : actYrs) { 
			totalEndingBalance += actYr.getEndBalance();
		}
		
		return this.totalEndingBalance;
	}
	
	public double getDeposit() {
		return this.totalDeposit;
	}
	
	public double getYearlyRate() {
		return this.rate * 12;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (AccountYear ay : this.accountYears) {
			sb.append(ay.toString()).append(System.lineSeparator());
		}
		
		return sb.toString();
	}
}
