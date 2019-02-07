package com.slonigiraf.homoksafe;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.sql.SQLException;
import java.util.List;

class SpinnerRoomArrayAdapter extends ArrayAdapter<Room> {
    private final LayoutInflater lInflater;
    @SuppressWarnings("unused")
    private Context context;
    private final List<Room> rooms;

    public SpinnerRoomArrayAdapter(Context context, int resource, List<Room> rooms) {
        super(context, resource, rooms);
        this.context = context;
        this.rooms = rooms;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            HelperFactory
                    .getHelper()
                    .getRoomDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Room getItem(int position) {
        return this.rooms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Room room = getItem(position);
        context = parent.getContext();

        SpinnerViewHolder viewHolder;
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.spinner_title, parent, false);
            viewHolder = new SpinnerViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SpinnerViewHolder) convertView.getTag();
        }
        viewHolder.spinnerTitle.setText(
                room != null && room.getName() != null ? room.getName() : ""
        );
        return convertView;
    }


}
