package com.example.batrakov.threadtask;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

    /**
     * Flag for messenger sending to service.
     */
    public static final String TARGET_MSG = "target msg";

    /**
     * Flag for image from service.
     */
    public static final String IMAGE = "image";
    private static final int MSG_ADD_THUMBNAIL_TASK = 0;

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LANDSCAPE_COL_SPAN = 3;

    private int mTargetThumbnailWidth;
    private int mTargetScreenDensity;
    private boolean mServiceBound = false;

    private Messenger mTaskServiceMessenger;

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

        mTargetScreenDensity = getResources().getDisplayMetrics().densityDpi;
        mTargetThumbnailWidth = INCH * mTargetScreenDensity;

        try {
            Intent startAnotherService = new Intent();
            startAnotherService.setPackage(getString(R.string.service_package));
            bindService(startAnotherService, mConnection, BIND_AUTO_CREATE);
        } catch (SecurityException aE) {
            aE.printStackTrace();
            Toast.makeText(this, R.string.no_service_permission, Toast.LENGTH_LONG).show();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName aName, IBinder aBinder) {
            mTaskServiceMessenger = new Messenger(aBinder);
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
        private Messenger mMessenger;
        private String mPathToCurrentImage;
        private Handler mHandler;

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

            mHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message aMessage) {
                    if (aMessage.getData() != null) {
                        if (mPathToCurrentImage.equals(aMessage.getData().getString(IMAGE_PATH))) {
                            setThumbnail((Bitmap) aMessage.getData().getParcelable(IMAGE));
                        }
                    }
                    return true;
                }
            });
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

            if (mServiceBound) {
                setMessenger(new Messenger(mHandler));

                Bundle bundle = new Bundle();
                bundle.putString(IMAGE_PATH, mPathToCurrentImage);
                bundle.putParcelable(TARGET_MSG, getMessenger());

                Message msgToService = Message.obtain();
                msgToService.what = MSG_ADD_THUMBNAIL_TASK;
                msgToService.arg1 = mTargetScreenDensity;
                msgToService.arg2 = mTargetThumbnailWidth;
                msgToService.setData(bundle);

                try {
                    mTaskServiceMessenger.send(msgToService);
                } catch (RemoteException aE) {
                    aE.printStackTrace();
                }
            }
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
         * Get holder messenger for sending it to Service.
         *
         * @return current holder messenger.
         */
        Messenger getMessenger() {
            return mMessenger;
        }

        /**
         * Set holder messenger.
         *
         * @param aMessenger target new messenger.
         */
        void setMessenger(Messenger aMessenger) {
            mMessenger = aMessenger;
        }

        /**
         * Set path to new required image.
         *
         * @param aPathToCurrentImage path to new image.
         */
        void setPathToCurrentImage(String aPathToCurrentImage) {
            mPathToCurrentImage = aPathToCurrentImage;
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
            aHolder.setPathToCurrentImage(image.getPath());
            aHolder.bindView(image);

            final Intent intent = new Intent(getBaseContext(), BigPictureImageActivity.class);
            aHolder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View aView) {
                    intent.putExtra(IMAGE_PATH, image.getPath());
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onViewDetachedFromWindow(ListHolder aHolder) {
            super.onViewDetachedFromWindow(aHolder);
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
