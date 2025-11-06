package com.bixan.revest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Scenario.class)
@ActiveProfiles("test")
class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetScenario() throws Exception {
        mockMvc.perform(get("/scenario/1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json"));
    }

    @Test
    void testPostInvestmentData() throws Exception {
        String investmentJson = "{\"amount\": 1000, \"years\": 10}";
        
        mockMvc.perform(post("/scenario/investment")
                .contentType("application/json")
                .content(investmentJson))
               .andExpect(status().isOk());
    }
}
