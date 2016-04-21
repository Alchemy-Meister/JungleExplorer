package com.creations.meister.jungleexplorer.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import com.creations.meister.jungleexplorer.adapter.DomainAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.domain.Domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/2/16.
 */
public class FavoriteList extends ListFragment implements android.view.ActionMode.Callback {

    private PinnedHeaderListView mListView;
    private LayoutInflater mInflater;

    private boolean isFiltered = false;
    private boolean destroyActionMode = true;

    private ArrayList<Domain> animals;

    private DomainAdapter mAdapter;
    private DBHelper dbHelper;

    private ActionMode mActionMode;

    private AnimalListListener mAnimalCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mAnimalCallback = (AnimalListListener) this.getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(this.getActivity().toString()
                    + " must implement AnimalListListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorite_list_fragment, container, false);

        mInflater = inflater;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = DBHelper.getHelper(this.getActivity());

        animals = (ArrayList) dbHelper.getFavorites();
        Collections.sort(animals);

        this.mListView = ((PinnedHeaderListView)this.getListView());
        mListView.setPinnedHeaderView(mInflater.inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mAdapter=new DomainAdapter(this.getContext(), animals);
        int pinnedHeaderBackgroundColor=getResources().getColor(this.getResIdFromAttribute(
                this.getActivity(),android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEnableHeaderTransparencyChanges(false);

        SearchView sv = ((MainActivity) this.getActivity()).getSearchView();

        if(sv != null) {
            this.mAdapter.getFilter().filter(sv.getQuery());
            this.mAdapter.setHeaderViewVisible(TextUtils.isEmpty(sv.getQuery()));
        }

        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    view.setSelected(true);
                    if (isFiltered) {
                        int expertId = ((DomainAdapter.ViewHolder) view.getTag()).id;
                        for (int i = 0; i < animals.size(); i++) {
                            if (animals.get(i).getId() == expertId) {
                                onListItemSelect(i);
                            }
                        }
                    } else {
                        FavoriteList.this.onListItemSelect(position);
                    }
                }
            }
        });

        this.mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (isFiltered) {
                    int expertId = ((DomainAdapter.ViewHolder) view.getTag()).id;
                    for (int i = 0; i < animals.size(); i++) {
                        if (animals.get(i).getId() == expertId) {
                            onListItemSelect(i);
                        }
                    }
                } else {
                    onListItemSelect(position);
                }
                return true;
            }
        });
    }

    private void onListItemSelect(int position) {
        mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = this.getActivity().startActionMode(FavoriteList.this);
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
    public void onResume() {
        super.onResume();
        animals = (ArrayList) dbHelper.getFavorites();
        Collections.sort(animals);
        if(mAdapter != null && mListView != null) {
            mAdapter.setData(animals);
            mListView.setAdapter(mAdapter);
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

    public DomainAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
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
            for(int i = animals.size(); i >= 0; i--) {
                if(selectedIDs.get(i)){
                    ((Animal) animals.get(i)).setFavorite(0);
                    dbHelper.updateAnimalFavorite((Animal) animals.get(i));
                    mAnimalCallback.onAnimalListChanged((Animal) animals.get(i));
                    animals.remove(i);
                }
            }
            mAdapter.notifyDataSetChanged();
            ((MainActivity) this.getActivity()).filterClean();

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

    public void removeFavorite(Animal animal) {
        if(animal != null) {
            Iterator<Domain> animalIterator = animals.iterator();
            while(animalIterator.hasNext()) {
                Animal removeAnimal = (Animal) animalIterator.next();
                if(removeAnimal.getId() == animal.getId()) {
                    animalIterator.remove();
                }
            }
        }
    }

    public interface AnimalListListener {
        public void onAnimalListChanged(Animal animal);
    }
}
