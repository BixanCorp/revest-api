/**
 * 
 */
package com.bixan.revest.retirement;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bixan.revest.core.Constant.Retirement;
import com.bixan.revest.data.DataException;
import com.bixan.revest.data.HouseholdRetirement;
import com.bixan.revest.data.Person;
import com.bixan.revest.data.RetirementYear;

import lombok.NoArgsConstructor;

/**
 * @author sambit
 *
 */
@Component
@NoArgsConstructor
public class Projection {
	private static final Logger log = LogManager.getLogger(Projection.class); 
	RetirementYear startRetirementYear = null;
	
	public RetirementYear project(HouseholdRetirement house) throws DataException, IOException {
		Person[] persons = house.getPersons();
		
		int lowestAge = Integer.MAX_VALUE;
		for (Person p : persons) {
			lowestAge = Math.min(lowestAge, p.getAge(OffsetDateTime.now()));
		}
		
		RetirementYear startRetirementYear = new RetirementYear(house);
		RetirementYear prev = new RetirementYear(startRetirementYear);
		
		for (int loop = lowestAge + 2; loop <= Retirement.EXPIRY_AGELIMIT; loop++) {
			prev = new RetirementYear(prev);
		}
		
		return startRetirementYear;
	}
	
	public String toString() {
		RetirementYear ry = this.startRetirementYear;
		StringBuilder sb = new StringBuilder();
		
		while (null != ry) {
			sb.append(ry.toString()).append(System.lineSeparator());
			ry = ry.getNext();
		}
		
		return sb.toString();
	}
}
