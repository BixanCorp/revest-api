package com.bixan.revest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WhatIf.class)
@ActiveProfiles("test")
class WhatIfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetWhatIfAnalysis() throws Exception {
        mockMvc.perform(get("/whatif/1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetWhatIfWithParameters() throws Exception {
        mockMvc.perform(get("/whatif/analysis")
                .param("years", "10")
                .param("amount", "5000"))
               .andExpect(status().isOk());
    }
}
