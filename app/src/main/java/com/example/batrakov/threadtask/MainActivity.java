package com.example.batrakov.threadtask;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application Activity.
 * Contain RecyclerView and allow to open every image in second Activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final int INCH = 1;

    /**
     * Flag for string path to image.
     */
    public static final String IMAGE_NAME = "image path";

    /**
     * Flag for image from service.
     */
    public static final String IMAGE = "image";
    private static final String FILES_NAME_LIST = "file name list";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LANDSCAPE_COL_SPAN = 3;

    private int mTargetThumbnailWidth;
    private int mTargetScreenDensity;
    private boolean mServiceBound = false;
    private ArrayList<String> mFilesNameList = new ArrayList<>();
    private ListAdapter mAdapter;

    private IServiceRequest mTaskServiceRequestInterface;

    @Override
    protected void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.activity_main);

        mTargetScreenDensity = getResources().getDisplayMetrics().densityDpi;
        mTargetThumbnailWidth = INCH * mTargetScreenDensity;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), LANDSCAPE_COL_SPAN));
        }

        mAdapter = new ListAdapter(mFilesNameList);
        recyclerView.setAdapter(mAdapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        connectToService();
    }

    /**
     * Connect to image loader service.
     */
    private void connectToService() {
        try {
            Intent startLoaderService = new Intent(getString(R.string.service_action));
            startLoaderService.setPackage(getString(R.string.service_package));
            bindService(startLoaderService, mConnection, BIND_AUTO_CREATE);
        } catch (SecurityException aE) {
            aE.printStackTrace();
            Toast.makeText(this, R.string.no_service_permission, Toast.LENGTH_LONG).show();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName aName, IBinder aBinder) {
            mTaskServiceRequestInterface = IServiceRequest.Stub.asInterface(aBinder);
            mServiceBound = true;

            final Handler setFileListHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message aMsg) {
                    Bundle msgData = aMsg.getData();
                    mFilesNameList = msgData.getStringArrayList(FILES_NAME_LIST);
                    if (mFilesNameList != null) {
                        mAdapter.replaceData(mFilesNameList);
                    } else {
                        Toast.makeText(getBaseContext(), R.string.no_images,
                                Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });

            IServiceCallback aidlCallback = new IServiceCallback.Stub() {
                @Override
                public void bitmapLoaded(String aPath, Bitmap aBitmap) throws RemoteException {
                }

                @Override
                public void listsLoaded(List<String> aNameList) throws RemoteException {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(FILES_NAME_LIST, (ArrayList<String>) aNameList);
                    message.setData(bundle);
                    setFileListHandler.sendMessage(message);
                }
            };

            try {
                mTaskServiceRequestInterface.addListTask(aidlCallback);
            } catch (RemoteException aE) {
                aE.printStackTrace();
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
        if (mServiceBound) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    /**
     * Holder for RecyclerView. Contain single list element.
     */
    final class ListHolder extends RecyclerView.ViewHolder implements Serializable {

        private ImageView mImage;
        private TextView mDescription;
        private View mContainer;
        private String mCurrentImageName;
        private Handler mHandler;
        private IServiceCallback.Stub mAidlCallback;

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
                    Bundle msgData = aMessage.getData();
                    if (msgData != null) {
                        if (mCurrentImageName.equals(msgData.getString(IMAGE_NAME))) {
                            setThumbnail((Bitmap) msgData.getParcelable(IMAGE));
                        }
                    }
                    return false;
                }
            });

            mAidlCallback = new IServiceCallback.Stub() {
                @Override
                public void bitmapLoaded(String aName, Bitmap aBitmap) throws RemoteException {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(IMAGE, aBitmap);
                    bundle.putString(IMAGE_NAME, aName);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }

                @Override
                public void listsLoaded(List<String> aNameList) throws RemoteException {
                }
            };
        }

        /**
         * View filling.
         *
         * @param aName image name from list
         */
        void bindView(String aName) {
            if (mDescription != null
                    && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mDescription.setText(aName);
            }
            mImage.setImageDrawable(getDrawable(R.drawable.img));

            if (mServiceBound) {
                try {
                    mTaskServiceRequestInterface.addThumbnailTask(mCurrentImageName,
                            mAidlCallback, mTargetScreenDensity, mTargetThumbnailWidth);
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
         * Set path to new required image.
         *
         * @param aCurrentImageName path to new image.
         */
        void setCurrentImageName(String aCurrentImageName) {
            mCurrentImageName = aCurrentImageName;
        }
    }

    /**
     * Adapter for recycler view. Allow to fill and update list.
     */
    private class ListAdapter extends RecyclerView.Adapter<ListHolder> {

        private ArrayList<String> mNameList;

        /**
         * Constructor.
         *
         * @param aNameList target name list.
         */
        ListAdapter(ArrayList<String> aNameList) {
            mNameList = aNameList;
        }

        /**
         * List updating.
         *
         * @param aNameList target name list.
         */
        void replaceData(ArrayList<String> aNameList) {
            mNameList = aNameList;
            notifyDataSetChanged();
        }

        @Override
        public ListHolder onCreateViewHolder(ViewGroup aParent, int aViewType) {
            View rowView = LayoutInflater.from(aParent.getContext()).inflate(R.layout.list_item, aParent, false);
            return new ListHolder(rowView);
        }

        @Override
        public void onBindViewHolder(final ListHolder aHolder, final int aPosition) {
            final String name = mNameList.get(aPosition);
            aHolder.setCurrentImageName(name);
            aHolder.bindView(name);

            final Intent intent = new Intent(getBaseContext(), BigPictureImageActivity.class);
            aHolder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View aView) {
                    intent.putExtra(IMAGE_NAME, aHolder.mCurrentImageName);
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
            return mNameList.size();
        }
    }
}
