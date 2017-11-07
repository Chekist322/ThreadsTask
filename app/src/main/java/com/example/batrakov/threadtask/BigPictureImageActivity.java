package com.example.batrakov.threadtask;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.batrakov.threadtask.draft.ImageLoaderTask;
import com.example.batrakov.threadtask.draft.TaskManager;

/**
 * Represent requested image in full size.
 */
public class BigPictureImageActivity extends AppCompatActivity {

    private ImageView mImageView;

    private TaskManager mTaskManager;

    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.big_picture_layout);

        mImageView = findViewById(R.id.big_image);

        if (getIntent().hasExtra(MainActivity.IMAGE_PATH)) {
            Handler imageChanger = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message aMsg) {
                    mImageView.setImageBitmap((Bitmap) aMsg.obj);
                    return false;
                }
            });
            String path = getIntent().getStringExtra(MainActivity.IMAGE_PATH);
            mTaskManager = new TaskManager(1);
            Message message = Message.obtain();
            message.setTarget(imageChanger);
            mTaskManager.addTask(new ImageLoaderTask(path, message));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTaskManager.clear();
    }
}
