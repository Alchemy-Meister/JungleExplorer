package com.creations.meister.jungleexplorer;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.creations.meister.jungleexplorer.adapter.DomainAdapter;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.domain.Expert;
import com.creations.meister.jungleexplorer.utils.ContactsQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/1/16.
 */
public class ContactList extends AppCompatActivity implements AdapterView.OnItemClickListener {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    private PinnedHeaderListView mListView;
    private DomainAdapter mAdapter;
    private SearchView searchView;

    private ArrayList<Domain> contacts;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.contact_list_frament);

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

    public static int getResIdFromAttribute(final Activity activity,final int attr)
    {
        if(attr==0)
            return 0;
        final TypedValue typedValue=new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.resourceId;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    contacts = this.getExperts();
                    this.initializeAdapter();
                } else {
                    showMessageOKCancel("You need to allow access to Contacts");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initializeContacts() {
        int hasReadContactsPermission = ContextCompat.checkSelfPermission(ContactList.this,
                Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ContactList.this,
                    requiredPermissions,
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        contacts = getExperts();

    }

    private void showMessageOKCancel(String message) {
        new AlertDialog.Builder(ContactList.this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNegativeButton("SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactList.this.goToSettings();
                    }
                })
                .create()
                .show();
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + this.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(myAppSettings);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(hasPermissions(requiredPermissions)) {
            contacts = this.getExperts();
            this.initializeAdapter();
        }
    }

    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                    ContactList.this, permission))
                return false;
        return true;
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

    private void initializeAdapter() {
        Collections.sort(contacts, new Comparator<Domain>() {

            @Override
            public int compare(Domain lhs, Domain rhs) {
                char lhsFirstLetter = TextUtils.isEmpty(lhs.getName()) ? ' ' : lhs.getName().charAt(0);
                char rhsFirstLetter = TextUtils.isEmpty(rhs.getName()) ? ' ' : rhs.getName().charAt(0);
                int firstLetterComparison = Character.toUpperCase(lhsFirstLetter) - Character.toUpperCase(rhsFirstLetter);
                if (firstLetterComparison == 0)
                    return lhs.getName().compareTo(rhs.getName());
                return firstLetterComparison;
            }
        });

        mAdapter = new DomainAdapter(this, contacts);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this, android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
    }
}
