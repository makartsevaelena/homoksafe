package com.slonigiraf.homoksafe;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


class SpinnerViewHolder extends RecyclerView.ViewHolder {
    final TextView spinnerTitle;

    SpinnerViewHolder(View view) {
        super(view);
        spinnerTitle = view.findViewById(R.id.spinner_title);
    }
}
