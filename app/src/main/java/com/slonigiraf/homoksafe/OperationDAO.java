package com.slonigiraf.homoksafe;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class OperationDAO extends BaseDaoImpl<Operation, Integer> {

    OperationDAO(ConnectionSource connectionSource, Class<Operation> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public Operation queryForId(Integer integer) throws SQLException {
        return super.queryForId(integer);
    }
}