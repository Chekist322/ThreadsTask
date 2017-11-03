package com.example.batrakov.threadtask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import java.util.ArrayList;

/**
 * Main application Activity.
 * Contain RecyclerView and allow to open every image in second Activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUESTED_AMOUNT_OF_THREADS = 4;

    /**
     * Flag for string path to image.
     */
    public static final String IMAGE_PATH = "image path";

    private CustomThreadManager mThreadManager;

    @Override
    protected void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.activity_main);

        Handler responseHandler = new Handler();

        ArrayList<LoaderThread> threads = new ArrayList<>();

        for (int i = 0; i < REQUESTED_AMOUNT_OF_THREADS; i++) {
            LoaderThread thread = new LoaderThread("LoaderThread" + i, responseHandler);
            thread.setThumbnailLoadListener(new LoaderThread.ThumbnailLoadListener() {
                @Override
                public void onThumbnailLoaded(ListHolder aHolder, Bitmap aThumbnail) {
                    aHolder.setThumbnail(aThumbnail);
                }
            });
            threads.add(thread);
        }

        mThreadManager = new CustomThreadManager(threads, REQUESTED_AMOUNT_OF_THREADS);

        ArrayList<SingleImage> imageArrayList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdapter adapter = new ListAdapter(imageArrayList);
        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        File sImagesDirectory = new File(Environment.getExternalStorageDirectory() + "/images");
        for (int i = 0; i < sImagesDirectory.listFiles().length; i++) {
            File file = sImagesDirectory.listFiles()[i];
            SingleImage elementWithoutImage = new SingleImage();
            elementWithoutImage.setName(file.getName());
            elementWithoutImage.setPath(file.getPath());
            imageArrayList.add(elementWithoutImage);
        }
        adapter.replaceData(imageArrayList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThreadManager.clearPool();
    }

    /**
     * Holder for RecyclerView. Contain single list element.
     */
    final class ListHolder extends RecyclerView.ViewHolder implements Serializable {

        private ImageView mImage;
        private TextView mDescription;
        private View mContainer;

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
            mImage.setImageDrawable(getResources().getDrawable(R.drawable.img, null));
        }

        /**
         * Set bitmap to list holder ImageView.
         *
         * @param aThumbnail target bitmap.
         */
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
            final SingleImage image = mList.get(aPosition);
            aHolder.bindView(image);

            final Intent intent = new Intent(getBaseContext(), BigPictureImageActivity.class);
            aHolder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View aView) {
                    intent.putExtra(IMAGE_PATH, image.getPath());
                    startActivity(intent);
                }
            });
            mThreadManager.sendRequestMessage(aHolder, image.getPath());
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
