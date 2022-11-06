/**
 * 
 */
package com.bixan.revest.util;

import java.util.ArrayList;

/**
 * @author sambit.bixan@gmail.com
 *
 */
public class DataUtil {
	
	public static ArrayList subList(ArrayList list, int startIdx, int endIdx) {
		ArrayList ret = new ArrayList();
		
		if (endIdx >= list.size() - 1) {
			return list;
		}
		
		for (int idx = startIdx; idx < endIdx; idx++) {
			ret.add(idx, list.get(idx));
		}
		
		return ret;
	}
}
