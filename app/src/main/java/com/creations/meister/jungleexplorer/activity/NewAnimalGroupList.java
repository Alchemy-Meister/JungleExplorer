package com.creations.meister.jungleexplorer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.adapter.ContactAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;

import java.util.ArrayList;
import java.util.Collections;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/15/16.
 */
public class NewAnimalGroupList extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private PinnedHeaderListView mListView;
    private ContactAdapter mAdapter;
    private SearchView searchView;

    private ArrayList<Domain> groups;
    private boolean isFiltered;

    private DBHelper dbHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu);

        final MenuItem searchItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                mAdapter.getFilter().filter(newText);
                mAdapter.setHeaderViewVisible(TextUtils.isEmpty(newText));
                if(TextUtils.isEmpty(newText)) {
                    isFiltered = false;
                } else {
                    isFiltered = true;
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // this is your adapter that will be filtered
                mAdapter.getFilter().filter(query);
                mAdapter.setHeaderViewVisible(TextUtils.isEmpty(query));
                if(TextUtils.isEmpty(query)) {
                    isFiltered = false;
                } else {
                    isFiltered = true;
                }
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.group_list_fragment);

        dbHelper = DBHelper.getHelper(this);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.animal_groups);

        FloatingActionButton groupFAB = (FloatingActionButton) this.findViewById(R.id.groupFAB);
        groupFAB.hide();

        this.mListView = (PinnedHeaderListView) this.findViewById(android.R.id.list);
        this.mListView.setEmptyView(this.findViewById(android.R.id.empty));
        this.mListView.setOnItemClickListener(this);
        mListView.setPinnedHeaderView(this.getLayoutInflater().inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mListView.setEnableHeaderTransparencyChanges(false);

        groups = (ArrayList) dbHelper.getAllGroups();
        Collections.sort(groups);

        TextView et = (TextView) this.findViewById(android.R.id.empty);
        et.setText(this.getResources().getString(R.string.no_groups));

        mAdapter = new ContactAdapter(this, groups);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this, android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchView.setQuery(query, false);
            if (!TextUtils.isEmpty(searchView.getQuery())) {
                searchView.post(new Runnable() {
                    @Override
                    public void run() {
                        searchView.setQuery(searchView.getQuery(), true);
                    }
                });
            }
        }
    }

    public static int getResIdFromAttribute(final Activity activity, final int attr)
    {
        if(attr==0)
            return 0;
        final TypedValue typedValue=new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.resourceId;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent resultIntent = new Intent();
        if(isFiltered) {
            int groupId = ((ContactAdapter.ViewHolder) view.getTag()).id;
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i).getId() == groupId) {
                    resultIntent.putExtra("newAnimalGroup", groups.get(i));
                }
            }
        } else {
            resultIntent.putExtra("newAnimalGroup", groups.get(position));
        }

        this.setResult(AppCompatActivity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
