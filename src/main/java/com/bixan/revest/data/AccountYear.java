/**
 * 
 */
package com.bixan.revest.data;

import java.util.Comparator;

/**
 * @author sambit
 *
 */
public class AccountYear {
	private int year = 0;
	private Account account = null;
	private double beginBalance = 0.00D;
	private double endBalance = 0.00D;
	private double runningBalance = 0.00D;
	private double withdrawal = 0.00D;
	private double deposit = 0.00D;
	private double irregularDeposit = 0.0D;
	private boolean canWithdraw = false;
	
	AccountYear(Account act, int year) {
		this.account = act;
		this.setYear(year);
		this.setBeginBalance(this.account.getBalance());
	}
	
	AccountYear(AccountYear prev) {
		this.setYear(prev.getYear() + 1);
		this.setAccount(prev.getAccount());
		this.setBeginBalance(prev.getEndBalance());
		this.account.setBalance(prev.getEndBalance());
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
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	/**
	 * @return the beginBalance
	 */
	public double getBeginBalance() {
		return beginBalance;
	}

	/**
	 * @param beginBalance the beginBalance to set
	 */
	public void setBeginBalance(double beginBalance) {
		this.beginBalance = beginBalance;
		this.setRunningBalance(beginBalance);
	}

	/**
	 * @return the endBalance
	 */
	public double getEndBalance() {
		return endBalance;
	}

	/**
	 * @param endBalance the endBalance to set
	 */
	public void setEndBalance(double endBalance) {
		this.endBalance = endBalance;
	}

	/**
	 * @return the runningBalance
	 */
	public double getRunningBalance() {
		return runningBalance;
	}

	/**
	 * @param runningBalance the runningBalance to set
	 */
	public void setRunningBalance(double runningBalance) {
		this.runningBalance = runningBalance;
		this.setEndBalance(runningBalance);
	}

	/**
	 * @return the withdrawal
	 */
	public double getWithdrawal() {
		return withdrawal;
	}

	/**
	 * @param withdrawal the withdrawal to set
	 */
	public void setWithdrawal(double withdrawal) {
		this.withdrawal = withdrawal;
	}

	/**
	 * @return the deposit
	 */
	public double getDeposit() {
		return deposit;
	}

	/**
	 * @param deposit the deposit to set
	 */
	public void addDeposit(double deposit) {
		this.deposit += deposit;
	}

	/**
	 * @return the irregularDeposit
	 */
	public double getIrregularDeposit() {
		return irregularDeposit;
	}

	/**
	 * @param irregularDeposit the irregularDeposit to set
	 */
	public void setIrregularDeposit(double irregularDeposit) {
		this.irregularDeposit = irregularDeposit;
	}

	/**
	 * @return the canWithdraw
	 */
	public boolean canWithdraw() {
		return canWithdraw;
	}

	/**
	 * @param canWithdraw the canWithdraw to set
	 */
	public void setCanWithdraw(boolean canWithdraw) {
		this.canWithdraw = canWithdraw;
	}
	
	public String toString() {
		return String.format("Year: %d, Account: %s, Beginning balance: $%.2f, Ending balance: $%.2f, "
				+ "Withdrawn amount: $%.2f", 
				this.year, this.account.getName(), this.beginBalance, this.endBalance, this.withdrawal);
	}
}

class AccountYearComparator implements Comparator<AccountYear> {

	@Override
	public int compare(AccountYear o1, AccountYear o2) {	
		return (int)(o2.getAccount().getBalance() - o1.getAccount().getBalance());
		
	}
}
