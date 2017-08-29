package com.mlt.kingsongwear;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BTScanActivity extends Activity {

    private static final String TAG = "BTScanActivity";

    ListView mScanResultView;
    ScanResultListAdapter mResultListAdapter;
    private boolean scanning = false;

    AdapterView.OnItemClickListener wheelSelectListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String deviceAddress = mResultListAdapter.getItem(position).getDevice().getAddress();
            Log.d(TAG, "WHEEL SELECTED: " + deviceAddress);
            startSpeedometerActivity(deviceAddress);
            finish();
        }
    };

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startSpeedometerActivity(String deviceAddress) {
        Intent intent = new Intent(BTScanActivity.this, SpeedometerActivity.class);
        intent.putExtra("deviceAddress", deviceAddress);
        startActivity(intent);
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult: " + result.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: " + results.toString());
            mResultListAdapter.clear();
            mResultListAdapter.addAll(results);
            mResultListAdapter.notifyDataSetInvalidated();
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed: " + errorCode);
            showToast("Scan failed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btscan);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        mResultListAdapter = new ScanResultListAdapter(this,
                R.layout.scanresultlayout,
                new ArrayList<ScanResult>());

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mScanResultView = (ListView) stub.findViewById(R.id.scanresults);
                mScanResultView.setAdapter(mResultListAdapter);
                mScanResultView.setOnItemClickListener(wheelSelectListener);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if(requestCode==1) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onResume, already having permission. starting scan");
            startScan();
        } else {
            Log.d(TAG, "onResume, requesting permissions");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause, stopping scan if running");
        stopScan();
    }

    private void startScan() {
        if(!scanning) {
            Log.d(TAG, "Starting scan");

            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setUseHardwareBatchingIfSupported(false)
                    .setReportDelay(1000)
                    .build();

            scanner.startScan(new ArrayList<ScanFilter>(), settings, scanCallback);
            scanning = true;
        }
    }

    private void stopScan() {
        if(scanning) {
            Log.d(TAG, "Stopping scan");

            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);
            scanning = false;
        }
    }
}
