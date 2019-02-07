package com.slonigiraf.homoksafe;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "OperationsDBase")
public class Operation implements Serializable {
    private static final String ID_FIELD_NAME = "id";
    private static final String HEADER_ROOM_FIELD_NAME = "headerRoom_id";
    private static final String NAME_FIELD_NAME = "nameOperation";
    private static final String CHECKED_FIELD_NAME = "isChecked";
    private static final String PHOTO_PATH_FIELD_NAME = "photoPath";
    private static final String TIME_PHOTO_FIELD_NAME = "time";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;
    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnName = HEADER_ROOM_FIELD_NAME)
    private Room room;
    @DatabaseField(columnName = NAME_FIELD_NAME)
    private String name;
    @DatabaseField(columnName = CHECKED_FIELD_NAME)
    private boolean isChecked;
    @DatabaseField(columnName = PHOTO_PATH_FIELD_NAME)
    private String photoPath;
    @DatabaseField(columnName = TIME_PHOTO_FIELD_NAME)
    private String timePhoto;

    public Operation() {
    }

    public Operation(String name) {
        this.name = name;
    }

    public Operation(String name, Room room) {
        this.name = name;
        this.room = room;
    }

    public int getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(int id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getTimePhoto() {
        return timePhoto;
    }

    public void setTimePhoto(String timePhoto) {
        this.timePhoto = timePhoto;
    }

    @Override
    public String toString() {
        return String.format(
                "Operation{id=%s, name=%s,currentRoom=%s,isChecked=%s, timePhoto=%s, photoPath=%s}",
                id, name, room.toString(), isChecked, timePhoto, photoPath);
    }
}
