package com.slonigiraf.homoksafe;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {
    //TYPES
    public static final String EDIT_TYPE_ACTION = "edit";
    public static final String PHOTO_TAKE_ACTION = "photo_take";
    public static final String NAVIGATION_ACTION = "navigation";
    public static final String PHOTO_SEE_ACTION = "photo_see";
    public static final String LIMIT_REACHED_ACTION = "limit_reached";
    public static final String BUY_DIALOG_ACTION = "buy_dialog";
    public static final String DONATE_ACTION = "donate";
    public static final String TERMS_AND_POLITICS_ACTION = "terms_and_politics";

    public static final String TERMS_AND_POLITICS_AGREE = "terms_and_politics_agree";
    public static final String TERMS_AND_POLITICS_AGREE_DESCRIPTION = "User agrees with terms and politics";

    public static final String TERMS_AND_POLITICS_SEE_ALL = "terms_and_politics_see_all";
    public static final String TERMS_AND_POLITICS_SEE_ALL_DESCRIPTION = "User opens full view of terms and politics";

    public static final String ROOM_ADD = "room_add";
    public static final String ROOM_ADD_DESCRIPTION = "Room add";

    public static final String ROOM_DELETE = "room_delete";
    public static final String ROOM_DELETE_DESCRIPTION = "Room delete";

    public static final String OPERATION_ADD = "operation_add";
    public static final String OPERATION_ADD_DESCRIPTION = "Operation add";

    public static final String REMOVE_DATES = "remove_dates";
    public static final String REMOVE_DATES_DESCRIPTION = "Remove dates and photos";

    public static final String OPERATION_DELETE = "operation_delete";
    public static final String OPERATION_DELETE_DESCRIPTION = "Operation delete";

    public static final String OPERATION_RENAME = "operation_rename";
    public static final String OPERATION_RENAME_DESCRIPTION = "Operation rename";

    public static final String PHOTO_TAKE = "photo_take";
    public static final String PHOTO_TAKE_DESCRIPTION = "Photo take";

    public static final String AGREEMENT_OPEN = "agreement_open";
    public static final String AGREEMENT_OPEN_DESCRIPTION = "Agreement open";

    public static final String PHOTO_SEE = "photo_see";
    public static final String PHOTO_SEE_DESCRIPTION = "Photo see";


    public static final String SUGGEST_BUY = "buy_suggest";
    public static final String SUGGEST_BUY_DESCRIPTION = "User wants more operations";

    public static final String BUY_DECLINED = "buy_declined";
    public static final String BUY_DECLINED_DESCRIPTION = "User clicked on Cancel after buy suggestion";

    public static final String BUY_TRY = "buy_try";
    public static final String BUY_TRY_DESCRIPTION = "User clicked on Agree after buy suggestion";

    public static final String DONATE_TRY = "donate_try";
    public static final String DONATE_TRY_DESCRIPTION = "User clicked on donate button";

    protected static void logFirebaseAnalytics(Context context, String id, String name, String type) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
