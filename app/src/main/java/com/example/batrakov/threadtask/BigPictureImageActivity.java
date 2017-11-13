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

/**
 * Represent requested image in full size.
 */
public class BigPictureImageActivity extends AppCompatActivity {

    private static final String TAG = BigPictureImageActivity.class.getSimpleName();

    private ImageView mImageView;
    private boolean mServiceBound;

    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.big_picture_layout);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mImageView = findViewById(R.id.big_image);


        Intent startLoaderService = new Intent(getString(R.string.service_action));
        startLoaderService.setPackage(getString(R.string.service_package));
        bindService(startLoaderService, mConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName aName, IBinder aBinder) {
            Log.i(TAG, "onServiceConnected: ");
            IServiceRequest taskServiceRequestInterface = IServiceRequest.Stub.asInterface(aBinder);
            mServiceBound = true;

            final Handler handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message aMessage) {
                    System.out.println("kek");
                    Bundle msgData = aMessage.getData();
                    if (msgData != null) {
                        mImageView.setImageBitmap((Bitmap) msgData.getParcelable(IMAGE));
                    }
                    return false;
                }
            });

            IServiceCallback aidlCallback = new IServiceCallback.Stub() {
                @Override
                public void bitmapLoaded(String aPath, Bitmap aBitmap) throws RemoteException {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(IMAGE, aBitmap);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }

                @Override
                public void listsLoaded(List<String> aPathList, List<String> aNameList) throws RemoteException {
                }
            };

            try {
                taskServiceRequestInterface.addListTask(aidlCallback);
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
            unbindService(mConnection);
        }
        super.onDestroy();
    }
}
