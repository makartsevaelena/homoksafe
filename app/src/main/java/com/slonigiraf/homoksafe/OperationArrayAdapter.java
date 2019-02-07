package com.slonigiraf.homoksafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

class OperationArrayAdapter extends BaseAdapter implements Serializable {

    private Context context;
    private final LayoutInflater lInflater;
    private final ArrayList<Operation> operationsList;
    private OperationDAO operationDAO;
    private TextView currentTextViewTime;
    @SuppressWarnings("unused")
    private TextView currentTextViewName;
    private ImageView currentImageViewPhoto;
    private Operation currentOperation;
    private File imageFile;
    private EditText textEditRename;
    private OperationViewHolder viewHolder;


    public OperationArrayAdapter(Context context, ArrayList<Operation> operations) {
        this.context = context;
        this.operationsList = operations;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            operationDAO = HelperFactory
                    .getHelper()
                    .getOperationDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        return operationsList.size();
    }

    @Override
    public Operation getItem(int position) {
        return this.operationsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Operation itemOperation = getItem(position);
        context = parent.getContext();

        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.custom_viewtext_layout, parent, false);
            viewHolder = new OperationViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (OperationViewHolder) convertView.getTag();
        }
        viewHolder.itemImageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPopupMenuOfItem(view, itemOperation);
            }
        });
        setTime(itemOperation);
        setName(itemOperation);
        setPhoto(itemOperation);
        return convertView;
    }

    private void setName(Operation operation) {
        viewHolder.itemTextViewName.setText(operation.getName());
    }

    private void setTime(Operation operation) {
        String timeOperation = operation.getTimePhoto();
        viewHolder.itemTextViewTime.setText(timeOperation);
    }

    private void setPhoto(final Operation operation) {
        String imagePath = operation.getPhotoPath();
        if (imagePath == null) {
            viewHolder.itemImageViewPhoto.setImageResource(R.drawable.ic_check_black);
            viewHolder.itemTextViewTime.setText("");
            viewHolder.itemImageViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentImageViewPhoto = viewHolder.itemImageViewPhoto;
                    currentTextViewTime = viewHolder.itemTextViewTime;
                    currentTextViewName = viewHolder.itemTextViewName;
                    currentOperation = operation;
                    takePhotoFromCamera();
                }
            });
        } else {
            viewHolder.itemImageViewPhoto.setImageBitmap(BitmapHelper.decodeSampledBitmapFromResource(operation.getPhotoPath(), 70, 70));
            viewHolder.itemImageViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetailsActivity(operation);
                }
            });
        }
    }



    private void takePhotoFromCamera() {
        Analytics.logFirebaseAnalytics(context, Analytics.PHOTO_TAKE, Analytics.PHOTO_TAKE_DESCRIPTION, Analytics.PHOTO_TAKE_ACTION);
        Intent intentTakePhotoFromCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentTakePhotoFromCamera.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = createImageFileForPhotoFromCamera();
            Uri uriPhotoFile;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                uriPhotoFile = Uri.fromFile(photoFile);
                intentTakePhotoFromCamera.setClipData(ClipData.newRawUri("", uriPhotoFile));
                intentTakePhotoFromCamera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uriPhotoFile = FileProvider.getUriForFile(context, context.getString(R.string.fileprovider), photoFile);
            }
            intentTakePhotoFromCamera.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile);
            int CAMERA_RESULT = 1;
            ((Activity) context).startActivityForResult(intentTakePhotoFromCamera, CAMERA_RESULT);
        }
    }

    private File createImageFileForPhotoFromCamera() {
        String timeStamp = Integer.toString(currentOperation.getId());
        String photoFileName = "JPEG_" + timeStamp + ".jpeg";
        File photoFilePath = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/");
        if (photoFilePath.exists()) {
            //noinspection ResultOfMethodCallIgnored
            //photoFilePath.getParentFile().mkdirs();
        }
        imageFile = new File(photoFilePath, photoFileName);
        return imageFile;
    }

    private void openDetailsActivity(Operation operation) {
        Analytics.logFirebaseAnalytics(context, Analytics.PHOTO_SEE, Analytics.PHOTO_SEE_DESCRIPTION, Analytics.PHOTO_SEE_ACTION);
        Intent intentDetailsActivity = new Intent(context, DetailsActivity.class);
        intentDetailsActivity.putExtra("nameOperation", operation);
        context.startActivity(intentDetailsActivity);
    }

    private void alertDialogRename(final Operation operation) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        String renameDialogTitle = context.getString(R.string.renameDialog_title_rename);
        alertDialog.setTitle(renameDialogTitle);
        textEditRename = new EditText(context);
        alertDialog.setView(textEditRename);
        String name = operation.getName();
        alertDialog.setMessage(context.getString(R.string.renameDialog_textMessage) + " " + "\"" + name + "\"");
        alertDialog.setNegativeButton(
                R.string.renameDialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.setPositiveButton(
                R.string.renameDialog_button_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        renameOperation(operation);
                    }
                });
        alertDialog.show();
    }

    private void renameOperation(Operation operation) {
        String textRename = textEditRename.getText().toString();
        if (textRename.equals("")) {
            operation.setName(operation.getName());
        } else {
            operation.setName(textRename);
        }
        updateOperationInDBAndList(operation);
        Analytics.logFirebaseAnalytics(context, Analytics.OPERATION_RENAME, Analytics.OPERATION_RENAME_DESCRIPTION, Analytics.EDIT_TYPE_ACTION);
    }

    public void updateOperationInDBAndList(Operation operation) {
        try {
            operationDAO.update(operation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    private void alertDialogDelete(final Operation operation) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        String deleteDialogTitle = context.getString(R.string.deleteDialog_title_delete);
        String nameItem = operation.getName();
        alertDialog.setMessage(context.getString(R.string.deleteDialog_textMessage) + " " + "\"" + nameItem + "\"" + "?");
        alertDialog.setTitle(deleteDialogTitle);
        alertDialog.setNegativeButton(
                R.string.deleteDialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.setPositiveButton(
                R.string.deleteDialog_button_delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteOperation(operation);
                    }
                });

        alertDialog.show();
    }

    private void deleteOperation(Operation operation) {
        try {
            operationDAO.delete(operation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        operationsList.remove(operation);
        notifyDataSetChanged();
        Analytics.logFirebaseAnalytics(context, Analytics.OPERATION_DELETE, Analytics.OPERATION_DELETE_DESCRIPTION, Analytics.EDIT_TYPE_ACTION);
    }

    private void openPopupMenuOfItem(View view, final Operation operation) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.rename) {
                    alertDialogRename(operation);
                } else if (id == R.id.delete) {
                    alertDialogDelete(operation);
                }
                return true;
            }
        });
        popup.getMenuInflater().inflate(R.menu.popupmenu_item, popup.getMenu());
        popup.show();
    }

    public ImageView getCurrentImageViewPhoto() {
        return currentImageViewPhoto;
    }

    public Operation getCurrentOperation() {
        return currentOperation;
    }

    public TextView getCurrentTextViewTime() {
        return currentTextViewTime;
    }

    public String getCurrentPhotoPath() {
        return imageFile.getAbsolutePath();
    }
}