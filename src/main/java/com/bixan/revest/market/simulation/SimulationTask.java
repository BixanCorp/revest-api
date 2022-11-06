/**
 * 
 */
package com.bixan.revest.market.simulation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bixan.revest.core.Constant.Default;
import com.bixan.revest.data.DataException;
import com.bixan.revest.data.HouseholdRetirement;
import com.bixan.revest.data.RetirementYear;
import com.bixan.revest.retirement.Projection;
import com.bixan.revest.stat.DailyMarket;

/**
 * @author sambit
 *
 */
public class SimulationTask implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(SimulationTask.class);
	
	public enum TaskStatus {
		NOT_STARTED,
		RUNNING,
		COMPLETED,
		ABORTED,
		ERRORED
	}
	private HouseholdRetirement house = null;
	private SimulationRun simRun = null;
	private TaskStatus status = TaskStatus.NOT_STARTED;
	
	DailyMarket dailyMarket = new DailyMarket();
	
	public SimulationTask(HouseholdRetirement house, SimulationRun simRun) {
		this.house = house;
		this.simRun = simRun;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		this.status = TaskStatus.RUNNING;
		
		RetirementYear ry = null;
		Projection project = new Projection();
		
		try {
			ry = project.project(house);
		} catch (DataException | IOException e) {
			this.status = TaskStatus.ERRORED;
			log.error("Task errored: " + e.getMessage());
		}
		
		double endBalance = 0.00d;
		int lastYear = 0;
		double rate = 0.00d;
		while (ry != null) {
			lastYear++;
			endBalance = ry.getEndingBalance();
			rate += ry.getYearlyRate();
			
			if (0 == endBalance && this.simRun.getLastWithdrawYearIndex() == 0) {
				this.simRun.setLastWithdrawYearIndex(lastYear - 1);
			}
			
			ry = ry.getNext();
		}
		
		if (this.simRun.getLastWithdrawYearIndex() == 0) {
			this.simRun.setLastWithdrawYearIndex(lastYear);
		}
		this.simRun.setAverageRate(rate / lastYear + 1);
		this.simRun.setEndBalance(endBalance);
		this.simRun.setPredictedDailyRates(house.getDailyRates());
		try {
			this.simRun.setDailyMarket(dailyMarket.getDailyMarket(Default.MARKET));
		} catch (IOException e) {
			log.error("Task error: " + e.getMessage());
			this.status = TaskStatus.ERRORED;
			return;
		}

		this.status = TaskStatus.COMPLETED;
	}

	public boolean isCompleted() {
		return this.status == TaskStatus.COMPLETED;
	}
	
	public boolean isRunning() {
		return this.status == TaskStatus.RUNNING;
	}
	
	public boolean isAborted() {
		return this.status == TaskStatus.ABORTED;
	}
	
	public boolean hasStated() {
		return this.status != TaskStatus.NOT_STARTED;
	}
}
