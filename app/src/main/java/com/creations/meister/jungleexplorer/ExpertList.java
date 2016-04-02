package com.creations.meister.jungleexplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.creations.meister.jungleexplorer.activities.ContactList;
import com.creations.meister.jungleexplorer.adapter.DomainAdapter;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.domain.Expert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/1/16.
 */
public class ExpertList extends ListFragment implements AdapterView.OnItemClickListener {

    private PinnedHeaderListView mListView;
    private FloatingActionButton fabAddExpert;
    private LayoutInflater mInflater;
    private ContactList contactList;

    private DomainAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expert_list_fragment, container, false);

        mInflater = inflater;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        this.mListView = ((PinnedHeaderListView)this.getListView());
        this.mListView.setOnItemClickListener(this);
        mListView.setPinnedHeaderView(mInflater.inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mAdapter = new DomainAdapter(this.getContext(), animals);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this.getActivity(),android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEnableHeaderTransparencyChanges(false);

        this.fabAddExpert = (FloatingActionButton) this.getView().findViewById(R.id.expertFAB);
        this.fabAddExpert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menuIntent = new Intent(ExpertList.this.getContext(), ContactList.class);
                startActivityForResult(menuIntent, 0);
            }
        });

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
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    private boolean checkContactsReadPermission()
    {
        String permission="android.permission.READ_CONTACTS";
        int res = this.getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private ArrayList<Domain> getExperts()
    {
        ArrayList<Domain> result=new ArrayList<>();

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
        return result;
    }
}
