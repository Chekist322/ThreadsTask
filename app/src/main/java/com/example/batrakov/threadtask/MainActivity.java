package com.example.batrakov.threadtask;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.batrakov.threadtask.loadImageTask.ThumbnailTask;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Main application Activity.
 * Contain RecyclerView and allow to open every image in second Activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final int INCH = 1;

    /**
     * Flag for string path to image.
     */
    public static final String IMAGE_PATH = "image path";
    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LANDSCAPE_COL_SPAN = 3;

    private int mTargetSize;
    private int mTargetDensity;
    private boolean mServiceBound = false;

    private ImageTaskService mTaskService;

    @Override
    public void onRequestPermissionsResult(int aRequestCode,
                                           @NonNull String[] aPermissions, @NonNull int[] aGrantResults) {
        super.onRequestPermissionsResult(aRequestCode, aPermissions, aGrantResults);
    }

    @Override
    protected void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

        Intent startImageTaskServiceIntent = new Intent(this, ImageTaskService.class);
        bindService(startImageTaskServiceIntent, mConnection, BIND_AUTO_CREATE);

        mTargetDensity = getResources().getDisplayMetrics().densityDpi;
        mTargetSize = INCH * mTargetDensity;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName aName, IBinder aBinder) {
            ImageTaskService.ImageTaskBinder binder = (ImageTaskService.ImageTaskBinder) aBinder;
            mTaskService = binder.getService();
            mServiceBound = true;

            ArrayList<SingleImage> imageArrayList = new ArrayList<>();

            RecyclerView recyclerView = findViewById(R.id.list);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), LANDSCAPE_COL_SPAN));
            }
            ListAdapter adapter = new ListAdapter(imageArrayList);
            recyclerView.setAdapter(adapter);
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

            File sImagesDirectory = new File(Environment.getExternalStorageDirectory() + "/images");
            if (sImagesDirectory.listFiles() != null) {
                File[] listFiles = sImagesDirectory.listFiles();
                for (File file : listFiles) {
                    SingleImage elementWithoutImage = new SingleImage();
                    elementWithoutImage.setName(file.getName());
                    elementWithoutImage.setPath(file.getPath());
                    imageArrayList.add(elementWithoutImage);
                }
                adapter.replaceData(imageArrayList);
            } else {
                Toast.makeText(getBaseContext(), "There is no image directory...", Toast.LENGTH_LONG).show();
            }
            Log.i(TAG, "onServiceConnected: ");

        }

        @Override
        public void onServiceDisconnected(ComponentName aName) {
            Log.i(TAG, "onServiceDisconnected: ");
            mServiceBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceBound) {
            unbindService(mConnection);
        }
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
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mDescription = aItemView.findViewById(R.id.image_description);
            }
            mContainer = aItemView.findViewById(R.id.image_container);
        }

        /**
         * View filling.
         *
         * @param aImage image from list
         */
        void bindView(final SingleImage aImage) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mDescription.setText(aImage.getName());
            }
            mImage.setImageDrawable(getDrawable(R.drawable.img));
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
            if (mServiceBound) {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inTargetDensity = mTargetDensity;
                options.inScaled = true;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(image.getPath(), options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = BitmapUtils.calculateInSampleSize(options, mTargetSize, mTargetSize);

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
                ThumbnailTask thumbnailTask = new ThumbnailTask(image.getPath(), options, callback);
                aHolder.setTask(thumbnailTask);
                mTaskService.addTask(thumbnailTask);
            }
        }

        @Override
        public void onViewDetachedFromWindow(ListHolder aHolder) {
            super.onViewDetachedFromWindow(aHolder);
            aHolder.getTask().cancel();
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
