package com.creations.meister.jungleexplorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.fragment.AnimalBasicInfo;
import com.creations.meister.jungleexplorer.fragment.AnimalLocation;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by meister on 4/1/16.
 */
public class NewAnimal extends AppCompatActivity {

    private static final String INFO_KEY = "INFO";
    private static final String LOCATION_KEY = "LOCATION";
    private static final String ANIMAL_KEY = "ANIMAL";

    private FragmentManager mFragmentManager;
    private BottomBar mBottomBar;
    private ActionBar actionBar;
    private Menu menu;

    private boolean editMode = true;
    private boolean creation;

    private AnimalBasicInfo info;
    private AnimalLocation location;
    private Animal animal;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        this.menu = menu;

        if(animal == null) {
            menuInflater.inflate(R.menu.new_animal_menu, menu);
        } else {
            menuInflater.inflate(R.menu.view_animal_menu, menu);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        mFragmentManager = this.getSupportFragmentManager();
        actionBar = this.getSupportActionBar();

        if(savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if(bundle != null) {
                animal = (Animal) bundle.get(ANIMAL_KEY);
            } else {
                creation = true;
            }

            info = new AnimalBasicInfo();
            location = new AnimalLocation();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.contentFragment, location, NewAnimal.LOCATION_KEY);
            transaction.detach(location);
            transaction.add(R.id.contentFragment, info, NewAnimal.INFO_KEY);
            transaction.commit();
        } else {
            info = (AnimalBasicInfo) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.INFO_KEY);
            location = (AnimalLocation) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.LOCATION_KEY);

            creation = false;
        }

        if (actionBar != null) {
            if(animal == null) {
                actionBar.setTitle(R.string.add_animal);
            } else {
                actionBar.setTitle(R.string.view_animal);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBottomBar = BottomBar.attach(this, savedInstanceState);

        mBottomBar.setItemsFromMenu(R.menu.new_anima_bottombar, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                switch (menuItemId) {
                    case R.id.bb_menu_info:
                        if(info.isDetached()) {
                            transaction.attach(info);
                            transaction.detach(location);
                        }
                        break;
                    case R.id.bb_menu_location:
                        if(location.isDetached()) {
                            transaction.attach(location);
                            transaction.detach(info);
                        }
                        break;
                    case R.id.bb_menu_new_animal_group:
                        transaction.replace(R.id.contentFragment, null, "GROUP");
                        break;
                    case R.id.bb_menu_new_animal_experts:
                        transaction.replace(R.id.contentFragment, null, "EXPERT");
                        break;
                }

                transaction.commit();
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                // TODO do nothing here.
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == android.R.id.home) {
            this.finish();
        } else if(menuItem.getItemId() == R.id.done) {
            if(creation) {
                Animal newAnimal = new Animal();
                for (Fragment fragment : mFragmentManager.getFragments()) {
                    if (fragment instanceof AnimalBasicInfo) {
                       newAnimal = ((AnimalBasicInfo) fragment).setAnimalBasicInfo(newAnimal);
                    } else  if(fragment instanceof AnimalLocation) {
                        newAnimal = ((AnimalLocation) fragment).setAnimalLocation(newAnimal);
                    }
                }
                if(!TextUtils.isEmpty(newAnimal.getName())) {
                    DBHelper.getHelper(NewAnimal.this).insertAnimal(newAnimal);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newAnimal", newAnimal);
                    this.setResult(AppCompatActivity.RESULT_OK, resultIntent);
                } else {
                    this.setResult(AppCompatActivity.RESULT_CANCELED);
                }
                this.finish();
            } else if(editMode){
                actionBar.setTitle(getResources().getString(R.string.view_animal));
                this.menu.clear();
                this.getMenuInflater().inflate(R.menu.view_animal_menu, menu);
                this.editMode = false;
                this.info.setEditable(editMode);
            }
        } else if(menuItem.getItemId() == R.id.edit) {
            actionBar.setTitle(getResources().getString(R.string.edit_animal));
            this.menu.clear();
            this.getMenuInflater().inflate(R.menu.new_animal_menu, menu);
            this.editMode = true;
            this.info.setEditable(editMode);
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mFragmentManager.putFragment(outState, NewAnimal.INFO_KEY, info);
        mFragmentManager.putFragment(outState, NewAnimal.LOCATION_KEY, location);

        mBottomBar.onSaveInstanceState(outState);
    }
}
