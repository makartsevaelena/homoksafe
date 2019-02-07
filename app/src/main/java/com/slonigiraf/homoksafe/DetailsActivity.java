package com.slonigiraf.homoksafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

public class DetailsActivity extends AppCompatActivity implements Serializable {
    private final int CAMERA_RE_PHOTO = 0;
    private Context context;
    private ImageView imageViewDetailPhoto;
    private TextView textViewDetailName;
    private TextView textViewDetailTime;
    private OperationDAO operationDAO;
    private Operation operation;
    private File file;
    private Uri photoFileUri;
    private String detailTimeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        HelperFactory.setHelper(getApplicationContext());
        try {
            operationDAO = HelperFactory
                    .getHelper()
                    .getOperationDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        context = DetailsActivity.this;
        imageViewDetailPhoto = findViewById(R.id.imageView_details_foto);
        textViewDetailName = findViewById(R.id.textView_details_name);
        textViewDetailTime = findViewById(R.id.textView_details_time);
        setDataFromIntent();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setLogo(R.mipmap.ic_main_app);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);
        }

    }

    private void setDataFromIntent() {
        operation = (Operation) getIntent().getSerializableExtra(getString(R.string.intent_name_operation));
        file = new File(operation.getPhotoPath());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            photoFileUri = Uri.fromFile(file);
        } else {
            photoFileUri = FileProvider.getUriForFile(context, getString(R.string.fileprovider), file);
        }
        imageViewDetailPhoto.setImageURI(photoFileUri);
        String detailNameOperation = operation.getName();
        if (detailNameOperation.equals("")) {
            textViewDetailName.setText(getString(R.string.widgetDetail_name));
        } else {
            textViewDetailName.setText(detailNameOperation);
        }
        detailTimeFormat = operation.getTimePhoto();
        textViewDetailTime.setText(detailTimeFormat);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.menu_item_refreshPhoto)
                .setIcon(R.drawable.ic_refresh_photo_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle() == getString(R.string.menu_item_refreshPhoto)) {
            alertDialogRefreshPhoto();
        }
        return super.onOptionsItemSelected(item);
    }

    private void alertDialogRefreshPhoto() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetailsActivity.this);
        alertDialog.setTitle(getString(R.string.refreshPhotoDialog_title));
        alertDialog.setMessage(getString(R.string.refreshPhotoDialog_textMessage) + "?");
        alertDialog.setNegativeButton(getString(R.string.refreshPhotoDialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setPositiveButton(getString(R.string.refreshPhotoDialog_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                takeNewPhotoFromCamera();
            }
        });

        alertDialog.show();
    }

    private void takeNewPhotoFromCamera() {
        Intent intentPhotoFromCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intentPhotoFromCamera.setClipData(ClipData.newRawUri("", photoFileUri));
            intentPhotoFromCamera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intentPhotoFromCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
        ((Activity) context).startActivityForResult(intentPhotoFromCamera, CAMERA_RE_PHOTO);
        setResult(RESULT_OK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_RE_PHOTO) {
                setPhotoInImageView();
                setTimeToListViewAndDB();
            }
        }
    }

    private void setPhotoInImageView() {
        Uri uri = FileProvider.getUriForFile(context, getString(R.string.fileprovider), file);
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imageViewDetailPhoto.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTimeToListViewAndDB() {
        Date time = new Date();
        detailTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(time);
        textViewDetailTime.setText(detailTimeFormat);
        operation.setTimePhoto(detailTimeFormat);
        try {
            operationDAO.update(operation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
