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

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.fragment.AnimalBasicInfo;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by meister on 4/1/16.
 */
public class NewAnimal extends AppCompatActivity {

    private FragmentManager mFragmentManager;
    private BottomBar mBottomBar;
    private AnimalBasicInfo info;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_animal_menu, menu);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.add_animal);
        }
        mFragmentManager = this.getSupportFragmentManager();
        mBottomBar = BottomBar.attach(this, savedInstanceState);

        info = new AnimalBasicInfo();

        mBottomBar.setItemsFromMenu(R.menu.new_anima_bottombar, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                switch (menuItemId) {
                    case R.id.bb_menu_info:
                        transaction.replace(R.id.contentFragment, info, "INFO");
                        break;
                    case R.id.bb_menu_location:
                        transaction.replace(R.id.contentFragment, null, "LOCATION");
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
            Animal newAnimal = new Animal();
            for (Fragment fragment : mFragmentManager.getFragments()) {
                if (fragment instanceof AnimalBasicInfo) {
                    ((AnimalBasicInfo) fragment).setAnimalBasicInfo(newAnimal);
                }
            }
            DBHelper.getHelper(NewAnimal.this).insertAnimal(newAnimal);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newAnimal", newAnimal);
            if(!TextUtils.isEmpty(newAnimal.getName())) {
                this.setResult(AppCompatActivity.RESULT_OK, resultIntent);
            } else {
                this.setResult(AppCompatActivity.RESULT_CANCELED);
            }
            this.finish();
        }

        return super.onOptionsItemSelected(menuItem);
    }
}
