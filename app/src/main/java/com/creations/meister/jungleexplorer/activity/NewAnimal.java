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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.fragment.AnimalBasicInfo;
import com.creations.meister.jungleexplorer.fragment.AnimalExpert;
import com.creations.meister.jungleexplorer.fragment.AnimalLocation;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by meister on 4/1/16.
 */
public class NewAnimal extends AppCompatActivity {

    private static final String INFO_KEY = "INFO";
    private static final String LOCATION_KEY = "LOCATION";
    private static final String EXPERT_KEY = "EXPERT";
    private static final String ANIMAL_KEY = "ANIMAL";

    private FragmentManager mFragmentManager;
    private BottomBar mBottomBar;
    private ActionBar actionBar;
    private Menu menu;

    private boolean editMode = false;
    private boolean creation;

    private AnimalBasicInfo info;
    private AnimalLocation location;
    private AnimalExpert expert;
    private Animal animal;

    private DBHelper dbHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        this.menu = menu;

        if(animal == null || editMode) {
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

        dbHelper = DBHelper.getHelper(this);

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
            expert = new AnimalExpert();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.contentFragment, expert, NewAnimal.EXPERT_KEY);
            transaction.hide(expert);
            transaction.add(R.id.contentFragment, location, NewAnimal.LOCATION_KEY);
            transaction.hide(location);
            transaction.add(R.id.contentFragment, info, NewAnimal.INFO_KEY);
            transaction.commit();
        } else {
            animal = (Animal)  savedInstanceState.getSerializable("animal");
            editMode = savedInstanceState.getBoolean("editable");
            info = (AnimalBasicInfo) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.INFO_KEY);
            location = (AnimalLocation) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.LOCATION_KEY);
            expert = (AnimalExpert) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.EXPERT_KEY);

            creation = false;
        }

        if (actionBar != null) {
            if(animal == null) {
                actionBar.setTitle(R.string.add_animal);
            } else {
                if(!editMode)
                    actionBar.setTitle(R.string.view_animal);
                else
                    actionBar.setTitle(R.string.edit_animal);
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
                        if(!info.isVisible()) {
                            transaction.show(info);
                            transaction.hide(location);
                            transaction.hide(expert);
                        }
                        break;
                    case R.id.bb_menu_location:
                        if(!location.isVisible()) {
                            transaction.show(location);
                            transaction.hide(info);
                            transaction.hide(expert);
                            location.initializeMyLocationPermission();
                        }
                        break;
                    case R.id.bb_menu_new_animal_group:
                        transaction.replace(R.id.contentFragment, null, "GROUP");
                        break;
                    case R.id.bb_menu_new_animal_experts:
                        if(!expert.isVisible()) {
                            transaction.show(expert);
                            transaction.hide(info);
                            transaction.hide(location);
                        }
                        break;
                }

                transaction.commit();
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                // TODO do nothing here.
            }
        });

        mBottomBar.mapColorForTab(0, "#009688");
        mBottomBar.mapColorForTab(1, "#009688");
        mBottomBar.mapColorForTab(2, "#009688");
        mBottomBar.mapColorForTab(3, "#009688");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == android.R.id.home) {
            if(animal == null) {
                this.finish();
            } else {
                sendEditRequest();
            }
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
                for (Fragment fragment : mFragmentManager.getFragments()) {
                    if (fragment instanceof AnimalBasicInfo) {
                        animal = ((AnimalBasicInfo) fragment).setAnimalBasicInfo(animal);
                    } else  if(fragment instanceof AnimalLocation) {
                        animal = ((AnimalLocation) fragment).setAnimalLocation(animal);
                    }
                }
                if(!TextUtils.isEmpty(animal.getName())) {
                    dbHelper.updateAnimal(animal);
                    actionBar.setTitle(getResources().getString(R.string.view_animal));
                    this.menu.clear();
                    this.getMenuInflater().inflate(R.menu.view_animal_menu, menu);
                    this.editMode = false;
                    this.info.setEditable(editMode);
                    this.location.setEditable(editMode);
                } else {
                    Toast.makeText(NewAnimal.this,
                            this.getResources().getString(R.string.invalid_name),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else if(menuItem.getItemId() == R.id.edit) {
            actionBar.setTitle(getResources().getString(R.string.edit_animal));
            this.menu.clear();
            this.getMenuInflater().inflate(R.menu.new_animal_menu, menu);
            this.editMode = true;
            this.info.setEditable(editMode);
            this.location.setEditable(editMode);
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mFragmentManager.putFragment(outState, NewAnimal.INFO_KEY, info);
        mFragmentManager.putFragment(outState, NewAnimal.LOCATION_KEY, location);
        mFragmentManager.putFragment(outState, NewAnimal.EXPERT_KEY, expert);

        mBottomBar.onSaveInstanceState(outState);
        outState.putSerializable("animal", animal);
        outState.putBoolean("editable", editMode);
    }

    private void sendEditRequest() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("editAnimal", animal);
        this.setResult(AppCompatActivity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(animal != null)
            sendEditRequest();
        super.onBackPressed();
    }
}
