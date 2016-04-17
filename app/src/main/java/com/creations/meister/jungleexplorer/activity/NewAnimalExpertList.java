package com.creations.meister.jungleexplorer.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/15/16.
 */
public class NewAnimalExpertList extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private PinnedHeaderListView mListView;
    private ContactAdapter mAdapter;
    private SearchView searchView;

    private ArrayList<Domain> contacts;

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
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // this is your adapter that will be filtered
                mAdapter.getFilter().filter(query);
                mAdapter.setHeaderViewVisible(TextUtils.isEmpty(query));
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.contact_list_frament);

        dbHelper = DBHelper.getHelper(this);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.experts);



        this.mListView = (PinnedHeaderListView) this.findViewById(R.id.list);
        this.mListView.setEmptyView(this.findViewById(R.id.emptyText));
        this.mListView.setOnItemClickListener(this);
        mListView.setPinnedHeaderView(this.getLayoutInflater().inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mListView.setEnableHeaderTransparencyChanges(false);

        contacts = (ArrayList) dbHelper.getAllExperts();
        Collections.sort(contacts);

        TextView et = (TextView) this.findViewById(R.id.emptyText);
        et.setText(this.getResources().getString(R.string.no_experts));

        mAdapter = new ContactAdapter(this, contacts);
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
        resultIntent.putExtra("newAnimalExpert", contacts.get(position));
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
