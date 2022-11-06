/**
 * 
 */
package com.bixan.revest.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sambit.bixan@gmail.com
 *
 */
@RestController
@RequestMapping("/account")
public class Account {
	@GetMapping(value = "/list", produces = "application/json")
	public String list() {
		return (new JSONObject().put("result", "Hello World")).toString();
	}
}
