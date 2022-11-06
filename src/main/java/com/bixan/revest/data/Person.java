/**
 * 
 */
package com.bixan.revest.data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author sambit
 *
 */
public class Person {
	private HouseholdRetirement house = null;
	private String name = null;
	private String dob = null;
	private OffsetDateTime odtDob = null;
	private int retireAge = 0;
	private double retireWithdraw = 0.00d;
	
	public Person() {
		// no-argument constructor
	}
	
	/**
	 * Copy constructor
	 * @param per
	 */
	public Person(Person per) {
		this.setHouseholdRetirement(per.house);
		this.setName(per.getName());
		this.setDob(per.getDob());
		this.setRetireAge(per.getRetireAge());
		this.setRetireWithdraw(per.getRetireWithdraw());
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
	 * @return the dob
	 */
	public String getDob() {
		return dob;
	}
	/**
	 * @param dob the dob to set
	 */
	public void setDob(String dob) {
		this.odtDob = OffsetDateTime.parse(dob);
		this.dob = dob;
	}
	/**
	 * @return the retireAge
	 */
	public int getRetireAge() {
		return retireAge;
	}
	/**
	 * @param retireAge the retireAge to set
	 */
	public void setRetireAge(int retireAge) {
		this.retireAge = retireAge;
	}
	/**
	 * @return the retireWithdraw
	 */
	public double getRetireWithdraw() {
		return retireWithdraw;
	}
	/**
	 * @param retireWithdraw the retireWithdraw to set
	 */
	public void setRetireWithdraw(double retireWithdraw) {
		this.retireWithdraw = retireWithdraw;
	}
	
	/**
	 * Returns age at <code>at</code>
	 * @param at
	 * @return
	 */
	public int getAge(OffsetDateTime at) {
		return at.getYear() - odtDob.getYear();
	}
	
	/**
	 * Returns age at <code>at</code>
	 * @param at
	 * @return
	 */
	public int getAge(int at) {
		return at - odtDob.getYear();
	}
}
