package com.creations.meister.jungleexplorer.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by meister on 4/1/16.
 */
public class NewAnimal extends AppCompatActivity implements View.OnClickListener {

    private String mCurrentPhotoPath;
    private ImageView mImageView;
    private final int CAMERA_REQUEST = 1888;
    private final int GALLERY_REQUEST = 4261;
    private final int STORAGE_ASK_REQUEST = 123;

    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_animal_menu, menu);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.new_animal);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.add_animal);

        mImageView = (ImageView) this.findViewById(R.id.animalImage);


        initializeImageViewListener();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(RuntimePermissionsHelper.hasPermissions(NewAnimal.this, requiredPermissions)) {
            mImageView.setOnClickListener(this);
        }
    }

    private void initializeImageViewListener() {
        int hasReadContactsPermission = ContextCompat.checkSelfPermission(NewAnimal.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewAnimal.this,
                    requiredPermissions,
                    STORAGE_ASK_REQUEST);
            return;
        }
        mImageView.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_ASK_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mImageView.setOnClickListener(NewAnimal.this);
                } else {
                    RuntimePermissionsHelper.showMessageOKCancel(getResources().getString(
                            R.string.storage_permission_message,
                            getResources().getString(R.string.app_name)),
                            NewAnimal.this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            this.galleryAddPic();
            this.setPic();
            mImageView.invalidate();
        } else if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            if(data != null) {
                try {
                    Uri uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    mImageView.setImageBitmap(bitmap);
                    mImageView.invalidate();

                } catch (FileNotFoundException e) {
                    // Error opening the image
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == android.R.id.home) {
            this.finish();
        } else if(menuItem.getItemId() == R.id.done) {
            // TODO save on db;
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.animalImage) {
            final String[] pOptions = getResources().getStringArray(R.array.photo_options);
            new AlertDialog.Builder(NewAnimal.this)
                    .setTitle(R.string.change_photo)
                    .setItems(R.array.photo_options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(pOptions[which].equals(pOptions[0])) {
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                // Ensure that there's a camera activity to handle the intent
                                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                    // Create the File where the photo should go
                                    File photoFile = null;
                                    try {
                                        photoFile = createImageFile();
                                    } catch (IOException ex) {
                                        // Error occurred while creating the File
                                    }
                                    // Continue only if the File was successfully created
                                    if (photoFile != null) {
                                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                Uri.fromFile(photoFile));
                                        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                                    }
                                }
                            }else if(pOptions[which].equals(pOptions[1])) {
                                Intent galleryPickerIntent = new Intent(Intent.ACTION_PICK);
                                galleryPickerIntent.setType("image/*");
                                startActivityForResult(galleryPickerIntent, GALLERY_REQUEST);
                            }
                        }
                    })
                    .setPositiveButton(R.string.cancel, null)
                    .create()
                    .show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Jungle Explorer");
        if(!storageDir.exists())
            storageDir.mkdir();

        File image = new File(storageDir, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;

        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        mImageView.setImageBitmap(bitmap);
    }
}
