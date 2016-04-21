package com.creations.meister.jungleexplorer.fragment;

import android.Manifest;
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
import com.creations.meister.jungleexplorer.activity.ContactList;
import com.creations.meister.jungleexplorer.activity.MainActivity;
import com.creations.meister.jungleexplorer.adapter.ContactAdapter;
import com.creations.meister.jungleexplorer.adapter.DomainAdapter;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.domain.Expert;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;

import java.util.ArrayList;
import java.util.Collections;

import lb.library.PinnedHeaderListView;

/**
 * Created by meister on 4/1/16.
 */
public class ExpertList extends ListFragment implements ActionMode.Callback {

    private PinnedHeaderListView mListView;
    private FloatingActionButton fabAddExpert;
    private LayoutInflater mInflater;

    private final static int EXPERT_REQUEST = 0;
    private ArrayList<Domain> experts;

    private boolean isFiltered = false;
    private boolean destroyActionMode = true;

    private ContactAdapter mAdapter;
    private DBHelper dbHelper;

    private ActionMode mActionMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expert_list_fragment, container, false);

        mInflater = inflater;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = DBHelper.getHelper(this.getActivity());

        experts = (ArrayList) dbHelper.getAllExperts();
        Collections.sort(experts);

        this.mListView = ((PinnedHeaderListView)this.getListView());
        mListView.setPinnedHeaderView(mInflater.inflate(
                R.layout.pinned_header_listview_side_header, mListView, false));

        mAdapter = new ContactAdapter(this.getContext(), experts);

        if(!RuntimePermissionsHelper.hasPermissions(this.getContext(),
                    Manifest.permission.READ_CONTACTS))
        {
            mAdapter.loadImages(false);
        }

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
                startActivityForResult(menuIntent, EXPERT_REQUEST);
            }
        });

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
                        for (int i = 0; i < experts.size(); i++) {
                            if (experts.get(i).getId() == expertId) {
                                onListItemSelect(i);
                            }
                        }
                    } else {
                        ExpertList.this.onListItemSelect(position);
                    }
                }
            }
        });

        this.mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (isFiltered) {
                    int expertId = ((ContactAdapter.ViewHolder) view.getTag()).id;
                    for (int i = 0; i < experts.size(); i++) {
                        if (experts.get(i).getId() == expertId) {
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
            mActionMode = this.getActivity().startActionMode(ExpertList.this);
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
        if(mAdapter != null && mListView != null) {
            if (RuntimePermissionsHelper.hasPermissions(this.getContext(),
                    Manifest.permission.READ_CONTACTS)) {
                mAdapter.loadImages(true);
            } else {
                mAdapter.loadImages(false);
            }
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EXPERT_REQUEST
                && resultCode == AppCompatActivity.RESULT_OK)
        {
            experts.add((Domain) data.getExtras().getSerializable("newContact"));
            Collections.sort(experts);
            mAdapter.setData(experts);
            mListView.setAdapter(mAdapter);
            Toast.makeText(this.getContext(), getResources().getString(R.string.expert_saved),
                    Toast.LENGTH_SHORT).show();
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

    public ContactAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
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
                    dbHelper.removeExpert((Expert) experts.get(i));
                    experts.remove(i);
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
}
