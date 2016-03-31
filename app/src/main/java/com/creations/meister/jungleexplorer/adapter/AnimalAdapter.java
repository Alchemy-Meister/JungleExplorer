package com.creations.meister.jungleexplorer.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import com.creations.meister.jungleexplorer.domain.Animal;
import lb.library.SearchablePinnedHeaderListViewAdapter;

/**
 * Created by meister on 3/28/16.
 */
public class AnimalAdapter extends SearchablePinnedHeaderListViewAdapter<Animal> {

    private ArrayList<Animal> mAnimal;
//    private final int CONTACT_PHOTO_IMAGE_SIZE;
//    private final int[] PHOTO_TEXT_BACKGROUND_COLORS;
//    private final AsyncTaskThreadPool mAsyncTaskThreadPool=new AsyncTaskThreadPool(1,2,10);

    public AnimalAdapter(final ArrayList<Animal> animal)
    {
//        setData(contacts);
        //PHOTO_TEXT_BACKGROUND_COLORS=getResources().getIntArray(R.array.contacts_text_background_colors);
        //CONTACT_PHOTO_IMAGE_SIZE=getResources().getDimensionPixelSize(
        //        R.dimen.list_item__contact_imageview_size);
    }

    @Override
    public boolean doFilter(Animal item, CharSequence constraint) {
        return false;
    }

    @Override
    public ArrayList<Animal> getOriginalList() {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
