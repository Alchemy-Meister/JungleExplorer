package com.creations.meister.jungleexplorer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.activity.MainActivity;
import com.creations.meister.jungleexplorer.activity.NewAnimal;
import com.creations.meister.jungleexplorer.adapter.DomainAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 3/27/16.
 */
public class AnimalList extends ListFragment implements AdapterView.OnItemClickListener {

    private final int NEW_ANIMAL_REQUEST = 0;

    private PinnedHeaderListView mListView;
    private FloatingActionButton fabAddAnimal;
    private LayoutInflater mInflater;

    private ArrayList<Domain> animals;
    private DomainAdapter mAdapter;
    private DBHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.animal_list_fragment, container, false);

        mInflater = inflater;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = DBHelper.getHelper(this.getActivity());

        animals = (ArrayList) dbHelper.getAllAnimals();
        Collections.sort(animals);

        this.mListView = ((PinnedHeaderListView)this.getListView());
        this.mListView.setOnItemClickListener(this);
        mListView.setPinnedHeaderView(mInflater.inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mAdapter = new DomainAdapter(this.getContext(), animals);
        int pinnedHeaderBackgroundColor=getResources().getColor(AnimalList.getResIdFromAttribute(
                this.getActivity(), android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEnableHeaderTransparencyChanges(false);

        this.fabAddAnimal = (FloatingActionButton) this.getActivity().findViewById(R.id.animalFAB);
        this.fabAddAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menuIntent = new Intent(AnimalList.this.getContext(), NewAnimal.class);
                startActivityForResult(menuIntent, NEW_ANIMAL_REQUEST);
            }
        });

        SearchView sv = ((MainActivity) this.getActivity()).getSearchView();

        if(sv != null) {
            this.mAdapter.getFilter().filter(sv.getQuery());
            this.mAdapter.setHeaderViewVisible(TextUtils.isEmpty(sv.getQuery()));
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
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<Domain> getAnimals()
    {
        ArrayList<Domain> result=new ArrayList<>();
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<1000;++i)
        {
            Domain animal = new Domain();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == NEW_ANIMAL_REQUEST
                && resultCode == AppCompatActivity.RESULT_OK)
        {
            animals.add((Domain) data.getExtras().getSerializable("newAnimal"));
            Collections.sort(animals);
            AnimalList.this.mAdapter.setData(animals);
            mListView.setAdapter(mAdapter);
        } else {

        }
    }

    public DomainAdapter getAdapter() {
        return this.mAdapter;
    }
}
