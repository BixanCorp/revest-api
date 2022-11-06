/**
 * 
 */
package com.bixan.revest.data;

/**
 * @author sambit
 *
 */
public class Account {
	private HouseholdRetirement house = null;
	private String name = null;
	private double balance = 0.00D;
	private double yearlyDeposit = 0.00D;
	private boolean retirement = false;
	private Person belongsTo = null; 
	private String owner = null;
	
	// no-argument constructor
	public Account() {
		
	}
	/**
	 * Copy constructor
	 * @param act
	 * @throws DataException 
	 */
	public Account(Account act) throws DataException {
		this.setHouseholdRetirement(act.house);
		this.setName(act.getName());
		this.setBalance(act.getBalance());
		this.setYearlyDeposit(act.getYearlyDeposit());
		this.setRetirement(act.isRetirement());
		this.setBelongsTo(act.getBelongsTo());
		this.setOwner(act.getOwner());
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
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}
	/**
	 * @param balance the balance to set
	 */
	public void setBalance(double balance) {
		this.balance = balance;
	}
	/**
	 * @return the yearlyDeposit
	 */
	public double getYearlyDeposit() {
		return yearlyDeposit;
	}
	/**
	 * @param yearlyDeposit the yearlyDeposit to set
	 */
	public void setYearlyDeposit(double yearlyDeposit) {
		this.yearlyDeposit = yearlyDeposit;
	}
	/**
	 * @return the retirement
	 */
	public boolean isRetirement() {
		return retirement;
	}
	/**
	 * @param retirement the retirement to set
	 */
	public void setRetirement(boolean retirement) {
		this.retirement = retirement;
	}
	/**
	 * @return the belongsTo
	 * @throws DataException 
	 */
	public Person getBelongsTo() throws DataException {
		if (null == belongsTo && null != this.owner) {
			this.setBelongsTo(house.getPerson(owner));
		}
		
		if (retirement && null == belongsTo) {
			throw new DataException(String.format("Retirement account '%s' without owner is not permitted",
					this.name));
		}
		
		return belongsTo;
	}
	/**
	 * @param belongsTo the belongsTo to set
	 */
	public void setBelongsTo(Person belongsTo) {		
		this.belongsTo = belongsTo;
	}
	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
}
