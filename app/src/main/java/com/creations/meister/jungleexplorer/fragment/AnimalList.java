package com.creations.meister.jungleexplorer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.activity.MainActivity;
import com.creations.meister.jungleexplorer.activity.NewAnimal;
import com.creations.meister.jungleexplorer.adapter.DomainAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.domain.Domain;

import java.util.ArrayList;
import java.util.Collections;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 3/27/16.
 */
public class AnimalList extends ListFragment {

    private final int NEW_ANIMAL_REQUEST = 0;
    private final int ANIMAL_EDIT_REQUEST = 1;
    private final String ANIMAL_KEY = "ANIMAL";

    private PinnedHeaderListView mListView;
    private FloatingActionButton fabAddAnimal;
    private LayoutInflater mInflater;

    private ArrayList<Domain> animals;
    private DomainAdapter mAdapter;
    private DBHelper dbHelper;

    private ActionMode mActionMode;

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
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
            }
        });
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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mActionMode != null) {
                    onListItemSelect(position);
                } else {
                    Intent newAnimalIntent = new Intent(AnimalList.this.getContext(), NewAnimal.class);
                    newAnimalIntent.putExtra(ANIMAL_KEY, animals.get(position));
                    startActivityForResult(newAnimalIntent, ANIMAL_EDIT_REQUEST);
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                onListItemSelect(position);
                return true;
            }
        });

    }

    private void onListItemSelect(int position) {
        mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = this.getActivity().startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.cab_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if(item.getItemId() == R.id.action_delete) {
                        SparseBooleanArray selectedIDs = mAdapter.getSelectedIds();
                        for(int i = animals.size(); i >= 0; i--) {
                            if(selectedIDs.get(i)){
                                dbHelper.removeAnimal((Animal) animals.get(i));
                                animals.remove(i);
                            }
                        }
                        mAdapter.notifyDataSetChanged();

                    }
                    mActionMode.finish();
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mAdapter.removeSelection();
                    mActionMode = null;
                }
            });
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " selected");
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == NEW_ANIMAL_REQUEST
                && resultCode == AppCompatActivity.RESULT_OK)
        {
            animals.add((Domain) data.getExtras().getSerializable("newAnimal"));
            Collections.sort(animals);
            AnimalList.this.mAdapter.setData(animals);
            mListView.setAdapter(mAdapter);
            Toast.makeText(this.getContext(), getResources().getString(R.string.animal_saved), Toast.LENGTH_SHORT).show();
        }
    }

    public DomainAdapter getAdapter() {
        return this.mAdapter;
    }
}
