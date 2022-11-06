/**
 * 
 */
package com.bixan.revest.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author sambit.bixan@gmail.com
 *
 */
public class JsonUtil {
	public static JSONObject createObject() {
		return new JSONObject();
	}
	
	public static JSONObject createObject(String key, Object value) {
		return createObject().put(key, value);
	}
	
	public static JSONArray createArray() {
		return new JSONArray();
	}
	
	public static JSONObject appendObject(JSONObject parent, String key, JSONObject child) {
		if (null == parent) {
			parent = new JSONObject();
		}
		
		parent.append(key, child);
		return parent;
	}
	
	
}
