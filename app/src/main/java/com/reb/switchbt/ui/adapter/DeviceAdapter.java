package com.reb.switchbt.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.clj.fastble.data.BleDevice;
import com.reb.switchbt.db.DeviceBond;
import com.reb.switchbt.R;
import com.reb.switchbt.profile.DeviceConfig;
import com.reb.switchbt.ui.util.ScanController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class DeviceAdapter extends BaseAdapter {

    private List<DeviceBond> devices = new ArrayList<>();
    private Context mContext;
    private OnAddMyDeviceListener mListener;
    private ScanController scanControler;

    public DeviceAdapter(Context context, ScanController scanControler) {
        this.mContext = context;
        this.scanControler = scanControler;
    }

    public void setDevices(List<DeviceBond> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
//        for (int i = 0; i < devices.size(); i++) {
//            DeviceBond device = devices.get(i);
//            this.devices.add(device);
//
//        }
        notifyDataSetChanged();
    }

//    public void refreshOnLineState(ExtendedBluetoothDevice device) {
//        String mac = device.device.getAddress();
//        for (DeviceBond deviceBond: devices) {
//            if (mac.equals(deviceBond.getMac())) {
//                deviceBond.isOnline = true;
//                DebugLog.i(deviceBond.getDisplay_name() + "---isOnline?" + true);
//                break;
//            }
//        }
//    }

    public void removeDevice(DeviceBond device) {
        int index = -1;
        for (int i = 0; i < devices.size(); i++) {
            if(device.getMac().equals(devices.get(i).getMac())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            devices.remove(index);
            notifyDataSetChanged();
        }
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

    public void addOrUpdateDevice(BleDevice device) {
        for (DeviceBond deviceBond:devices) {
            if (device.getMac().equals(deviceBond.getMac())) {
                if (!deviceBond.isOnline) {
                    deviceBond.isOnline = true;
                    notifyDataSetChanged();
                }
                return;
            }
        }
        DeviceBond deviceBond = new DeviceBond(device.getMac(), device.getName(), "", device.getName(), DeviceConfig.defaultPsw, 1);
        deviceBond.device = device;
        devices.add(deviceBond);
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

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device, null);
            vh = new ViewHolder();
            vh.mNameView = convertView.findViewById(R.id.item_device_name);
            vh.mAddressView = convertView.findViewById(R.id.item_device_address);
            vh.mBondView = convertView.findViewById(R.id.item_device_bondState);
            vh.mPairBtn = convertView.findViewById(R.id.item_device_connect);
            vh.mPairBtn.setText(mContext.getString(com.reb.switchbt.R.string.add_my_devices));
            scanControler.addView(vh.mPairBtn);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        final DeviceBond device = devices.get(i);
        String name;
        if (!TextUtils.isEmpty(device.getName())) {
            name = device.getName();
        } else {
            name = mContext.getString(R.string.unknown);
        }
        vh.mNameView.setText(name);
        vh.mAddressView.setText(device.getMac());
        vh.mPairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAddMyDevice(device);
                    devices.remove(device);
                    notifyDataSetChanged();
                }
            }
        });
        scanControler.addView(vh.mPairBtn);
        return convertView;
    }


    private class ViewHolder {
        TextView mNameView;
        TextView mAddressView;
        TextView mBondView;
        Button mPairBtn;
    }

    public void setOnAddMyDeviceListener (OnAddMyDeviceListener listener) {
        mListener = listener;
    }

    public interface OnAddMyDeviceListener {
        void onAddMyDevice(DeviceBond device);
    }
}
