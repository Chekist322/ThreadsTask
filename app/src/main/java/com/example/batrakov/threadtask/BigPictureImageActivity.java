package com.example.batrakov.threadtask;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.example.batrakov.threadtask.loadImageTask.ImageLoaderTask;

/**
 * Represent requested image in full size.
 */
public class BigPictureImageActivity extends AppCompatActivity {

    private static final String TAG = BigPictureImageActivity.class.getSimpleName();
    private ImageView mImageView;
    private boolean mServiceBound;
    private ImageLoaderTask mLoaderTask;

    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.big_picture_layout);

        mImageView = findViewById(R.id.big_image);

        Intent startImageTaskServiceIntent = new Intent(this, ImageTaskService.class);
        bindService(startImageTaskServiceIntent, mConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName aName, IBinder aBinder) {
            Log.i(TAG, "onServiceConnected: ");
            ImageTaskService.ImageTaskBinder binder = (ImageTaskService.ImageTaskBinder) aBinder;
            ImageTaskService taskService = binder.getService();
            mServiceBound = true;

            if (getIntent().hasExtra(MainActivity.IMAGE_PATH)) {
                Handler imageChanger = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message aMsg) {
                        mImageView.setImageBitmap((Bitmap) aMsg.obj);
                        return false;
                    }
                });
                String path = getIntent().getStringExtra(MainActivity.IMAGE_PATH);
                Message message = Message.obtain();
                message.setTarget(imageChanger);
                mLoaderTask = new ImageLoaderTask(path, message);
                taskService.addTask(new ImageLoaderTask(path, message));
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
        if (mLoaderTask != null) {
            mLoaderTask.cancel();
        }
        if (mServiceBound) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }
}
