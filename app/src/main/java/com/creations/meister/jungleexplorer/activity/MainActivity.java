package com.creations.meister.jungleexplorer.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class MainActivity extends AppCompatActivity {

    private BottomBar mBottomBar;
    private FragmentManager mFragmentManager;

    private GroupList mGroupList;
    private AnimalList mAnimalList;
    private FavoriteList mFavoriteList;
    private ExpertList mExpertList;

    private SearchView searchView;

    private boolean showAnimals = false;
    private static final String SHOW_ANIMALS = "SHOW_ANIMALS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            Bundle bundle = this.getIntent().getExtras();
            if(bundle != null) {
                showAnimals = bundle.getBoolean(SHOW_ANIMALS);
            }
        }

        mFragmentManager = this.getSupportFragmentManager();
        mBottomBar = BottomBar.attach(this, savedInstanceState);

        mGroupList = new GroupList();
        mAnimalList = new AnimalList();
        mFavoriteList = new FavoriteList();
        mExpertList = new ExpertList();

        mBottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                switch (menuItemId) {
                    case R.id.bb_menu_animal_group:
                        transaction.replace(R.id.contentFragment, mGroupList, "GROUP");
                        break;
                    case R.id.bb_menu_animals:
                        transaction.replace(R.id.contentFragment, mAnimalList, "ANIMAL");
                        break;
                    case R.id.bb_menu_favorites:
                        transaction.replace(R.id.contentFragment, mFavoriteList, "FAV");
                        break;
                    case R.id.bb_menu_experts:
                        transaction.replace(R.id.contentFragment, mExpertList, "EXPERT");
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
            transaction.replace(R.id.contentFragment, mAnimalList, "ANIMAL");
            transaction.commit();
            mBottomBar.selectTabAtPosition(1, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        });

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
        Fragment myFragment = mFragmentManager.findFragmentById(R.id.contentFragment);
        if(myFragment instanceof GroupList) {
            ((GroupList) myFragment).getAdapter().getFilter().filter(query);
            ((GroupList) myFragment).getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        } else if(myFragment instanceof AnimalList) {
            ((AnimalList) myFragment).getAdapter().getFilter().filter(query);
            ((AnimalList) myFragment).getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        } else if(myFragment instanceof FavoriteList) {
            ((FavoriteList) myFragment).getAdapter().getFilter().filter(query);
            ((FavoriteList) myFragment).getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        } else if(myFragment instanceof ExpertList) {
            ((ExpertList) myFragment).getAdapter().getFilter().filter(query);
            ((ExpertList) myFragment).getAdapter().setHeaderViewVisible(TextUtils.isEmpty(query));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }

    public SearchView getSearchView() {
        return this.searchView;
    }
}
