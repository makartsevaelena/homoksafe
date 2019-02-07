package com.slonigiraf.homoksafe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "homoksafe_app.db";
    private static final int DATABASE_VERSION = 1;

    private OperationDAO operationDao = null;
    private RoomDAO roomDao = null;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Operation.class);
            TableUtils.createTable(connectionSource, Room.class);
            Room home = new Room(context.getString(R.string.room_home));
            try {
                roomDao.create(home);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Operation iron = new Operation(context.getString(R.string.item_Iron));
            iron.setRoom(home);
            try {
                operationDao.create(iron);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            /* Disabled temperary for growing revenue
            Operation tv = new Operation(context.getString(R.string.item_TV));
            tv.setRoom(home);
            try {
                operationDao.create(tv);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Operation cookingRange = new Operation(context.getString(R.string.item_CookingRange));
            cookingRange.setRoom(home);
            try {
                operationDao.create(cookingRange);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            */

            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") Collection<Operation> operationsHome = new HashSet<>();
            operationsHome.add(iron);

            /* Disabled temperary for growing revenue
            operationsHome.add(tv);
            operationsHome.add(cookingRange);
            */

        } catch (SQLException e) {
            Log.e(TAG, "error creating DB " + DATABASE_NAME);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer,
                          int newVer) {
        try {
            TableUtils.dropTable(connectionSource, Operation.class, true);
            TableUtils.dropTable(connectionSource, Room.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "error upgrading db " + DATABASE_NAME + "from ver " + oldVer);
            throw new RuntimeException(e);
        }
    }

    public OperationDAO getOperationDAO() throws SQLException {
        if (operationDao == null) {
            operationDao = new OperationDAO(getConnectionSource(), Operation.class);
        }
        return operationDao;
    }

    public RoomDAO getRoomDAO() throws SQLException {
        if (roomDao == null) {
            roomDao = new RoomDAO(getConnectionSource(), Room.class);
        }
        return roomDao;
    }

    @Override
    public void close() {
        super.close();
        operationDao = null;
        roomDao = null;
    }
}
