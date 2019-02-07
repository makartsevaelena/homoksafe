package com.slonigiraf.homoksafe;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "RoomsDBase")
public class Room implements Serializable {
    private static final String ID_FIELD_NAME = "id";
    private static final String NAME_FIELD_NAME = "nameRoom";
    private static final String FOREIGN_FIELD_NAME = "operationList";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;
    @DatabaseField(columnName = NAME_FIELD_NAME)
    private String name;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @ForeignCollectionField(columnName = FOREIGN_FIELD_NAME)
    private final Collection<Operation> operations = new HashSet<>();

    public Room() {

    }

    public Room(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Operation> getOperations() {
        return new ArrayList<>(operations);
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }
}
