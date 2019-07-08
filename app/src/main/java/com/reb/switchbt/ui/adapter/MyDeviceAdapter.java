package com.reb.switchbt.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;


import com.clj.fastble.data.BleDevice;
import com.reb.switchbt.db.DeviceBond;
import com.reb.switchbt.R;
import com.reb.switchbt.ui.util.ScanController;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class MyDeviceAdapter extends BaseAdapter {

    private List<DeviceBond> devices = new ArrayList<>();
    private Context mContext;
    private ConnectDeviceListener mListener;
    private ScanController scanControler;

    public MyDeviceAdapter(Context context, ScanController scanControler) {
        this.mContext = context;
        this.scanControler = scanControler;
    }

    public void setDevices(List<DeviceBond> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
        notifyDataSetChanged();
    }


    public void addDevice(DeviceBond deviceBond) {
        devices.add(deviceBond);
        notifyDataSetChanged();
    }
    public void updateDevice(DeviceBond deviceBond) {
        for (int i = 0; i < devices.size(); i++) {
            if (deviceBond.getMac().equals(devices.get(i).getMac())) {
                devices.set(i, deviceBond);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void deleteDevice(DeviceBond deviceBond) {
        for (int i = 0; i < devices.size(); i++) {
            if (deviceBond.getMac().equals(devices.get(i).getMac())) {
                devices.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

//    @Override
//    public int getViewTypeCount() {
//        return 2;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        DeviceBond deviceBond = devices.get(position);
//        if (deviceBond.isSelect)
//            return 1;
//        return 2;
//    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_my_device, null);
            vh = new ViewHolder();
            vh.mNameView = convertView.findViewById(R.id.item_device_name);
            vh.mAddressView = convertView.findViewById(R.id.item_device_address);
            vh.mConnectBtn = convertView.findViewById(R.id.item_device_connect);
            vh.relay1 = convertView.findViewById(R.id.relay1);
            vh.relay2 = convertView.findViewById(R.id.relay2);
            vh.relay3 = convertView.findViewById(R.id.relay3);
            vh.relay1Switch = convertView.findViewById(R.id.relay1_switch);
            vh.relay2Switch = convertView.findViewById(R.id.relay2_switch);
            vh.relay3Switch = convertView.findViewById(R.id.relay3_switch);
//            vh.relaybg = convertView.findViewById(R.id.relaybg);
            convertView.setTag(vh);
            scanControler.addView(vh.mConnectBtn);
            scanControler.addView(vh.relay1Switch);
            scanControler.addView(vh.relay2Switch);
            scanControler.addView(vh.relay3Switch);
//            if (getItemViewType(i) == 1) {
//                vh.relay1.setVisibility(View.VISIBLE);
//                vh.relay2.setVisibility(View.VISIBLE);
//                vh.relay3.setVisibility(View.VISIBLE);
//            }
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        final DeviceBond device = devices.get(i);
        vh.mNameView.setText(device.getDisplay_name());
        vh.mAddressView.setText(device.getMsg());
        View.OnClickListener relaySwitch = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int index = 1;
                    switch (v.getId()) {
                        case R.id.relay1_switch:
                            index = 1;
                            break;
                        case R.id.relay2_switch:
                            index = 2;
                            break;
                        case R.id.relay3_switch:
                            index = 3;
                            break;
                    }
                    mListener.switchRelay(device, index, ((Switch)v).isChecked());
                }
            }
        };
        vh.relay1Switch.setOnClickListener(relaySwitch);
        vh.relay2Switch.setOnClickListener(relaySwitch);
        vh.relay3Switch.setOnClickListener(relaySwitch);
        if (!scanControler.isScanning()) {
            vh.mConnectBtn.setEnabled(device.isOnline && !device.isConnectting);
            vh.relay1Switch.setEnabled(device.isConnected);
            vh.relay2Switch.setEnabled(device.isConnected);
            vh.relay3Switch.setEnabled(device.isConnected);
        }
        vh.mConnectBtn.setChecked(device.isConnected || device.isConnectting);
        vh.mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.connectDevice(device, ((Switch)v).isChecked());
                }
            }
        });
        int visibility = device.isSelect ? View.VISIBLE:View.GONE;
        vh.relay1.setVisibility(visibility);
        vh.relay2.setVisibility(visibility);
        vh.relay3.setVisibility(visibility);
        if (device.isConnected) {
            vh.relay1Switch.setChecked(device.relayState[0]);
            vh.relay2Switch.setChecked(device.relayState[1]);
            vh.relay3Switch.setChecked(device.relayState[2]);
        } else {
            vh.relay1Switch.setChecked(device.relayState[0]);
            vh.relay2Switch.setChecked(device.relayState[1]);
            vh.relay3Switch.setChecked(device.relayState[2]);
        }
        switch (device.relayCount) {
            case 1:
                vh.relay2.setVisibility(View.GONE);
                vh.relay3.setVisibility(View.GONE);
                break;
            case 2:
                vh.relay3.setVisibility(View.GONE);
                break;
        }
        return convertView;
    }

    public void setConnectDeviceListener(ConnectDeviceListener deviceListFragment) {
        this.mListener = deviceListFragment;
    }

    public boolean refreshOnLineState(BleDevice device) {
        String mac = device.getMac();
        for (DeviceBond deviceBond: devices) {
            if (mac.equals(deviceBond.getMac())) {
                if (!deviceBond.isOnline) {
                    deviceBond.isOnline = true;
                    deviceBond.device = device;
                    notifyDataSetChanged();
                }
                return true;
            }
        }
        return false;
    }

    public void clearOnLineState() {
        for (DeviceBond deviceBond: devices) {
            if (!deviceBond.isConnected) {
                deviceBond.isOnline = false;
            }
            deviceBond.isSelect = false;
        }
        notifyDataSetChanged();
    }

    public List<DeviceBond> getData() {
        return devices;
    }

    public void setSelect(int position) {
        DeviceBond deviceBond = devices.get(position);
        if (deviceBond.isSelect) {
            deviceBond.isSelect = false;
            hasSelect = false;
        } else {
            for (DeviceBond device: devices) {
                device.isSelect = false;
            }
            if (deviceBond.isConnected) {
                deviceBond.isSelect = true;
                hasSelect = true;
            }
        }
        notifyDataSetChanged();
    }

    private boolean hasSelect = false;
    public boolean hasSelect() {
        return hasSelect ;
    }


    private class ViewHolder {
        TextView mNameView;
        TextView mAddressView;
        Switch mConnectBtn;
        ViewGroup relay1,relay2,relay3;
        Switch relay1Switch,relay2Switch,relay3Switch;
//        View relaybg;
    }

    public interface ConnectDeviceListener {
        void connectDevice(DeviceBond device, boolean isChecked);
        void switchRelay(DeviceBond device, int index, boolean isChecked);
    }
}
