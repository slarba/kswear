package com.mlt.kingsongwear;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class SpeedometerActivity extends WearableActivity {

    private LinearLayout mContainerView;
    private TextView mSpeedView;
    private TextView mBatteryView;
    private String mDeviceAddress;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mGatt;
    private KingsongData mKingsongData = new KingsongData();

    private static final String TAG = "SpeedometerActivity";

    private static final UUID KINGSONG_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID KINGSONG_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static final UUID KINGSONG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private void startScanActivity() {
        Intent intent = new Intent(this, BTScanActivity.class);
        startActivity(intent);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public boolean reconnected = false;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT client. Attempting to start service discovery");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpeedView.setTextColor(getResources().getColor(R.color.white));
                    }
                });
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT client");

                reconnected = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpeedView.setTextColor(getResources().getColor(R.color.red));
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Toast.makeText(SpeedometerActivity.this, "No GATT", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "Services discovered");

            BluetoothGattCharacteristic characteristic = gatt.getService(KINGSONG_SERVICE)
                    .getCharacteristic(KINGSONG_CHARACTERISTIC);
            gatt.setCharacteristicNotification(characteristic, true);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KINGSONG_DESCRIPTOR);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            readKingsongCharacteristic(characteristic);
        }

        private void readKingsongCharacteristic(BluetoothGattCharacteristic characteristic) {
            if(KINGSONG_CHARACTERISTIC.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                mKingsongData.decodeKingSong(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDisplay();
                    }
                });
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "Kingsong data changed");
            readKingsongCharacteristic(characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if(KINGSONG_DESCRIPTOR.equals(descriptor.getUuid())) {
                Log.d(TAG, "Descriptor written");
                if(!reconnected) requestNameData(null);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
    private TextView mTempView;
    private TextView mCurrentView;
    private TextView mClockView;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGatt.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedometer);
        setAmbientEnabled();

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra("deviceAddress");
        connectWheel(mDeviceAddress);

        mContainerView = (LinearLayout) findViewById(R.id.container);

        mSpeedView = (TextView) findViewById(R.id.speed);
        mBatteryView = (TextView) findViewById(R.id.battery);
        mTempView = (TextView) findViewById(R.id.temperature);
        mCurrentView = (TextView) findViewById(R.id.current);
        mClockView = (TextView) findViewById(R.id.clock);
    }

    private void connectWheel(String deviceAddress) {
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        mGatt = device.connectGatt(this, true, mGattCallback);
    }

    public void playHorn(View view) {
        byte[] data = new byte[20];
        data[0] = (byte) -86;
        data[1] = (byte) 85;
        data[16] = (byte) -120;
        data[17] = (byte) 20;
        data[18] = (byte) 90;
        data[19] = (byte) 90;
        BluetoothGattCharacteristic c = mGatt.getService(KINGSONG_SERVICE).getCharacteristic(KINGSONG_CHARACTERISTIC);
        c.setValue(data);
        c.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mGatt.writeCharacteristic(c);
    }

    public void requestNameData(View view) {
        byte[] data = new byte[20];
        data[0] = (byte) -86;
        data[1] = (byte) 85;
        data[16] = (byte) -101;
        data[17] = (byte) 20;
        data[18] = (byte) 90;
        data[19] = (byte) 90;
        BluetoothGattCharacteristic c = mGatt.getService(KINGSONG_SERVICE).getCharacteristic(KINGSONG_CHARACTERISTIC);
        c.setValue(data);
        c.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mGatt.writeCharacteristic(c);
    }

    public void requestSerialData(View view) {
        byte[] data = new byte[20];
        data[0] = (byte) -86;
        data[1] = (byte) 85;
        data[16] = (byte) 99;
        data[17] = (byte) 20;
        data[18] = (byte) 90;
        data[19] = (byte) 90;
        BluetoothGattCharacteristic c = mGatt.getService(KINGSONG_SERVICE).getCharacteristic(KINGSONG_CHARACTERISTIC);
        c.setValue(data);
        c.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mGatt.writeCharacteristic(c);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    public void speedAlert(View view) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {0, 10, 200, 10};
        vibrator.vibrate(pattern, -1);
    }

    private void updateDisplay() {
        if(mKingsongData.getSpeed()>=34) {
            speedAlert(null);
        }

        if (isAmbient()) {
            mSpeedView.setText(String.format("%.1f", mKingsongData.getSpeed()));
            mBatteryView.setText(String.format("%d ", mKingsongData.getBattery()));
            mTempView.setText(String.format("%.1f ", mKingsongData.getTemperature()));
            mCurrentView.setText(String.format("%.1f ", mKingsongData.getCurrent()));
            mClockView.setText(android.text.format.DateFormat.format("HH:mm", new Date()));
        } else {
            mSpeedView.setText(String.format("%.1f", mKingsongData.getSpeed()));
            mBatteryView.setText(String.format("%d ", mKingsongData.getBattery()));
            mTempView.setText(String.format("%.1f ", mKingsongData.getTemperature()));
            mCurrentView.setText(String.format("%.1f ", mKingsongData.getCurrent()));
            mClockView.setText(android.text.format.DateFormat.format("HH:mm", new Date()));
        }
    }
}
