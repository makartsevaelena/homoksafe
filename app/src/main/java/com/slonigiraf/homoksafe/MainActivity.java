package com.slonigiraf.homoksafe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.slonigiraf.homoksafe.billing.BillingManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.slonigiraf.homoksafe.HelperFactory.releaseHelper;

public class MainActivity extends AppCompatActivity {

    private static final int maxOperationsInFreeVersion = 2;
    private static final String MORE_OPERATIONS = "more_operations";
    private static final String USER_AGREE = "user_agree";
    private static final String SPINNER = "spinner_lastselection";

    private final String KEY_SavedSel = "Saved Selection";
    private Context context;
    private final ArrayList<Operation> operationsList = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private HashMap<String, Room> roomHashMap;
    private OperationDAO operationDao;
    private RoomDAO roomDAO;
    private OperationArrayAdapter arrayAdapter;
    private Operation operation;
    private Spinner spinner;
    private ListView listView;
    private Menu menu;
    private BillingManager mBillingManager;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Button addRoomButton;
    private Button addOperationButton;
    private Button donateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkHasAgree();
        context = MainActivity.this;
        listView = findViewById(R.id.mainListView);
        addRoomButton = findViewById(R.id.add_room);
        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userWantsToAddRoom();
            }
        });

        addOperationButton = findViewById(R.id.add_operation);
        addOperationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userWantsToAddOperation();
            }
        });


        donateButton = findViewById(R.id.donate);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.DONATE_TRY, Analytics.DONATE_TRY_DESCRIPTION, Analytics.DONATE_ACTION);
                buyUnlimitedOperations();
            }
        });


        createActionBar();
        createDataBaseTable();
        getDAO();
        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        hideDonateIfUserPaid();
        handleDinamicLink();
    }

    private void handleDinamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void checkHasAgree() {
        SharedPreferences sharedPreferences = getSharedPreferences(USER_AGREE, Context.MODE_PRIVATE);
        boolean hasAgree = sharedPreferences.getBoolean("hasAgree", false);
        if (!hasAgree) {
            Intent preferences = new Intent(MainActivity.this, PreferencesActivity.class);
            startActivity(preferences);
            finish();
        }
    }

    private void createActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    private void createDataBaseTable() {
        new DatabaseHelper(this);
    }

    private void getDAO() {
        HelperFactory.setHelper(getApplicationContext());
        try {
            operationDao = HelperFactory
                    .getHelper()
                    .getOperationDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            roomDAO = HelperFactory
                    .getHelper()
                    .getRoomDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.optionsmenu, menu);
        changeDeleteRoomMenuItemVisibility();
        invalidateOptionsMenu();
        return true;
    }

    void userWantsToAddRoom() {
        alertDialogAddNewRoom();
    }

    void userWantsToAddOperation() {
        if (isAllowedToAddNewOperation()) {
            alertDialogAddNewOperation();
        } else {
            Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.SUGGEST_BUY, Analytics.SUGGEST_BUY_DESCRIPTION, Analytics.LIMIT_REACHED_ACTION);
            alertDialogBuyUnlimitedOperations();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            alertDialogDeleteRoom();
        }
        if (id == R.id.refresh) {
            alertDialogRefreshContent();
        }
        if (id == R.id.info) {
            Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.AGREEMENT_OPEN, Analytics.AGREEMENT_OPEN_DESCRIPTION, Analytics.NAVIGATION_ACTION);
            startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isAllowedToAddNewOperation() {
        boolean isAllowedToAddNewOperation = false;
        if (isMoreOperationBought()) {
            isAllowedToAddNewOperation = true;
        }
        if (getTotalNumberOfOperationsInDB() < maxOperationsInFreeVersion) {
            isAllowedToAddNewOperation = true;
        }
        return isAllowedToAddNewOperation;
    }

    private void alertDialogAddNewRoom() {
        final EditText editTextRoom = new EditText(this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setView(editTextRoom);
        alertDialog.setTitle(R.string.addDialogNewRoom_title_addNewNameRoom);
        alertDialog.setPositiveButton(
                R.string.addNewDialog_button_save,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String roomName = editTextRoom.getText().toString();
                        if (roomName.equals("")) {
                            roomName = context.getString(R.string.item_noName);
                        }
                        addRoom(roomName);
                    }
                });
        alertDialog.setNegativeButton(
                R.string.addNewDialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        spinner.setSelection(1);
                        dialog.cancel();
                    }

                });
        alertDialog.show();
    }

    private void addRoom(String roomName) {
        if (!roomHashMap.containsKey(roomName)) {
            Room room = new Room();
            room.setName(roomName);
            try {
                roomDAO.create(room);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            int newRoomIndex = rooms.size();
            saveLastSelectedRoomPosition(newRoomIndex);
            refreshView();
        } else {
            spinner.setSelection(1);
            Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_exist_name, Toast.LENGTH_SHORT);
            toast.show();
        }
        Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.ROOM_ADD, Analytics.ROOM_ADD_DESCRIPTION, Analytics.EDIT_TYPE_ACTION);
    }

    private void alertDialogDeleteRoom() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getString(R.string.deleteDialog_title_deleteRoom));
        alertDialog.setMessage(context.getString(R.string.deleteDialogRoom_textMessage) + " " + "\"" +
                spinner.getSelectedItem().toString() + "\"" + "?");
        alertDialog.setNegativeButton(getString(R.string.deleteDialog_button_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.setPositiveButton(getString(R.string.deleteDialog_button_delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRoom();
                    }
                });
        alertDialog.show();
    }

    private void deleteRoom() {
        try {
            if (roomDAO.getAllRooms().size() > 1) {
                try {
                    roomDAO.delete(getSelectedSpinnerItem());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                saveLastSelectedRoomPosition(1);
                refreshView();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_last_room, Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.ROOM_DELETE, Analytics.ROOM_DELETE_DESCRIPTION, Analytics.EDIT_TYPE_ACTION);
    }

    private void alertDialogBuyUnlimitedOperations() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getString(R.string.get_full_version_title));
        alertDialog.setMessage(getString(R.string.get_more_operations_message) + " " + "\"" + getString(R.string.get_more_operations_pay) + "\"" + ".");
        alertDialog.setNegativeButton(getString(R.string.get_more_operations_buttonCancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.BUY_DECLINED, Analytics.BUY_DECLINED_DESCRIPTION, Analytics.BUY_DIALOG_ACTION);
                        dialog.cancel();
                    }
                });
        alertDialog.setPositiveButton(getString(R.string.get_more_operations_buttonPay),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.BUY_TRY, Analytics.BUY_TRY_DESCRIPTION, Analytics.BUY_DIALOG_ACTION);
                        buyUnlimitedOperations();
                    }
                });
        alertDialog.show();
    }

    private void buyUnlimitedOperations() {
        mBillingManager.startPurchaseFlow(MORE_OPERATIONS,
                BillingClient.SkuType.INAPP);
    }

    private boolean isMoreOperationBought() {
        return mBillingManager.isPurchased(MORE_OPERATIONS);
    }

    private void alertDialogRefreshContent() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getString(R.string.refreshContentDialog_title_deleteContent));
        alertDialog.setMessage(getString(R.string.refreshContentDialog_textMessage) + "?");
        alertDialog.setNegativeButton(
                R.string.refreshContentDialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.setPositiveButton(
                R.string.refreshContentDialog_button_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.REMOVE_DATES, Analytics.REMOVE_DATES_DESCRIPTION, Analytics.EDIT_TYPE_ACTION);
                        for (Operation operation : getSelectedSpinnerItem().getOperations()) {
                            refreshContentInDBandList(operation);
                        }
                    }
                });
        alertDialog.show();
    }

    private void refreshContentInDBandList(Operation operation) {
        operation.setChecked(false);
        operation.setTimePhoto(null);
        operation.setPhotoPath(null);
        try {
            operationDao.update(operation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        refreshView();
    }

    private void alertDialogAddNewOperation() {
        final EditText editTextOperation = new EditText(this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setView(editTextOperation);
        alertDialog.setTitle(R.string.addDialogNewOperation_title_addNewName);
        alertDialog.setPositiveButton(
                R.string.addNewDialog_button_save,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String operationName = editTextOperation.getText().toString();
                        Operation operation = new Operation(operationName, getSelectedSpinnerItem());
                        saveOperation(operation);
                        Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.OPERATION_ADD, Analytics.OPERATION_ADD_DESCRIPTION, Analytics.EDIT_TYPE_ACTION);
                        refreshView();
                    }
                });
        alertDialog.setNegativeButton(
                R.string.addNewDialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void saveOperation(Operation operation) {
        if (operation.getName().equals("")) {
            operation.setName(context.getString(R.string.item_noName));
        }
        operation.setRoom(operation.getRoom());
        try {
            operationDao.create(operation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        operation = arrayAdapter.getCurrentOperation();
        if (resultCode == RESULT_OK) {
            int CAMERA_RESULT = 1;
            if (requestCode == CAMERA_RESULT) {
                setTimeToDBandList();
                setPhotoToDBandList();
                arrayAdapter.updateOperationInDBAndList(operation);
            }
        }
    }

    private void setPhotoToDBandList() {
        String pathSavePhoto = arrayAdapter.getCurrentPhotoPath();
        ImageView itemImageViewPhoto = arrayAdapter.getCurrentImageViewPhoto();
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(new File(pathSavePhoto));
        } else {
            uri = FileProvider.getUriForFile(context, getString(R.string.fileprovider), new File(pathSavePhoto));
        }
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            itemImageViewPhoto.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        operation.setPhotoPath(pathSavePhoto);
        if (itemImageViewPhoto.isClickable()) {
            operation.setChecked(true);
        } else {
            operation.setChecked(false);
        }
    }

    private void setTimeToDBandList() {
        Date time = new Date();
        String dataFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(time);
        TextView itemTextViewTime = arrayAdapter.getCurrentTextViewTime();
        itemTextViewTime.setText(dataFormat);
        operation.setTimePhoto(dataFormat);
    }

    private void refreshView() {
        loadAndSetDataFromDB();
        addButtonAddNewInSpinner();
        addSpinner();
        spinner.setSelection(getLastSelectedRoomPosition());
        changeDeleteRoomMenuItemVisibility();
        hideDonateIfUserPaid();
    }

    private void hideDonateIfUserPaid() {


        if (isMoreOperationBought()) {
            donateButton.setVisibility(View.INVISIBLE);
        }
    }

    private void loadAndSetDataFromDB() {
        roomHashMap = new HashMap<>();
        List<Room> roomsForLoadFromDB = new ArrayList<>();
        //noinspection EmptyCatchBlock
        try {
            roomsForLoadFromDB = roomDAO.getAllRooms();
        } catch (SQLException e) {
        }
        operationsList.clear();
        rooms.clear();
        for (int i = 0; i < roomsForLoadFromDB.size(); i++) {
            Room room = roomsForLoadFromDB.get(i);
            operationsList.addAll(room.getOperations());
            String roomName = room.getName();
            roomHashMap.put(roomName, room);
            rooms.add(room);
        }
    }

    private void addButtonAddNewInSpinner() {
        Room addNew = new Room(getString(R.string.addButtonSpinner));
        rooms.add(0, addNew);
    }

    private void addSpinner() {
        spinner = findViewById(R.id.spinner);
        SpinnerRoomArrayAdapter spinnerAdapter = new SpinnerRoomArrayAdapter(this, R.layout.spinner_title, rooms);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSelectedSpinnerItem();
                if (position == 0) {
                    userWantsToAddRoom();
                } else {
                    saveLastSelectedRoomPosition(position);
                    arrayAdapter = new OperationArrayAdapter(MainActivity.this, getSelectedSpinnerItem().getOperations());
                    listView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void changeDeleteRoomMenuItemVisibility() {
        if (menu != null) {
            MenuItem menuItemDelete = menu.findItem(R.id.delete);
            try {
                if (roomDAO.getAllRooms().size() == 1) {
                    menuItemDelete.setVisible(false);
                } else {
                    menuItemDelete.setVisible(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveLastSelectedRoomPosition(int position) {
        SharedPreferences sharedPreferences = getSharedPreferences(SPINNER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_SavedSel, position);
        editor.apply();
    }

    private int getLastSelectedRoomPosition() {
        SharedPreferences sharedPreferences = getSharedPreferences(SPINNER, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_SavedSel, 1);
    }

    private Room getSelectedSpinnerItem() {
        return (Room) spinner.getSelectedItem();
    }

    private int getTotalNumberOfOperationsInDB() {
        refreshView();
        return operationsList.size();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseHelper();
        mBillingManager.destroy();
    }
}