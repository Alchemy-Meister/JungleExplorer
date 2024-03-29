package com.creations.meister.jungleexplorer.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.fragment.AnimalBasicInfo;
import com.creations.meister.jungleexplorer.fragment.AnimalExpert;
import com.creations.meister.jungleexplorer.fragment.AnimalGroup;
import com.creations.meister.jungleexplorer.fragment.AnimalLocation;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by meister on 4/1/16.
 */
public class NewAnimal extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String INFO_KEY = "INFO";
    private static final String LOCATION_KEY = "LOCATION";
    private static final String GROUP_KEY = "GROUP";
    private static final String EXPERT_KEY = "EXPERT";
    private static final String ANIMAL_KEY = "ANIMAL";

    private FragmentManager mFragmentManager;
    private BottomBar mBottomBar;
    private ActionBar actionBar;
    private Menu menu;

    private boolean editMode = false;
    private boolean creation = false;
    private boolean favorite = false;

    private AnimalBasicInfo info;
    private AnimalLocation location;
    private AnimalGroup group;
    private AnimalExpert expert;
    private Animal animal;

    private SearchView searchView;
    private MenuItem searchItem;
    private MenuItem favoriteItem;

    private DBHelper dbHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        this.menu = menu;

        if(animal == null || editMode) {
            menuInflater.inflate(R.menu.new_animal_menu, menu);
        } else {
            menuInflater.inflate(R.menu.view_animal_menu, menu);
            favoriteItem = menu.findItem(R.id.favorite);
            if(this.favorite) {
                favoriteItem.setIcon(this.getResources().getDrawable(R.drawable.ic_full_heart));
            }
        }

        searchItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(this);

        return true;
    }

    private void filterFragment(String query) {
        expert.getAdapter().getFilter().filter(query);
        expert.getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        group.getAdapter().getFilter().filter(query);
        group.getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        if(TextUtils.isEmpty(query)) {
            group.setFiltered(false);
            expert.setFiltered(false);
        } else {
            group.setFiltered(true);
            expert.setFiltered(true);
        }
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
                if(animal != null && animal.getFavorite() == 1) {
                    this.favorite = true;
                }
            } else {
                creation = true;
            }

            info = new AnimalBasicInfo();
            location = new AnimalLocation();
            group = new AnimalGroup();
            expert = new AnimalExpert();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.contentFragment, expert, NewAnimal.EXPERT_KEY);
            transaction.hide(expert);
            transaction.add(R.id.contentFragment, group, NewAnimal.GROUP_KEY);
            transaction.hide(group);
            transaction.add(R.id.contentFragment, location, NewAnimal.LOCATION_KEY);
            transaction.hide(location);
            transaction.add(R.id.contentFragment, info, NewAnimal.INFO_KEY);
            transaction.commit();
        } else {
            animal = (Animal)  savedInstanceState.getSerializable("animal");
            favorite = savedInstanceState.getBoolean("favorite");
            editMode = savedInstanceState.getBoolean("editable");
            creation = savedInstanceState.getBoolean("creation");
            info = (AnimalBasicInfo) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.INFO_KEY);
            location = (AnimalLocation) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.LOCATION_KEY);
            group = (AnimalGroup) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.GROUP_KEY);
            expert = (AnimalExpert) mFragmentManager.getFragment(
                    savedInstanceState, NewAnimal.EXPERT_KEY);
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
                            transaction.hide(group);
                            transaction.hide(expert);
                            group.hideActionMode();
                            expert.hideActionMode();
                            setVisibleSearchMenuItem(false);
                        }
                        break;
                    case R.id.bb_menu_location:
                        if(!location.isVisible()) {
                            transaction.show(location);
                            transaction.hide(info);
                            transaction.hide(group);
                            transaction.hide(expert);
                            location.initializeMyLocationPermission();
                            group.hideActionMode();
                            expert.hideActionMode();
                            setVisibleSearchMenuItem(false);
                        }
                        break;
                    case R.id.bb_menu_new_animal_group:
                        if(!group.isVisible()) {
                            transaction.show(group);
                            transaction.hide(info);
                            transaction.hide(location);
                            transaction.hide(expert);
                            group.showActionMode();
                            expert.hideActionMode();
                            setVisibleSearchMenuItem(true);
                        }
                        break;
                    case R.id.bb_menu_new_animal_experts:
                        if(!expert.isVisible()) {
                            transaction.show(expert);
                            transaction.hide(info);
                            transaction.hide(location);
                            transaction.hide(group);
                            expert.showActionMode();
                            group.hideActionMode();
                            setVisibleSearchMenuItem(true);
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
                    } else if(fragment instanceof AnimalLocation) {
                        newAnimal = ((AnimalLocation) fragment).setAnimalLocation(newAnimal);
                    } else if(fragment instanceof  AnimalGroup) {
                        newAnimal = ((AnimalGroup) fragment).setAnimalGroups(newAnimal);
                    } else if(fragment instanceof  AnimalExpert) {
                        newAnimal = ((AnimalExpert) fragment).setAnimalExperts(newAnimal);
                    }
                }

                if(!TextUtils.isEmpty(newAnimal.getName())) {
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
                    } else if(fragment instanceof AnimalLocation) {
                        animal = ((AnimalLocation) fragment).setAnimalLocation(animal);
                    } else if(fragment instanceof AnimalExpert) {
                        animal = ((AnimalExpert) fragment).setAnimalExperts(animal);
                    }
                }

                if(!TextUtils.isEmpty(animal.getName())) {
                    dbHelper.updateAnimal(animal);
                    actionBar.setTitle(getResources().getString(R.string.view_animal));
                    this.menu.clear();
                    this.getMenuInflater().inflate(R.menu.view_animal_menu, menu);

                    favoriteItem = this.menu.findItem(R.id.favorite);
                    if(this.favorite) {
                        favoriteItem.setIcon(this.getResources().getDrawable(R.drawable.ic_full_heart));
                    }

                    searchItem = this.menu.findItem(R.id.searchView);
                    searchView = (SearchView) searchItem.getActionView();
                    searchView.setQueryHint(getResources().getString(R.string.hint));
                    searchView.setOnQueryTextListener(this);

                    if(this.expert.isVisible()) {
                        this.setVisibleSearchMenuItem(true);
                    }

                    this.editMode = false;
                    this.info.setEditable(editMode);
                    this.location.setEditable(editMode);
                    this.expert.setEditable(editMode);
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

            MenuItem done = menu.findItem(R.id.done);
            done.setTitle(this.getResources().getString(R.string.save_changes));

            searchItem = this.menu.findItem(R.id.searchView);
            searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint(getResources().getString(R.string.hint));
            searchView.setOnQueryTextListener(this);

            if(this.expert.isVisible()) {
                this.setVisibleSearchMenuItem(true);
            }

            this.editMode = true;
            this.info.setEditable(editMode);
            this.location.setEditable(editMode);
            this.expert.setEditable(editMode);
        } else if(menuItem.getItemId() == R.id.favorite && favoriteItem != null) {
            if(this.favorite) {
                this.favorite = false;
                favoriteItem.setIcon(this.getResources().getDrawable(R.drawable.ic_border_heart));
                animal.setFavorite(0);
                dbHelper.updateAnimalFavorite(animal);

            } else {
                this.favorite = true;
                favoriteItem.setIcon(this.getResources().getDrawable(R.drawable.ic_full_heart));
                animal.setFavorite(1);
                dbHelper.updateAnimalFavorite(animal);
            }
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mFragmentManager.putFragment(outState, NewAnimal.INFO_KEY, info);
        mFragmentManager.putFragment(outState, NewAnimal.LOCATION_KEY, location);
        mFragmentManager.putFragment(outState, NewAnimal.GROUP_KEY, group);
        mFragmentManager.putFragment(outState, NewAnimal.EXPERT_KEY, expert);

        mBottomBar.onSaveInstanceState(outState);
        outState.putBoolean("favorite", favorite);
        outState.putBoolean("creation", creation);
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

    public SearchView getSearchView() {
        return this.searchView;
    }

    private void setVisibleSearchMenuItem(boolean visible) {
        if(visible) {
            if(this.searchItem != null) {
                this.searchView.setVisibility(View.VISIBLE);
                this.searchItem.setVisible(true);
            }
        } else {
            if (this.searchItem != null) {
                this.searchView.setVisibility(View.INVISIBLE);
                this.searchItem.setVisible(false);
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        filterFragment(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterFragment(newText);
        return true;
    }

    public void filterClean() {
        MenuItemCompat.collapseActionView(searchItem);
    }
}
