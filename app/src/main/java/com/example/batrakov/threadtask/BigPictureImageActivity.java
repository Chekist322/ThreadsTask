package com.example.batrakov.threadtask;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Represent requested image in full size.
 */
public class BigPictureImageActivity extends AppCompatActivity {

    private static final String TAG = BigPictureImageActivity.class.getSimpleName();
    private static final int MSG_ADD_BIG_TASK = 1;

    private ImageView mImageView;
    private boolean mServiceBound;

    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.big_picture_layout);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mImageView = findViewById(R.id.big_image);

        Intent startAnotherService = new Intent("com.example.batrakov.imageloaderservice.ACTION");
        startAnotherService.setPackage("com.example.batrakov.imageloaderservice");
        bindService(startAnotherService, mConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName aName, IBinder aBinder) {
            Log.i(TAG, "onServiceConnected: ");
            Messenger taskServiceMessenger = new Messenger(aBinder);
            mServiceBound = true;

            if (getIntent().hasExtra(MainActivity.IMAGE_PATH)) {
                Handler imageChanger = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message aMsg) {
                        if (aMsg.getData() != null) {
                            mImageView.setImageBitmap((Bitmap) aMsg.getData().getParcelable(MainActivity.IMAGE));
                        }
                        return false;
                    }
                });

                Messenger messenger = new Messenger(imageChanger);

                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.IMAGE_PATH, getIntent().getStringExtra(MainActivity.IMAGE_PATH));
                bundle.putParcelable(MainActivity.TARGET_MSG, messenger);

                Message msgToService = Message.obtain();
                msgToService.what = MSG_ADD_BIG_TASK;
                msgToService.setData(bundle);

                try {
                    taskServiceMessenger.send(msgToService);
                } catch (RemoteException aE) {
                    aE.printStackTrace();
                }
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
