package com.example.batrakov.threadtask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static ArrayList<SingleImage> sImageArrayList;

    private static ListAdapter sAdapter;

    private static Messenger sMessenger = new Messenger(new HandlerForImageLoader());

    public static final String OPEN_IMAGE = "open image";

    public static final int IMAGE_LOADED = 0;

    private ExecutorService mExecutorService;
    private ImageThreadsExecutor sExecutor = new ImageThreadsExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExecutorService = Executors.newFixedThreadPool(4);
        setContentView(R.layout.activity_main);

        sImageArrayList = new ArrayList<>(50);

        RecyclerView recyclerView = findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sAdapter = new ListAdapter(sImageArrayList);
        recyclerView.setAdapter(sAdapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);


        File sImagesDirectory = new File(Environment.getExternalStorageDirectory() + "/images");
        for (int i = 0; i < sImagesDirectory.listFiles().length; i++) {
            File file = sImagesDirectory.listFiles()[i];
            SingleImage elementWithoutImage = new SingleImage();
            elementWithoutImage.setName(file.getName());
            elementWithoutImage.setPath(file.getPath());
            sImageArrayList.add(elementWithoutImage);
        }
        sAdapter.replaceData(sImageArrayList);
    }

    private static class HandlerForImageLoader extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IMAGE_LOADED:
                    sAdapter.changeItem(msg.arg1);
                    break;
            }
        }
    }

    private static class ImageThreadsExecutor implements Executor {

        @Override
        public void execute(@NonNull Runnable aRunnable) {
            new Thread(aRunnable).start();
        }
    }

    private class ImageLoader implements Runnable {

        int mImageId;


        ImageLoader(int aImageId) {
            mImageId = aImageId;
        }

        @Override
        public void run() {
            Log.i(TAG, "run Start: " + String.valueOf(mImageId));
            sImageArrayList.get(mImageId).buildThumbnail();
            Message msg = Message.obtain();
            msg.arg1 = mImageId;
            try {
                sMessenger.send(msg);
            } catch (RemoteException aE) {
                aE.printStackTrace();
            }
            Log.i(TAG, "run Finish: " + String.valueOf(mImageId));
        }
    }


    /**
     * Holder for RecyclerView. Contain single list element.
     */
    private final class ListHolder extends RecyclerView.ViewHolder {

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
            mImage.setImageBitmap(aImage.getThumbnail());
            mDescription.setText(aImage.getName());
            Log.i(TAG, "bindView: " + String.valueOf(getAdapterPosition()) + String.valueOf(aImage.getThumbnail()));
            if (aImage.getThumbnail() == null) {
                mExecutorService.execute(new ImageLoader(getAdapterPosition()));
            }
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

        void changeItem(int aId) {
            notifyItemChanged(aId);
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

            final Intent intent = new Intent(getBaseContext(), BigPictureImageView.class);
            aHolder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent.putExtra(OPEN_IMAGE, aHolder.getAdapterPosition());
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
