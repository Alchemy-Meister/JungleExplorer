package com.creations.meister.jungleexplorer;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

public class MainActivity extends AppCompatActivity {

    private BottomBar mBottomBar;
    private TextView mMessageView;
    private FragmentManager mFragmentManager;

    private GroupList groupListFragment;
    private AnimalList animalListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = this.getSupportFragmentManager();
        mBottomBar = BottomBar.attach(this, savedInstanceState);

        mBottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                ListFragment selectedFragment = null;
                switch (menuItemId) {
                    case R.id.bb_menu_animal_group:
                        selectedFragment = new GroupList();
                        break;
                    case R.id.bb_menu_animals:
                        selectedFragment = new AnimalList();

                        break;
                    case R.id.bb_menu_experts:
                        selectedFragment = new ExpertList();
                        break;
                }
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace(R.id.contentFragment, selectedFragment);
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

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
