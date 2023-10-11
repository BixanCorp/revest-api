/**
 * 
 */
package com.bixan.revest.market.simulation;


import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bixan.revest.concurrent.Pool;
import com.bixan.revest.data.DataException;
import com.bixan.revest.data.HouseholdRetirement;

import lombok.NoArgsConstructor;

/**
 * @author sambit.bixan@gmail.com
 *
 */
@NoArgsConstructor
@Service
public class Simulation {
	@Autowired
	Pool pool;
	
	@Autowired
	HouseholdRetirement household;
	
	private HouseholdRetirement origHouse;
	private Object lock = new Object();
	
	/*
	public Simulation(HouseholdRetirement house) {
		this.origHouse = house;
		house.getRate().setPolicy("random");
	}
	*/
	
	public void setHouseholdRetirement(HouseholdRetirement house) {
		this.origHouse = house;
		house.getRate().setPolicy("random");
	}
	
	public SimulationRun[] simulate(int iteration) throws DataException, InterruptedException {
		BlockingQueue<Runnable> taskQueue = pool.getTaskQueue();
		SimulationRun[] runs = new SimulationRun[iteration];
		SimulationTask[] tasks = new SimulationTask[iteration];
		boolean isComplete = true;
		
		for (int i = 0; i < iteration; i++) {
			HouseholdRetirement house = new HouseholdRetirement(this.origHouse);
			SimulationRun sr = new SimulationRun();
			runs[i] = sr;
			sr.setRunIndex(i + 1);
			
			SimulationTask task = new SimulationTask(house, sr);
			tasks[i] = task;
			taskQueue.add(task);
			isComplete = isComplete && (task.isCompleted() || task.isAborted());
		}
		
		while (!isComplete) {
			//synchronized(lock) {
				Thread.sleep(100);
			//}
			
			isComplete = true;
			for (SimulationTask t : tasks) {
				isComplete = isComplete && (t.isCompleted() || t.isAborted());
			}
		}
		
		return runs;
	}
}
