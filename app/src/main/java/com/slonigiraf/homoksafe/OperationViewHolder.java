package com.slonigiraf.homoksafe;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;



class OperationViewHolder extends RecyclerView.ViewHolder {
    final ImageView itemImageViewPhoto, itemImageViewSetting;
    final TextView itemTextViewName, itemTextViewTime;

    OperationViewHolder(View view) {
        super(view);
        itemTextViewName = view.findViewById(R.id.textViewName);
        itemTextViewTime = view.findViewById(R.id.textViewTime);
        itemImageViewPhoto = view.findViewById(R.id.imageViewPhoto);
        itemImageViewSetting = view.findViewById(R.id.imageViewSetting);
    }
}
