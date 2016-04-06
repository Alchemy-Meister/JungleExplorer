package com.creations.meister.jungleexplorer.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.image_utils.ImageHelper;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created by meister on 4/1/16.
 */
public class NewAnimal extends AppCompatActivity implements View.OnClickListener {

    private String mCurrentPhotoPath;
    private Bitmap animalBitmap;
    private ImageView mImageView;
    private EditText mAnimalName;
    private EditText mLocationText;
    private EditText mDescription;

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
        mAnimalName = (EditText) this.findViewById(R.id.name);
        mDescription = (EditText) this.findViewById(R.id.description);
        mLocationText = (EditText) this.findViewById(R.id.locationText);


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
            ImageHelper.galleryAddPic(NewAnimal.this, mCurrentPhotoPath);
            animalBitmap = ImageHelper.scaleImage(mImageView, mCurrentPhotoPath);
            mImageView.setImageBitmap(animalBitmap);
            mImageView.invalidate();
        } else if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            if(data != null) {
                try {
                    Uri uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    animalBitmap = bitmap;
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
            Animal newAnimal = new Animal();
            newAnimal.setName(mAnimalName.getText().toString());
            if(mDescription.getText() != null) {
                newAnimal.setDescription(mDescription.getText().toString());
            }
            if(mLocationText.getText() != null) {
                newAnimal.setDescription(mLocationText.getText().toString());
            }
            DBHelper.getHelper(NewAnimal.this).insertAnimal(newAnimal);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newAnimal", newAnimal);
            this.setResult(AppCompatActivity.RESULT_OK, resultIntent);
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
                                        photoFile = ImageHelper.createImageFile();
                                        mCurrentPhotoPath = photoFile.getAbsolutePath();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("animalImage", animalBitmap);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        animalBitmap = savedState.getParcelable("animalImage");

        if(animalBitmap != null) {
            mImageView.setImageBitmap(animalBitmap);
            mImageView.invalidate();
        }
    }
}
