package com.mlt.kingsongwear;

/**
 * Created by Marko on 28.8.2017.
 */

public class KingsongData {

    private int mVoltage;
    private int mSpeed;
    private long mTotalDistance;
    private int mCurrent;
    private int mTemperature;
    private byte mMode;
    private int mBattery;
    private long mDistance;
    private int mCurrentTime;
    private int mTopSpeed;
    private byte mFanStatus;
    private String mName;
    private String mModel;
    private int mVersion;
    private String mSerialNumber;

    public float getSpeed() {
        return (float) mSpeed / 100;
    }

    private int byteArrayInt2(byte low, byte high) {
        return (low & 255) + ((high & 255) * 256);
    }

    private long byteArrayInt4(byte value1, byte value2, byte value3, byte value4) {
        return (((((long) ((value1 & 255) << 16))) | ((long) ((value2 & 255) << 24))) | ((long) (value3 & 255))) | ((long) ((value4 & 255) << 8));
    }

    public KingsongData() {
    }

    @Override
    public String toString() {
        return "mSpeed="+mSpeed + " mVoltage="+mVoltage + " mCurrent=" + mCurrent;
    }

    public boolean decodeKingSong(byte[] data) {

        if (data.length >= 20) {
            int a1 = data[0] & 255;
            int a2 = data[1] & 255;
            if (a1 != 170 || a2 != 85) {
                return false;
            }
            if ((data[16] & 255) == 169) { // Live data
                mVoltage = byteArrayInt2(data[2], data[3]);
                mSpeed = byteArrayInt2(data[4], data[5]);
                mTotalDistance = byteArrayInt4(data[6], data[7], data[8], data[9]);
                mCurrent = byteArrayInt2(data[10], data[11]);
                if (mCurrent > 7000) {
                    mCurrent = 7000;
                } else if (mCurrent < 0) {
                    mCurrent = 0;
                }
                mTemperature = byteArrayInt2(data[12], data[13]);

                if ((data[15] & 255) == 224) {
                    mMode = data[14];
                }

                int battery;
                if (mVoltage < 5000) {
                    battery = 0;
                } else if (mVoltage >= 6600) {
                    battery = 100;
                } else {
                    battery = (mVoltage - 5000) / 16;
                }
                mBattery = battery;

                return true;
            } else if ((data[16] & 255) == 185) { // Distance/Time/Fan Data
                long distance = byteArrayInt4(data[2], data[3], data[4], data[5]);
                mDistance = distance;
                int currentTime = byteArrayInt2(data[6], data[7]);
                mCurrentTime = currentTime;
                mTopSpeed = byteArrayInt2(data[8], data[9]);
                mFanStatus = data[12];
            } else if ((data[16] & 255) == 187) { // Name and Type data
                int end = 0;
                int i = 0;
                while (i < 14 && data[i + 2] != 0) {
                    end++;
                    i++;
                }
                mName = new String(data, 2, end).trim();
                mModel = "";
                String[] ss = mName.split("-");
                for (i = 0; i < ss.length - 1; i++) {
                    if (i != 0) {
                        mModel += "-";
                    }
                    mModel += ss[i];
                }
                try {
                    mVersion = Integer.parseInt(ss[ss.length - 1]);
                } catch (Exception ignored) {
                }

            } else if ((data[16] & 255) == 179) { // Serial Number
                byte[] sndata = new byte[18];
                System.arraycopy(data, 2, sndata, 0, 14);
                System.arraycopy(data, 17, sndata, 14, 3);
                sndata[17] = (byte) 0;
                mSerialNumber = new String(sndata);
            }
        }
        return false;
    }

    public float getVoltage() {
        return (float)mVoltage/100;
    }
    public int getBattery() { return mBattery; }
    public float getTemperature() { return (float)mTemperature/100; }
    public float getCurrent() { return (float)mCurrent/100; }
}
