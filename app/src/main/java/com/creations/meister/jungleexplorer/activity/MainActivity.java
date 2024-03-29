package com.creations.meister.jungleexplorer.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.fragment.AnimalList;
import com.creations.meister.jungleexplorer.fragment.ExpertList;
import com.creations.meister.jungleexplorer.fragment.FavoriteList;
import com.creations.meister.jungleexplorer.fragment.GroupList;
import com.creations.meister.jungleexplorer.R;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        FavoriteList.AnimalListListener, AnimalList.AnimalListListener
{

    private static final String GROUP_KEY = "GROUP";
    private static final String ANIMAL_KEY = "ANIMAL";
    private static final String FAVORITE_KEY = "FAVORITE";
    private static final String EXPERT_KEY = "EXPERT";

    private BottomBar mBottomBar;
    private FragmentManager mFragmentManager;

    private GroupList mGroupList;
    private AnimalList mAnimalList;
    private FavoriteList mFavoriteList;
    private ExpertList mExpertList;

    private SearchView searchView;
    private MenuItem searchItem;

    private boolean showAnimals = false;
    private static final String SHOW_ANIMALS = "SHOW_ANIMALS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = this.getSupportFragmentManager();
        mBottomBar = BottomBar.attach(this, savedInstanceState);

        if(savedInstanceState == null) {
            Bundle bundle = this.getIntent().getExtras();
            if(bundle != null) {
                showAnimals = bundle.getBoolean(SHOW_ANIMALS);
            }

            mGroupList = new GroupList();
            mAnimalList = new AnimalList();
            mFavoriteList = new FavoriteList();
            mExpertList = new ExpertList();

            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.contentFragment, mExpertList, MainActivity.EXPERT_KEY);
            transaction.hide(mExpertList);
            transaction.add(R.id.contentFragment, mFavoriteList, MainActivity.FAVORITE_KEY);
            transaction.hide(mFavoriteList);
            transaction.add(R.id.contentFragment, mAnimalList, MainActivity.ANIMAL_KEY);
            transaction.hide(mAnimalList);
            transaction.add(R.id.contentFragment, mGroupList, MainActivity.GROUP_KEY);
            transaction.commit();
        } else {
            mGroupList = (GroupList) mFragmentManager.getFragment(
                    savedInstanceState, MainActivity.GROUP_KEY);
            mAnimalList = (AnimalList) mFragmentManager.getFragment(
                    savedInstanceState, MainActivity.ANIMAL_KEY);
            mFavoriteList = (FavoriteList) mFragmentManager.getFragment(
                    savedInstanceState, MainActivity.FAVORITE_KEY);
            mExpertList = (ExpertList) mFragmentManager.getFragment(
                    savedInstanceState, MainActivity.EXPERT_KEY);
        }

        mBottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                switch (menuItemId) {
                    case R.id.bb_menu_animal_group:
                        if(!mGroupList.isVisible()) {
                            transaction.show(mGroupList);
                            transaction.hide(mAnimalList);
                            transaction.hide(mFavoriteList);
                            transaction.hide(mExpertList);
                            mAnimalList.hideActionMode();
                            mFavoriteList.hideActionMode();
                            mExpertList.hideActionMode();
                        }
                        break;
                    case R.id.bb_menu_animals:
                        if(!mAnimalList.isVisible()) {
                            transaction.show(mAnimalList);
                            transaction.hide(mGroupList);
                            transaction.hide(mFavoriteList);
                            transaction.hide(mExpertList);
                            mAnimalList.showActionMode();
                            mFavoriteList.hideActionMode();
                            mExpertList.hideActionMode();
                        }
                        break;
                    case R.id.bb_menu_favorites:
                        if(!mFavoriteList.isVisible()) {
                            transaction.show(mFavoriteList);
                            transaction.hide(mGroupList);
                            transaction.hide(mAnimalList);
                            transaction.hide(mExpertList);
                            mAnimalList.hideActionMode();
                            mFavoriteList.showActionMode();
                            mExpertList.hideActionMode();
                        }
                        break;
                    case R.id.bb_menu_experts:
                        if(!mExpertList.isVisible()) {
                            transaction.show(mExpertList);
                            transaction.hide(mGroupList);
                            transaction.hide(mAnimalList);
                            transaction.hide(mFavoriteList);
                            mExpertList.showActionMode();
                            mAnimalList.hideActionMode();
                            mFavoriteList.hideActionMode();
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

        mBottomBar.mapColorForTab(0, "#8BC34A");
        mBottomBar.mapColorForTab(1, "#009688");
        mBottomBar.mapColorForTab(2, "#F44336");
        mBottomBar.mapColorForTab(3, "#2196F3");

        if(showAnimals) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.show(mAnimalList);
            transaction.hide(mGroupList);
            transaction.commit();
            mBottomBar.selectTabAtPosition(1, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        searchItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings) {
            Intent preferences = new Intent(this, PreferencesActivity.class);
            MainActivity.this.startActivity(preferences);
        }

        return super.onOptionsItemSelected(item);
    }

    private void filterFragment(String query) {
        mGroupList.getAdapter().getFilter().filter(query);
        mGroupList.getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        mAnimalList.getAdapter().getFilter().filter(query);
        mAnimalList.getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        mFavoriteList.getAdapter().getFilter().filter(query);
        mFavoriteList.getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        mExpertList.getAdapter().getFilter().filter(query);
        mExpertList.getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        if(TextUtils.isEmpty(query)) {
            mGroupList.setFiltered(false);
            mAnimalList.setFiltered(false);
            mFavoriteList.setFiltered(false);
            mExpertList.setFiltered(false);
        } else {
            mGroupList.setFiltered(true);
            mAnimalList.setFiltered(true);
            mFavoriteList.setFiltered(true);
            mExpertList.setFiltered(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mFragmentManager.putFragment(outState, MainActivity.GROUP_KEY, mGroupList);
        mFragmentManager.putFragment(outState, MainActivity.ANIMAL_KEY,  mAnimalList);
        mFragmentManager.putFragment(outState, MainActivity.FAVORITE_KEY, mFavoriteList);
        mFragmentManager.putFragment(outState, MainActivity.EXPERT_KEY,  mExpertList);

        mBottomBar.onSaveInstanceState(outState);
    }

    public SearchView getSearchView() {
        return this.searchView;
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

    @Override
    public void onAnimalListChanged(Animal animal) {
        mAnimalList.updateAnimal(animal);
    }


    @Override
    public void onAnimalRemove(Animal animal) {
        mFavoriteList.removeFavorite(animal);
    }
}
