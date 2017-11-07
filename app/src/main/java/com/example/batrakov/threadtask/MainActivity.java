package com.example.batrakov.threadtask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.batrakov.threadtask.draft.ThumbnailTask;
import com.example.batrakov.threadtask.draft.TaskManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Main application Activity.
 * Contain RecyclerView and allow to open every image in second Activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final int TARGET_WIDTH = 384;

    private static final int TARGET_DENSITY = 441;

    private static final int AMOUNT_OF_THREADS = 4;


    /**
     * Flag for string path to image.
     */
    public static final String IMAGE_PATH = "image path";

    private TaskManager mTaskManager;

    @Override
    protected void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.activity_main);

        mTaskManager = new TaskManager(AMOUNT_OF_THREADS);

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
        mTaskManager.clear();
    }

    /**
     * Holder for RecyclerView. Contain single list element.
     */
    final class ListHolder extends RecyclerView.ViewHolder implements Serializable {

        private ImageView mImage;
        private TextView mDescription;
        private View mContainer;
        private ThumbnailTask mTask;

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

        /**
         * Get task for current holder.
         *
         * @return current holder task.
         */
        ThumbnailTask getTask() {
            return mTask;
        }

        /**
         * Set task for current holder.
         *
         * @param aTask target task.
         */
        void setTask(ThumbnailTask aTask) {
            mTask = aTask;
        }
    }

    /**
     * Adapter for recycler view. Allow to fill and update list.
     */
    private class ListAdapter extends RecyclerView.Adapter<ListHolder> {
        @Override
        public void onViewDetachedFromWindow(ListHolder aHolder) {
            super.onViewDetachedFromWindow(aHolder);
            aHolder.getTask().cancel();
        }

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

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inScaled = true;
            bitmapOptions.inSampleSize = getMultiplier(image.getPath());
            bitmapOptions.inTargetDensity = TARGET_DENSITY;

            Handler imageChanger = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message aMessage) {
                    if (aMessage.obj != null) {
                        aHolder.setThumbnail((Bitmap) aMessage.obj);
                    }
                    return true;
                }
            });

            Message callback = Message.obtain();
            callback.setTarget(imageChanger);

            aHolder.setTask(new ThumbnailTask(image.getPath(), bitmapOptions, callback));
            mTaskManager.addTask(aHolder.getTask());
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

    /**
     * Get multiplier for image scaling.
     *
     * @param aPath path to target image.
     *
     * @return scale multiplier.
     */
    private int getMultiplier(String aPath) {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(aPath, bitmapOptions);
        return bitmapOptions.outHeight / TARGET_WIDTH;
    }

}
