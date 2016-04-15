package com.creations.meister.jungleexplorer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.adapter.ContactAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.domain.Expert;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;
import com.creations.meister.jungleexplorer.utils.ContactsQuery;

import java.util.ArrayList;
import java.util.Collections;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/15/16.
 */
public class NewAnimalExpertList extends AppCompatActivity implements AdapterView.OnItemClickListener {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    private PinnedHeaderListView mListView;
    private ContactAdapter mAdapter;
    private SearchView searchView;

    private static final String PERMISSION = "permission";
    private boolean alreadyAskedForPermission = false;

    private ArrayList<Domain> contacts;

    private DBHelper dbHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

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

        if(savedInstanceState != null) {
            alreadyAskedForPermission= savedInstanceState.getBoolean(PERMISSION, false);
        }

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.contacts);

        this.initializeContacts();

        this.mListView = (PinnedHeaderListView) this.findViewById(R.id.list);
        this.mListView.setEmptyView(this.findViewById(R.id.emptyText));
        this.mListView.setOnItemClickListener(this);
        mListView.setPinnedHeaderView(this.getLayoutInflater().inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mListView.setEnableHeaderTransparencyChanges(false);

        if(contacts != null) {
            this.initializeAdapter();
        }
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
        dbHelper.createExpert((Expert) contacts.get(position));
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newContact", contacts.get(position));
        this.setResult(AppCompatActivity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                alreadyAskedForPermission = false;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    contacts = this.getExperts();
                    this.initializeAdapter();
                } else {
                    RuntimePermissionsHelper.showMessageOKCancel(getResources().getString(
                            R.string.contact_permission_message,
                            getResources().getString(R.string.app_name)), NewAnimalExpertList.this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initializeContacts() {
        if(alreadyAskedForPermission) {
            return;
        }

        int hasReadContactsPermission = ContextCompat.checkSelfPermission(NewAnimalExpertList.this,
                Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewAnimalExpertList.this,
                    requiredPermissions,
                    REQUEST_CODE_ASK_PERMISSIONS);
            alreadyAskedForPermission = true;
            return;
        }
        contacts = getExperts();

    }

    private void initializeAdapter() {
        Collections.sort(contacts);

        mAdapter = new ContactAdapter(this, contacts);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this, android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(RuntimePermissionsHelper.hasPermissions(NewAnimalExpertList.this, requiredPermissions)) {
            contacts = this.getExperts();
            this.initializeAdapter();
        }
    }

    private ArrayList<Domain> getExperts()
    {
        ArrayList<Domain> result=new ArrayList<>();

        Uri uri = ContactsQuery.CONTENT_URI;
        final Cursor cursor = this.getContentResolver().query(
                uri,
                ContactsQuery.PROJECTION,
                ContactsQuery.SELECTION,
                null,
                ContactsQuery.SORT_ORDER);

        if(cursor == null)
            return null;
        while(cursor.moveToNext())
        {
            Expert expert = new Expert();
            expert.setContactUri(ContactsContract.Contacts.getLookupUri(
                    cursor.getLong(ContactsQuery.ID),
                    cursor.getString(ContactsQuery.LOOKUP_KEY)));
            expert.setName(cursor.getString(ContactsQuery.DISPLAY_NAME));
            expert.setPhotoId(cursor.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA));
            result.add(expert);
        }
        return result;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PERMISSION, alreadyAskedForPermission);
    }
}
