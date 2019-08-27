/**
 * Copyright (C) 2014 Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 * <p>
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 * <p>
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */

package com.samsung.android.simplehealth;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    public static final String APP_TAG = "SimpleHealth";

    @BindView(R.id.stepCount)
    TextView mStepCountTv;
    @BindView(R.id.heartRate)
    TextView mHeartRateCountTv;

    private HealthDataStore mStore;
    private StepCountReporter mReporter;
    private HeartRateReporter mHeartRateReporter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(this, mConnectionListener);
        // Request the connection to the health data store
        mStore.connectService();
    }

    @Override
    public void onDestroy() {
        mStore.disconnectService();
        super.onDestroy();
    }

    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(APP_TAG, "Health data service is connected.");
            mReporter = new StepCountReporter(mStore);
            mHeartRateReporter = new HeartRateReporter(mStore);
            if (isPermissionAcquired()) {
                mReporter.start(mStepCountObserver);
                mHeartRateReporter.start(mHeartReportObserver);
            } else {
                requestPermission();
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.d(APP_TAG, "Health data service is not available.");
            showConnectionFailureDialog(error);
        }

        @Override
        public void onDisconnected() {
            Log.d(APP_TAG, "Health data service is disconnected.");
            if (!isFinishing()) {
                mStore.connectService();
            }
        }
    };

    private void showPermissionAlarmDialog() {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle(R.string.notice)
                .setMessage(R.string.msg_perm_acquired)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showConnectionFailureDialog(final HealthConnectionErrorResult error) {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (error.hasResolution()) {
            switch (error.getErrorCode()) {
                case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                    alert.setMessage(R.string.msg_req_install);
                    break;
                case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                    alert.setMessage(R.string.msg_req_upgrade);
                    break;
                case HealthConnectionErrorResult.PLATFORM_DISABLED:
                    alert.setMessage(R.string.msg_req_enable);
                    break;
                case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                    alert.setMessage(R.string.msg_req_agree);
                    break;
                default:
                    alert.setMessage(R.string.msg_req_available);
                    break;
            }
        } else {
            alert.setMessage(R.string.msg_conn_not_available);
        }

        alert.setPositiveButton(R.string.ok, (dialog, id) -> {
            if (error.hasResolution()) {
                error.resolve(MainActivity.this);
            }
        });

        if (error.hasResolution()) {
            alert.setNegativeButton(R.string.cancel, null);
        }

        alert.show();
    }

    private boolean isPermissionAcquired() {
        Set<PermissionKey> mKeys = new HashSet<PermissionKey>();
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        mKeys.add(new PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ));
        mKeys.add(new PermissionKey(HealthConstants.HeartRate.HEALTH_DATA_TYPE, PermissionType.READ));
        try {
            // Check whether the permissions that this application needs are acquired
            Map<PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeys);
            return resultMap.get(mKeys);
        } catch (Exception e) {
            Log.e(APP_TAG, "Permission request fails.", e);
        }
        return false;
    }

    private void requestPermission() {
        Set<PermissionKey> mKeys = new HashSet<PermissionKey>();
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        mKeys.add(new PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ));
        mKeys.add(new PermissionKey(HealthConstants.HeartRate.HEALTH_DATA_TYPE, PermissionType.READ));
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(mKeys, MainActivity.this)
                    .setResultListener(result -> {
                        Log.d(APP_TAG, "Permission callback is received.");
                        Map<PermissionKey, Boolean> resultMap = result.getResultMap();

                        if (resultMap.containsValue(Boolean.FALSE)) {
                            updateStepCountView("");
                            showPermissionAlarmDialog();
                        } else {
                            // Get the current step count and display it
                            mReporter.start(mStepCountObserver);
                            mHeartRateReporter.start(mHeartReportObserver);
                        }
                    });
        } catch (Exception e) {
            Log.e(APP_TAG, "Permission setting fails.", e);
        }
    }

    private StepCountReporter.StepCountObserver mStepCountObserver = count -> {
        Log.d(APP_TAG, "Step reported : " + count);
        updateStepCountView(String.valueOf(count));
    };

    private HeartRateReporter.HeartRateObserver mHeartReportObserver = count -> {
        Log.d(APP_TAG, "HeartRate reported : " + count);
        updateHeartRateView(String.valueOf(count));
    };


    private void updateStepCountView(final String count) {
        runOnUiThread(() -> mStepCountTv.setText(count));
    }

    private void updateHeartRateView(final String count) {
        runOnUiThread(() -> mHeartRateCountTv.setText(count));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        if (item.getItemId() == R.id.connect) {
            requestPermission();
        }

        return true;
    }


}
