/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.camera.ICamera;
import com.stfalcon.hromadskyipatrol.camera.fragment.BaseCameraFragment;
import com.stfalcon.hromadskyipatrol.camera.fragment.Camera2VideoFragment;
import com.stfalcon.hromadskyipatrol.camera.fragment.CameraVideoFragment;
import com.stfalcon.hromadskyipatrol.location.LocationActivity;
import com.stfalcon.hromadskyipatrol.models.ViolationItem;
import com.stfalcon.hromadskyipatrol.services.VideoProcessingService;
import com.stfalcon.hromadskyipatrol.utils.AnimationUtils;

import java.io.File;

public class VideoCaptureActivity extends LocationActivity implements ICamera, View.OnClickListener {

    public static final String MOVIES_TO_SAVE = "moviesUrls";
    private BaseCameraFragment cameraFragment;
    private TextView message;
    private TextView time;
    private ImageButton mainMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (null == savedInstanceState) {
            initCameraFragmentForAPIVersion(Build.VERSION.SDK_INT);
        }
    }

    private void initViews() {
        message = (TextView) findViewById(R.id.tv_message);
        time = (TextView) findViewById(R.id.time);
        mainMenu = (ImageButton) findViewById(R.id.bt_main_screen);
        mainMenu.setOnClickListener(this);
    }


    public void initCameraFragmentForAPIVersion(int ver) {
        cameraFragment = getSupportCamera(ver);
        cameraFragment.addCameraCallback(this);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, cameraFragment)
                .commit();
    }

    private BaseCameraFragment getSupportCamera(int ver) {
        return ver < Build.VERSION_CODES.LOLLIPOP ?
                CameraVideoFragment.newInstance() :
                Camera2VideoFragment.newInstance();
        //return CameraVideoFragment.newInstance();
    }

    @Override
    public void onCameraPrepared() {
        cameraFragment.startRecordSegment();
    }

    @Override
    public void onStartRecord() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                time.setText("REC");
            }
        });
    }

    @Override
    public void onStopRecord() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideRecordMessage();
                time.setText("--:--");
            }
        });
    }

    @Override
    public void onVideoPrepared(ViolationItem violationItem) {
        Location location = getLastUserLocation();
        if (location != null) {
            violationItem.setLat(location.getLatitude());
            violationItem.setLon(location.getLongitude());
        }

        Intent intent = new Intent(VideoCaptureActivity.this, VideoProcessingService.class);
        intent.putExtra(MOVIES_TO_SAVE, violationItem);
        startService(intent);
    }

    @Override
    public void onViolationDetected() {
        showRecordMessage();
    }

    @Override
    public void onTime(final int sec) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                time.setText(String.format(getResources()
                        .getString(R.string.timer_format), String.valueOf(10 + sec)));
            }
        });
    }

    private void startMenuActivity() {
        try {
            if (null != cameraFragment.violationFileURI) {
                new File(cameraFragment.violationFileURI).delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showRecordMessage() {
        message.startAnimation(AnimationUtils.getBlinkAnimation());
        message.setVisibility(View.VISIBLE);
    }

    public void hideRecordMessage() {
        message.clearAnimation();
        message.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_main_screen:
                startMenuActivity();
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startMenuActivity();
    }

    @Override
    public void onStop() {
        cameraFragment.onStop();
        startMenuActivity();
        setResult(RESULT_CANCELED);
        finish();
        super.onStop();
    }
}

