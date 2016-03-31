package com.creations.meister.jungleexplorer;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

public class MainActivity extends AppCompatActivity {
    private BottomBar mBottomBar;
    private TextView mMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageView = (TextView) findViewById(R.id.messageView);

        mBottomBar = BottomBar.attach(this, savedInstanceState);

        mBottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                mMessageView.setText(getMessage(menuItemId, false));
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                Toast.makeText(getApplicationContext(), getMessage(menuItemId, true), Toast.LENGTH_SHORT).show();
            }
        });

        mBottomBar.mapColorForTab(0, "#8BC34A");
        mBottomBar.mapColorForTab(1, "#009688");
        mBottomBar.mapColorForTab(2, "#F44336");
        mBottomBar.mapColorForTab(3, "#2196F3");

    }

    private String getMessage(int menuItemId, boolean isReselection) {
        String message = "Content for ";

        switch (menuItemId) {
            case R.id.bb_menu_animal_group:
                message += "groups";
                break;
            case R.id.bb_menu_animals:
                message += "animals";
                break;
            case R.id.bb_menu_experts:
                message += "experts";
                break;
        }

        if (isReselection) {
            message += " WAS RESELECTED! YAY!";
        }

        return message;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
