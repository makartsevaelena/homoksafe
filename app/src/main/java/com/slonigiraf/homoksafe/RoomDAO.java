package com.slonigiraf.homoksafe;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

public class RoomDAO extends BaseDaoImpl<Room, Integer> {
    RoomDAO(ConnectionSource connectionSource, Class<Room> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public List<Room> getAllRooms() throws SQLException {
        return this.queryForAll();
    }

    @Override
    public Room queryForId(Integer integer) throws SQLException {
        return super.queryForId(integer);
    }
}
