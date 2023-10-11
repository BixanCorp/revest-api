/**
 * 
 */
package com.bixan.revest.core;

import org.springframework.stereotype.Component;

/**
 * This class holds the system constants, including derived constants.
 * 
 * @author sambit.bixan@gmail.com
 *
 */
public class Constant {
	
	public static class Config {
		public static final String LOCATION = "conf.location";
		public static final String LOG4J_CONF = "log4j.conf";
		public static final String APP_CONF = "app.conf";
	}
	
	public static class ThreadPool {
		public static final String BASE_PATH = "operations.threadpool";
		public static final String CORE_SIZE_PARAM = "coresize";
		public static final String MAX_SIZE_PARAM = "maxsize";
		public static final String KEEPALIVE_PARAM = "keepalive";
		public static final int CORE_SIZE_DEFAULT = 10;
		public static final int MAX_SIZE_DEFAULT = 50;
		public static final long KEEPALIVE_DEFAULT = 30000;
	}
	
	public static enum MarketIndex {
		SP500,
		RUSSELL2000,
		DOWJONES,
		RUSSELL3000
	}
	
	
	public static enum StatDist {
		GAUSSIAN,
		LAPLACE
	}
	
	public static class Data {
		public static final String LOCATION = "/static/data/";
		public static final String SP500_DAILY_FILE = "sp500_daily.csv";
		public static final String DOWJONES_DAILY_FILE = "dowjones_daily.csv";
		public static final String RUSSELL2000_DAILY_FILE = "russell2000_daily.csv";
		public static final String RUSSELL3000_DAILY_FILE = "russell3000_daily.csv";
	}
	
	public static class Default {
		public static final String LOG4J_CONF = "log4j2.xml";
		public static final String APP_CONF = "revest.xml";
		public static final MarketIndex MARKET = MarketIndex.RUSSELL3000;
		public static final StatDist STAT_DIST = StatDist.LAPLACE;
	}
	
	public static class UserDb {
		public static final String BASE_PATH = "databases.userdb";
		public static final String SERVER_PARAM = "server";
		public static final String PORT_PARAM = "port";
		public static final String NAME_PARAM = "name";
		public static final String USER_PARAM = "user";
		public static final String PWD_PARAM = "pwd";
	}
	
	public static class Encryption {
		public static final String BASE_PATH = "security.encryption";
		public static final String VALWORD_PARAM = "validationWord";
		public static final String ALGORITHM_PARAM = "algorithm";
		public static final String ITERATION_PARAM = "keyObtentionIterations";
	}
	
	public static class Retirement {
		public static final float MIN_WITHDRAWAL_AGE = 59.5F;
		public static final float EARLY_RETIREMENT_AGELIMIT = 70.0F;
		public static final float MIDDLE_RETIREMENT_AGELIMIT = 80.0F;
		public static final float EXPIRY_AGELIMIT = 100.0F;
	}
	
	public static class RatePolicy {
		public static final int NONE = 0;
		public static final int BEST_BEST_BEST = 5;			// take best years for all segments
		public static final int BEST_BEST_WORST = 6;
		public static final int BEST_WORST_WORST = 7;
		public static final int BEST_WORST_BEST = 8;
		public static final int WORST_WORST_WORST = 15;		// take worst years for all segments
		public static final int WORST_WORST_BEST = 16;
		public static final int WORST_BEST_BEST = 17;
		public static final int WORST_BEST_WORST = 18;
		public static final int RANDOM_RANDOM_RANDOM = 25;		// take random consecutive years for all segments
		public static final int AVERAGE = 30;					// take random consecutive years for all segments
		public static final int CUSTOM = 99;
	}
	
	public static class SimulationConf {
		public static final int DEFAULT_RUN_COUNT = 500;
		public static final int DAYS_IN_YEAR = 252;		// how many days stock-market open in an year
	}
}
