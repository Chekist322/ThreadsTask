package com.example.batrakov.threadtask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String IMAGE_PATH = "image path";

    private LoaderThread mLoaderThread;
    ArrayList<SingleImage> mImageArrayList;
    ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler responseHandler = new Handler();

        mLoaderThread = new LoaderThread("LoaderThread", responseHandler);
        mLoaderThread.setThumbnailDownloadListener(new LoaderThread.ThumbnailDownloadListener() {
            @Override
            public void onThumbnailDownloaded(ListHolder target, Bitmap thumbnail) {
                target.setThumbnail(thumbnail);
            }
        });
        mLoaderThread.start();

        mImageArrayList = new ArrayList<>(50);

        RecyclerView recyclerView = findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ListAdapter(mImageArrayList);
        recyclerView.setAdapter(mAdapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);


        File sImagesDirectory = new File(Environment.getExternalStorageDirectory() + "/images");
        for (int i = 0; i < sImagesDirectory.listFiles().length; i++) {
            File file = sImagesDirectory.listFiles()[i];
            SingleImage elementWithoutImage = new SingleImage();
            elementWithoutImage.setName(file.getName());
            elementWithoutImage.setPath(file.getPath());
            mImageArrayList.add(elementWithoutImage);
        }
        mAdapter.replaceData(mImageArrayList);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoaderThread.clearQueue();
    }

    /**
     * Holder for RecyclerView. Contain single list element.
     */
    public final class ListHolder extends RecyclerView.ViewHolder implements Serializable{

        ImageView mImage;
        TextView mDescription;
        View mContainer;

        /**
         * Constructor.
         *
         * @param aItemView item view
         */
        private ListHolder(View aItemView) {
            super(aItemView);
            mImage = aItemView.findViewById(R.id.image);
            mDescription = aItemView.findViewById(R.id.image_description);
            mContainer = aItemView.findViewById(R.id.image_container);
        }

        /**
         * View filling.
         *
         * @param aImage image from list
         */
        void bindView(final SingleImage aImage) {
            mDescription.setText(aImage.getName());
            mImage.setImageDrawable(getDrawable(R.drawable.img));
        }

        void setThumbnail(Bitmap aThumbnail) {
            mImage.setImageBitmap(aThumbnail);
        }
    }

    /**
     * Adapter for recycler view. Allow to fill and update list.
     */
    private class ListAdapter extends RecyclerView.Adapter<ListHolder> {

        private ArrayList<SingleImage> mList;

        /**
         * Constructor.
         *
         * @param aList target list for fill.
         */
        ListAdapter(ArrayList<SingleImage> aList) {
            setHasStableIds(true);
            mList = aList;
        }

        /**
         * List updating.
         *
         * @param aList new target list.
         */
        void replaceData(ArrayList<SingleImage> aList) {
            mList = aList;
            notifyDataSetChanged();
        }

        @Override
        public ListHolder onCreateViewHolder(ViewGroup aParent, int aViewType) {
            View rowView = LayoutInflater.from(aParent.getContext()).inflate(R.layout.list_item, aParent, false);
            return new ListHolder(rowView);
        }

        @Override
        public void onBindViewHolder(final ListHolder aHolder, final int aPosition) {

            SingleImage image = mList.get(aPosition);
            aHolder.bindView(image);

            mLoaderThread.queueThumbnail(aHolder, image.getPath());
            final Intent intent = new Intent(getBaseContext(), BigPictureImageView.class);
            aHolder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent.putExtra(IMAGE_PATH, aHolder.getAdapterPosition());
                    startActivity(intent);
                }
            });
        }


        @Override
        public long getItemId(int aIndex) {
            return aIndex;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

}
