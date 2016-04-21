package com.creations.meister.jungleexplorer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
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

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.activity.NewAnimal;
import com.creations.meister.jungleexplorer.activity.NewAnimalGroupList;
import com.creations.meister.jungleexplorer.adapter.DomainAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.domain.Group;

import java.util.ArrayList;
import java.util.Collections;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/15/16.
 */
public class AnimalGroup extends ListFragment implements ActionMode.Callback {

    private PinnedHeaderListView mListView;
    private FloatingActionButton fabAddGroup;
    private LayoutInflater mInflater;
    private ActionMode mActionMode;

    private final static String ANIMAL_KEY = "ANIMAL";
    private final static String GROUP_KEY = "GROUP";
    private final static int GROUP_REQUEST = 0;
    private ArrayList<Domain> groups;

    private boolean editable = false;
    private boolean destroyActionMode = false;
    private boolean isFiltered = false;
    private Animal animal;

    private DomainAdapter mAdapter;
    private DBHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_list_fragment, container, false);

        mInflater = inflater;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            editable = savedInstanceState.getBoolean("editable");
            animal = (Animal) savedInstanceState.getSerializable("animal");
            groups = (ArrayList<Domain>) savedInstanceState.getSerializable(AnimalGroup.GROUP_KEY);
        } else {
            Bundle bundle = this.getActivity().getIntent().getExtras();
            if(bundle != null) {
                animal = (Animal) bundle.get(ANIMAL_KEY);
            } else {
                editable = true;
                groups = new ArrayList<>();
                AppCompatTextView et = (AppCompatTextView) this.getView().findViewById(android.R.id.empty);
                et.setText(this.getResources().getString(R.string.no_groups_animal));
            }
        }

        dbHelper = DBHelper.getHelper(this.getActivity());

        if(groups == null && animal != null) {
            groups = (ArrayList) dbHelper.getAllAnimalGroups(animal);
            Collections.sort(groups);
        }

        this.mListView = ((PinnedHeaderListView)this.getListView());
        mListView.setPinnedHeaderView(mInflater.inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mAdapter = new DomainAdapter(this.getContext(), groups);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this.getActivity(),android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEnableHeaderTransparencyChanges(false);

        this.fabAddGroup = (FloatingActionButton) this.getView().findViewById(R.id.groupFAB);
        this.fabAddGroup.setBackgroundTintList(ColorStateList.valueOf(
                this.getResources().getColor(R.color.colorAnimal)));
        this.fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menuIntent = new Intent(AnimalGroup.this.getContext(),
                        NewAnimalGroupList.class);
                startActivityForResult(menuIntent, GROUP_REQUEST);
            }
        });

        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mActionMode != null && editable) {
                    if(isFiltered) {
                        int groupId = ((DomainAdapter.ViewHolder) view.getTag()).id;
                        for(int i = 0; i < groups.size(); i++) {
                            if(groups.get(i).getId() == groupId) {
                                onListItemSelect(i);
                            }
                        }
                    } else {
                        AnimalGroup.this.onListItemSelect(position);
                    }
                }
            }
        });

        this.mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(editable) {
                    if(isFiltered) {
                        int groupId = ((DomainAdapter.ViewHolder) view.getTag()).id;
                        for(int i = 0; i < groups.size(); i++) {
                            if(groups.get(i).getId() == groupId) {
                                onListItemSelect(i);
                            }
                        }

                    } else {
                        onListItemSelect(position);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        SearchView sv = ((NewAnimal) this.getActivity()).getSearchView();

        if(sv != null) {
            this.mAdapter.getFilter().filter(sv.getQuery());
            this.mAdapter.setHeaderViewVisible(TextUtils.isEmpty(sv.getQuery()));
        }

        this.setEditable(editable);
    }

    private void onListItemSelect(int position) {
        mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = this.getActivity().startActionMode(this);
        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            destroyActionMode = true;
            mActionMode.finish();
        }

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " selected");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GROUP_REQUEST
                && resultCode == AppCompatActivity.RESULT_OK)
        {
            boolean exists = false;
            Group newGroup = (Group) data.getExtras().getSerializable("newAnimalGroup");
            for(Domain searchGroup : groups) {
                if(newGroup.getId() == (searchGroup).getId()) {
                    exists = true;
                }
            }
            if(!exists) {
                groups.add(newGroup);
                Collections.sort(groups);
                mAdapter.setData(groups);
                mListView.setAdapter(mAdapter);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("editable", editable);
        outState.putSerializable("animal", animal);
        outState.putSerializable(AnimalGroup.GROUP_KEY, groups);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if(editable) {
            this.fabAddGroup.setVisibility(View.VISIBLE);
        } else {
            this.fabAddGroup.setVisibility(View.INVISIBLE);
        }
    }

    public Animal setAnimalGroups(Animal newAnimal) {
        newAnimal.setAnimalGroups((ArrayList) groups);
        return newAnimal;
    }

    public void hideActionMode() {
        if(mAdapter != null && mAdapter.getSelectedCount() > 0 && mActionMode != null) {
            destroyActionMode = false;
            mActionMode.finish();
        }
    }

    public void showActionMode() {
        if(mAdapter != null && mAdapter.getSelectedCount() > 0 && mActionMode != null) {
            mActionMode = this.getActivity().startActionMode(this);
            mActionMode.setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " selected");
        }
    }

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
            for(int i = groups.size(); i >= 0; i--) {
                if(selectedIDs.get(i)){
                    groups.remove(i);
                }
            }
            mAdapter.notifyDataSetChanged();
            ((NewAnimal) this.getActivity()).filterClean();

        }
        destroyActionMode = true;
        mActionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if(destroyActionMode) {
            mAdapter.removeSelection();
            mActionMode = null;
        }
    }

    public DomainAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
    }
}