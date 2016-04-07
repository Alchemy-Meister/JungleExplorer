package com.creations.meister.jungleexplorer.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

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
public class AnimalBasicInfo extends Fragment implements View.OnClickListener {

    private String mCurrentPhotoPath;
    private Bitmap animalBitmap;
    private ImageView mImageView;
    private TextInputLayout mNameTextLayout;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.new_animal, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageView = (ImageView) this.getActivity().findViewById(R.id.animalImage);

        if(savedInstanceState != null) {
            animalBitmap = savedInstanceState.getParcelable("animalImage");

            if (animalBitmap != null) {
                mImageView.setImageBitmap(animalBitmap);
                mImageView.invalidate();
            }
        }

        mNameTextLayout = (TextInputLayout)
                this.getActivity().findViewById(R.id.text_input_name);

        mAnimalName = (TextInputEditText) this.getActivity().findViewById(R.id.name);
        mDescription = (TextInputEditText) this.getActivity().findViewById(R.id.description);
        mLocationText = (TextInputEditText) this.getActivity().findViewById(R.id.locationText);

        initializeImageViewListener();

        mAnimalName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (TextUtils.isEmpty(mAnimalName.getText())) {
                        mNameTextLayout.setError(getResources().getString(R.string.invalid_name));
                    } else {
                        mNameTextLayout.setError(null);
                    }
                }
            }
        });

        this.getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(RuntimePermissionsHelper.hasPermissions(AnimalBasicInfo.this.getContext(),
                requiredPermissions))
        {
            mImageView.setOnClickListener(this);
        }
    }

    public Animal setAnimalBasicInfo(Animal animal) {
            animal.setName(mAnimalName.getText().toString());
            if (!TextUtils.isEmpty(mDescription.getText())) {
                animal.setDescription(mDescription.getText().toString());
            }
            if (!TextUtils.isEmpty(mLocationText.getText())) {
                animal.setDescription(mLocationText.getText().toString());
            }
            return animal;
    }

    private void initializeImageViewListener() {
        int hasReadContactsPermission = ContextCompat.checkSelfPermission(
                AnimalBasicInfo.this.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AnimalBasicInfo.this.getActivity(),
                    requiredPermissions,
                    STORAGE_ASK_REQUEST);
            return;
        }
        mImageView.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case STORAGE_ASK_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mImageView.setOnClickListener(AnimalBasicInfo.this);
                } else {
                    RuntimePermissionsHelper.showMessageOKCancel(getResources().getString(
                                    R.string.storage_permission_message,
                                    getResources().getString(R.string.app_name)),
                            AnimalBasicInfo.this.getContext());
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            ImageHelper.galleryAddPic(AnimalBasicInfo.this.getContext(), mCurrentPhotoPath);
            animalBitmap = ImageHelper.scaleImage(mImageView, mCurrentPhotoPath);
            mImageView.setImageBitmap(animalBitmap);
            mImageView.invalidate();
        } else if(requestCode == GALLERY_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            if(data != null) {
                try {
                    Uri uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            AnimalBasicInfo.this.getContext().getContentResolver(), uri);
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
            AnimalBasicInfo.this.getActivity().finish();
        } else if(menuItem.getItemId() == R.id.done) {
            if(!TextUtils.isEmpty(mAnimalName.getText())) {
                Animal newAnimal = new Animal();
                newAnimal.setName(mAnimalName.getText().toString());
                if (!TextUtils.isEmpty(mDescription.getText())) {
                    newAnimal.setDescription(mDescription.getText().toString());
                }
                if (!TextUtils.isEmpty(mLocationText.getText())) {
                    newAnimal.setDescription(mLocationText.getText().toString());
                }
                DBHelper.getHelper(AnimalBasicInfo.this.getContext()).insertAnimal(newAnimal);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("newAnimal", newAnimal);
                this.getActivity().setResult(AppCompatActivity.RESULT_OK, resultIntent);
            } else {
                this.getActivity().setResult(AppCompatActivity.RESULT_CANCELED);
            }
            this.getActivity().finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.animalImage) {
            final String[] pOptions = getResources().getStringArray(R.array.photo_options);
            new AlertDialog.Builder(AnimalBasicInfo.this.getContext())
                    .setTitle(R.string.change_photo)
                    .setItems(R.array.photo_options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(pOptions[which].equals(pOptions[0])) {
                                Intent takePictureIntent =
                                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                // Ensure that there's a camera activity to handle the intent
                                if (takePictureIntent.resolveActivity(
                                        AnimalBasicInfo.this.getActivity().
                                                getPackageManager()) != null)
                                {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("animalImage", animalBitmap);
    }
}
