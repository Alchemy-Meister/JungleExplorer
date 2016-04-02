package com.creations.meister.jungleexplorer;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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

    private PinnedHeaderListView mListView;
    private NewAnimal newAnimal;

    private DomainAdapter mAdapter;
    private SearchView searchView;

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

        this.requestPermission();

        // TODO set list adapter.
        final ArrayList<Domain> animals = getExperts();
        Collections.sort(animals, new Comparator<Domain>() {

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

        this.mListView = (PinnedHeaderListView) this.findViewById(R.id.list);
        this.mListView.setEmptyView(this.findViewById(R.id.emptyText));
        this.mListView.setOnItemClickListener(this);
        mListView.setPinnedHeaderView(this.getLayoutInflater().inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mAdapter = new DomainAdapter(this, animals);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this,android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEnableHeaderTransparencyChanges(false);
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

    private boolean checkContactsReadPermission()
    {
        String permission="android.permission.READ_CONTACTS";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private void requestPermission() {
        int hasReadContactsPermission = this.checkSelfPermission(
                Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[] {Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }

    }

    private ArrayList<Domain> getExperts()
    {
        ArrayList<Domain> result=new ArrayList<>();
        if(checkContactsReadPermission())
        {
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
        } else {

            Random r=new Random();
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<1000;++i)
            {
                Expert animal = new Expert();
                sb.delete(0,sb.length());
                int strLength=r.nextInt(10)+1;
                for(int j=0;j<strLength;++j)
                    switch(r.nextInt(3))
                    {
                        case 0:
                            sb.append((char)('a'+r.nextInt('z'-'a')));
                            break;
                        case 1:
                            sb.append((char)('A'+r.nextInt('Z'-'A')));
                            break;
                        case 2:
                            sb.append((char)('0'+r.nextInt('9'-'0')));
                            break;
                    }

                animal.setName(sb.toString());
                result.add(animal);
            }
        }
        return new ArrayList<>();
    }
}
