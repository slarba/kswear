package com.mlt.kingsongwear;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * Created by Marko on 28.8.2017.
 */

public class ScanResultListAdapter extends ArrayAdapter<ScanResult> {
    public ScanResultListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ScanResult> objects) {
        super(context, resource, objects);
        setNotifyOnChange(false);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ScanResult result = getItem(position);
        if(convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.scanresultlayout, parent, false);
        }

        TextView deviceName = (TextView)convertView.findViewById(R.id.devicename);
        deviceName.setText(result.getScanRecord().getDeviceName());
        return convertView;
    }
}
