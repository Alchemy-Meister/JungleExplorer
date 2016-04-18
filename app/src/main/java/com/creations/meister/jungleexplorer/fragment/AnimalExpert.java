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
import android.widget.Toast;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.activity.NewAnimal;
import com.creations.meister.jungleexplorer.activity.NewAnimalExpertList;
import com.creations.meister.jungleexplorer.adapter.ContactAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.domain.Expert;

import java.util.ArrayList;
import java.util.Collections;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/15/16.
 */
public class AnimalExpert extends ListFragment implements ActionMode.Callback {

    private PinnedHeaderListView mListView;
    private FloatingActionButton fabAddExpert;
    private LayoutInflater mInflater;
    private ActionMode mActionMode;

    private final static String ANIMAL_KEY = "ANIMAL";
    private final static String EXPERT_KEY = "EXPERT";
    private final static int EXPERT_REQUEST = 0;
    private ArrayList<Domain> experts;

    private boolean editable = false;
    private boolean destroyActionMode = false;
    private boolean isFiltered = false;
    private Animal animal;

    private ContactAdapter mAdapter;
    private DBHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expert_list_fragment, container, false);

        mInflater = inflater;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            editable = savedInstanceState.getBoolean("editable");
            animal = (Animal) savedInstanceState.getSerializable("animal");
            experts = (ArrayList<Domain>) savedInstanceState.getSerializable("experts");
        } else {
            Bundle bundle = this.getActivity().getIntent().getExtras();
            if(bundle != null) {
                animal = (Animal) bundle.get(ANIMAL_KEY);
            } else {
                editable = true;
                experts = new ArrayList<>();
                AppCompatTextView et = (AppCompatTextView) this.getActivity().findViewById(android.R.id.empty);
                et.setText(this.getResources().getString(R.string.no_experts_animal));
            }
        }

        dbHelper = DBHelper.getHelper(this.getActivity());

        if(experts == null && animal != null) {
            experts = (ArrayList) dbHelper.getAllAnimalExperts(animal);
            Collections.sort(experts);
        }

        this.mListView = ((PinnedHeaderListView)this.getListView());
        mListView.setPinnedHeaderView(mInflater.inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mAdapter = new ContactAdapter(this.getContext(), experts);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this.getActivity(),android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEnableHeaderTransparencyChanges(false);

        this.fabAddExpert = (FloatingActionButton) this.getView().findViewById(R.id.expertFAB);
        this.fabAddExpert.setBackgroundTintList(ColorStateList.valueOf(
                this.getResources().getColor(R.color.colorAnimal)));
        this.fabAddExpert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menuIntent = new Intent(AnimalExpert.this.getContext(),
                        NewAnimalExpertList.class);
                startActivityForResult(menuIntent, EXPERT_REQUEST);
            }
        });

        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mActionMode != null && editable) {
                if(isFiltered) {
                    int expertId = ((ContactAdapter.ViewHolder) view.getTag()).id;
                    for(int i = 0; i < experts.size(); i++) {
                        if(experts.get(i).getId() == expertId) {
                            onListItemSelect(i);
                        }
                    }
                } else {
                    AnimalExpert.this.onListItemSelect(position);
                }
            }
            }
        });

        this.mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(editable) {
                    if(isFiltered) {
                        int expertId = ((ContactAdapter.ViewHolder) view.getTag()).id;
                        for(int i = 0; i < experts.size(); i++) {
                            if(experts.get(i).getId() == expertId) {
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

        if(requestCode == EXPERT_REQUEST
                && resultCode == AppCompatActivity.RESULT_OK)
        {
            boolean exists = false;
            Expert newExpert = (Expert) data.getExtras().getSerializable("newAnimalExpert");
            for(Domain searchExpert : experts) {
                if(newExpert.getContactUri().equals(((Expert)searchExpert).getContactUri())) {
                    exists = true;
                }
            }
            if(!exists) {
                experts.add(newExpert);
                Collections.sort(experts);
                mAdapter.setData(experts);
                mListView.setAdapter(mAdapter);
                Toast.makeText(this.getContext(), getResources().getString(R.string.expert_saved), Toast.LENGTH_SHORT).show();
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
        outState.putSerializable("experts", experts);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if(editable) {
            this.fabAddExpert.setVisibility(View.VISIBLE);
        } else {
            this.fabAddExpert.setVisibility(View.INVISIBLE);
        }
    }

    public Animal setAnimalExperts(Animal newAnimal) {
        newAnimal.setAnimalExperts((ArrayList) experts);
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
            for(int i = experts.size(); i >= 0; i--) {
                if(selectedIDs.get(i)){
                    experts.remove(i);
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

    public ContactAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
    }
}