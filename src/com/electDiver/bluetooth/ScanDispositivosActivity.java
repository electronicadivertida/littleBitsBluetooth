/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.electDiver.bluetooth;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Activity for scanning and displaying available Bluetooth littlebits devices.
 */
public class ScanDispositivosActivity extends ListActivity {
    private DeviceListAdapter mDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private final String mmUUID = "00001101-0000-1000-8000-00805F9B34FB";

    private static final int REQUEST_ENABLE_BT = 1;
    private Menu menu;
    private boolean mbPrimeraVez=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // register event
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); 
        IntentFilter filterNAME = new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(mReceiverNAME, filterNAME); 
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu=menu;
        habilitarDeshabilitarMenus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                empezarBusqueda();
                break;
            case R.id.menu_stop:
                 cancelBusqueda();              
                break;
            case R.id.add_master:
                //nota: se pueden ir almacenando todos los bluetooth q se van encontrando, actualizando sus nombres
                //y luego seleccionasr entre una lista
                mDeviceListAdapter.addDevice(mBluetoothAdapter.getRemoteDevice("20:14:02:17:19:47"));
                setListAdapter(mDeviceListAdapter);
                
//                    BluetoothServerSocket loServer = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("LITTLE1", UUID.randomUUID());
//                    BluetoothSocket loSocket = loServer.accept(5000);
//                    loSocket.connect();
//                    loSocket.getInputStream().close();
//                    loSocket.close();
                break;
        }
        return true;
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }    
    /**1º borramos los ya enlazados, por si ha cambiado de nombre o de contraseña*/
    private void borrarYaEnlazados(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // Loop through paired devices
        for (BluetoothDevice device : pairedDevices) {
            // delete the device littlebits by refresh
            if(mDeviceListAdapter.isDeviceValido(device)){
                unpairDevice(device);
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if(mbPrimeraVez){
            empezarBusqueda();
            mbPrimeraVez=false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelBusqueda();
    }

    @Override
    protected void onDestroy() {
        mDeviceListAdapter.clear();
        unregisterReceiver(mReceiver); 
        unregisterReceiver(mReceiverNAME); 
        super.onDestroy(); 
    }
    
    

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {}
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(DeviceScanActivity.this,"BroadcastReceiver", Toast.LENGTH_SHORT).show();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    Toast.makeText(DeviceScanActivity.this
//                        , "broad name: " + device.getName(), Toast.LENGTH_SHORT).show();
                    // Add the name and address to an array adapter to show in a ListView
                    mDeviceListAdapter.addDevice(device);
                    setListAdapter(mDeviceListAdapter);
                   
            }
        }
    };
    // Create a BroadcastReceiver for ACTION_NAME_CHANGED
    private final BroadcastReceiver mReceiverNAME = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(DeviceScanActivity.this,"BroadcastReceiver name", Toast.LENGTH_SHORT).show();
//            // When discovery finds a device
//            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(intent.getAction())) {
//                    // Get the BluetoothDevice object from the Intent
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
////                    Toast.makeText(DeviceScanActivity.this
////                        , "broad name: " + device.getName(), Toast.LENGTH_SHORT).show();
//                    // Add the name and address to an array adapter to show in a ListView
//                    mDeviceListAdapter.addDevice(device);
//                    setListAdapter(mDeviceListAdapter);
//                   
//            }
        }
    };

    
    private void habilitarDeshabilitarMenus(){
        if(menu!=null){
            if (!mScanning) {
                menu.findItem(R.id.menu_stop).setVisible(false);
                menu.findItem(R.id.menu_scan).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
            } else {
                menu.findItem(R.id.menu_stop).setVisible(true);
                menu.findItem(R.id.menu_scan).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(
                        R.layout.actionbar_indeterminate_progress);
                
            }
        }
    }
    public void cancelBusqueda() {
        mBluetoothAdapter.cancelDiscovery();
        mScanning=false;
        habilitarDeshabilitarMenus();
    }

    public void empezarBusqueda() {      
        if(mDeviceListAdapter!=null){
            mDeviceListAdapter.clear();
        }else{
            mDeviceListAdapter = new DeviceListAdapter();
        }
        borrarYaEnlazados();
        setListAdapter(mDeviceListAdapter);
//        Toast.makeText(this, "startDiscovery", Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.startDiscovery();
        mScanning=true;
        habilitarDeshabilitarMenus();
    }

   
    // Adapter for holding devices found through scanning.
    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mBTDevices;
        private ArrayList<ViewDispositivo> mViews;
        
        private LayoutInflater mInflator;

        public DeviceListAdapter() {
            super();
            mBTDevices = new ArrayList<BluetoothDevice>();
            mViews = new ArrayList<ViewDispositivo>();
            mInflator = ScanDispositivosActivity.this.getLayoutInflater();
        }
        private boolean isDeviceValido(BluetoothDevice device){
            return (device.getName() ==null 
                    || device.getName().equals("")
                    || device.getName().indexOf("LITTLE")>=0 
                    );
        }


        public void addDevice(BluetoothDevice device) {
            
            if(!mBTDevices.contains(device) && isDeviceValido(device)) {
                mBTDevices.add(device);
                
                ViewDispositivo viewHolder = new ViewDispositivo(device, ScanDispositivosActivity.this, mmUUID, getString(R.string.connected), getString(R.string.disconnected));
                mViews.add(viewHolder);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mBTDevices.get(position);
        }
        public ViewDispositivo getViewHolder(int position) {
            return mViews.get(position);
        }

        public void clear() {
            mBTDevices.clear();
            for(ViewDispositivo viewH : mViews){
                viewH.cancel();
            }
            mViews.clear();
        }

        @Override
        public int getCount() {
            return mBTDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mBTDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewDispositivo viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = mViews.get(i);
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.btnConectar = (Button) view.findViewById(R.id.btnConectar);
                viewHolder.chkSet = (CheckBox) view.findViewById(R.id.chkSet);
                viewHolder.chkValor = (CheckBox) view.findViewById(R.id.chkValor);
                view.setTag(viewHolder);
                viewHolder.rellenar();
            }
            
            return view;
        }
    }


}


