package com.bixan.revest.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Abstract base DAO implementation that provides common database access
 * functionality
 */
@Profile("!test")
public abstract class AbstractBaseDao implements BaseDao {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}