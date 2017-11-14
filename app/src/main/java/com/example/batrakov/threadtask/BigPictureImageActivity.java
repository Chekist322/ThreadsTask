package com.example.batrakov.threadtask;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.List;

import static com.example.batrakov.threadtask.MainActivity.IMAGE;
import static com.example.batrakov.threadtask.MainActivity.IMAGE_NAME;

/**
 * Represent requested image in full size.
 */
public class BigPictureImageActivity extends AppCompatActivity {

    private static final String TAG = BigPictureImageActivity.class.getSimpleName();

    private ImageView mBigImageView;
    private boolean mServiceBound;

    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.big_picture_layout);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mBigImageView = (ImageView) findViewById(R.id.big_image);


        Intent startLoaderService = new Intent(getString(R.string.service_action));
        startLoaderService.setPackage(getString(R.string.service_package));
        bindService(startLoaderService, mLoaderServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mLoaderServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName aName, IBinder aBinder) {
            Log.i(TAG, "onServiceConnected: ");
            IServiceRequest taskServiceRequestInterface = IServiceRequest.Stub.asInterface(aBinder);
            mServiceBound = true;

            final Handler serviceCallbackHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message aMessage) {
                    Log.i(TAG, "handleMessage: ");
                    Bundle msgData = aMessage.getData();
                    if (msgData != null) {
                        mBigImageView.setImageBitmap((Bitmap) msgData.getParcelable(IMAGE));
                    }
                    return false;
                }
            });

            IServiceCallback aidlServiceCallback = new IServiceCallback.Stub() {
                @Override
                public void bitmapLoaded(String aName, Bitmap aBitmap) throws RemoteException {
                    Log.i(TAG, "bitmapLoaded: ");
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(IMAGE, aBitmap);
                    message.setData(bundle);
                    serviceCallbackHandler.sendMessage(message);
                }

                @Override
                public void listsLoaded(List<String> aNameList) throws RemoteException {
                }
            };

            try {
                taskServiceRequestInterface.addBigTask(getIntent().getStringExtra(IMAGE_NAME), aidlServiceCallback);
            } catch (RemoteException aE) {
                aE.printStackTrace();
            }
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
            unbindService(mLoaderServiceConnection);
        }
        super.onDestroy();
    }
}
