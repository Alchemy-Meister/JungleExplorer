package com.creations.meister.jungleexplorer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.domain.Domain;
import com.creations.meister.jungleexplorer.image_utils.ImageHelper;
import com.creations.meister.jungleexplorer.utils.CircularView;
import com.creations.meister.jungleexplorer.utils.ContactImageUtil;
import com.creations.meister.jungleexplorer.utils.ImageCache;
import com.creations.meister.jungleexplorer.utils.async_task_thread_pool.AsyncTaskEx;
import com.creations.meister.jungleexplorer.utils.async_task_thread_pool.AsyncTaskThreadPool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lb.library.SearchablePinnedHeaderListViewAdapter;
import lb.library.StringArrayAlphabetIndexer;

/**
 * Created by meister on 4/1/16.
 */
public class DomainAdapter extends SearchablePinnedHeaderListViewAdapter<Domain> {

    private LayoutInflater mInflater;
    private Context context;
    private ArrayList<Domain> mDomain;
    private SparseBooleanArray mSelectedItemsIds;
    private final int CONTACT_PHOTO_IMAGE_SIZE;
    private final int[] PHOTO_TEXT_BACKGROUND_COLORS;
    private final AsyncTaskThreadPool mAsyncTaskThreadPool=new AsyncTaskThreadPool(1,2,10);

    public DomainAdapter(Context context, final ArrayList<Domain> domain) {
        this.context = context;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.setData(domain);
        PHOTO_TEXT_BACKGROUND_COLORS = context.getResources().getIntArray(R.array.contacts_text_background_colors);
        CONTACT_PHOTO_IMAGE_SIZE = context.getResources().getDimensionPixelSize(
                R.dimen.list_item__contact_imageview_size);
        mInflater = LayoutInflater.from(context);
    }

    public void setData(final ArrayList<Domain> domainInstances) {
        this.mDomain = domainInstances;
        final String[] generatedContactNames = this.generateDomainNames(domainInstances);
        this.setSectionIndexer(new StringArrayAlphabetIndexer(generatedContactNames, true));
    }

    private String[] generateDomainNames(final List<Domain> domainInstances) {
        final ArrayList<String> animalNames = new ArrayList<String>();
        if (domainInstances != null)
            for (final Domain domainInstance : domainInstances)
                animalNames.add(domainInstance.getName());
        return animalNames.toArray(new String[animalNames.size()]);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final View rootView;
        if (convertView == null) {
            holder = new ViewHolder();
            rootView = mInflater.inflate(R.layout.listview_item, parent, false);
            holder.animalProfileCircularView = (CircularView) rootView
                    .findViewById(R.id.listview_item__friendPhotoImageView);
            holder.animalProfileCircularView.getTextView().setTextColor(0xFFffffff);
            holder.friendName = (TextView) rootView
                    .findViewById(R.id.listview_item__friendNameTextView);
            holder.headerView = (TextView) rootView.findViewById(R.id.header_text);
            rootView.setTag(holder);
        } else {
            rootView = convertView;
            holder = (ViewHolder) rootView.getTag();
        }
        final Domain domain = getItem(position);
        final String displayName = domain.getName();
        final int domainID = domain.getId();
        holder.friendName.setText(displayName);
        holder.id = domainID;
        boolean hasPhoto = !TextUtils.isEmpty(domain.getPhotoId());
        if (holder.updateTask != null && !holder.updateTask.isCancelled())
            holder.updateTask.cancel(true);
        final Bitmap cachedBitmap = hasPhoto ? ImageCache.INSTANCE.getBitmapFromMemCache(domain.getPhotoId()) : null;
        if (cachedBitmap != null)
            holder.animalProfileCircularView.setImageBitmap(cachedBitmap);
        else {
            final int backgroundColorToUse = PHOTO_TEXT_BACKGROUND_COLORS[position
                    % PHOTO_TEXT_BACKGROUND_COLORS.length];
            if (TextUtils.isEmpty(displayName))
                holder.animalProfileCircularView.setImageResource(R.drawable.ic_animal_white_120dp,
                        backgroundColorToUse);
            else {
                final String characterToShow = TextUtils.isEmpty(displayName) ? "" : displayName.substring(0, 1).toUpperCase(Locale.getDefault());
                holder.animalProfileCircularView.setTextAndBackgroundColor(characterToShow, backgroundColorToUse);
            }
            if (hasPhoto) {
                holder.updateTask = new AsyncTaskEx<Void, Void, Bitmap>() {

                    @Override
                    public Bitmap doInBackground(final Void... params) {
                        if (isCancelled())
                            return null;
                        // TODO get real image from DB.
                        final int THUMBNAIL_SIZE = 64;

                        Bitmap imageBitmap = null;

                        try {
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            bmOptions.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(new FileInputStream(domain.getPhotoId()),
                                    null, bmOptions);
                            int photoH = bmOptions.outHeight;

                            // Determine how much to scale down the image
                            int scaleFactor = photoH/THUMBNAIL_SIZE;

                            // Decode the image file into a Bitmap sized to fill the View
                            bmOptions.inJustDecodeBounds = false;

                            bmOptions.inSampleSize = scaleFactor;
                            bmOptions.inPurgeable = true;

                            imageBitmap = BitmapFactory.decodeStream(
                                    new FileInputStream(domain.getPhotoId()), null, bmOptions);
                            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE,
                                    THUMBNAIL_SIZE, false);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }


                        final Bitmap b = imageBitmap;
                        if (b != null)
                            return ThumbnailUtils.extractThumbnail(b, CONTACT_PHOTO_IMAGE_SIZE,
                                    CONTACT_PHOTO_IMAGE_SIZE);
                        return null;
                    }

                    @Override
                    public void onPostExecute(final Bitmap result) {
                        super.onPostExecute(result);
                        if (result == null)
                            return;
                        ImageCache.INSTANCE.addBitmapToCache(domain.getPhotoId(), result);
                        holder.animalProfileCircularView.setImageBitmap(result);
                    }
                };
                mAsyncTaskThreadPool.executeAsyncTask(holder.updateTask);
            }
        }
        bindSectionHeader(holder.headerView, null, position);
        return rootView;
    }

    @Override
    public boolean doFilter(final Domain item, final CharSequence constraint)
    {
        if(TextUtils.isEmpty(constraint))
            return true;
        final String displayName=item.getName();
        return !TextUtils.isEmpty(displayName)&&displayName.toLowerCase(Locale.getDefault())
                .contains(constraint.toString().toLowerCase(Locale.getDefault()));
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    @Override
    public ArrayList<Domain> getOriginalList()
    {
        return mDomain;
    }

    // /////////////////////////////////////////////////////////////////////////////////////
    // ViewHolder //
    // /////////////
    public static class ViewHolder {
        public CircularView animalProfileCircularView;
        TextView friendName, headerView;
        public int id;
        public AsyncTaskEx<Void, Void, Bitmap> updateTask;
    }
}
