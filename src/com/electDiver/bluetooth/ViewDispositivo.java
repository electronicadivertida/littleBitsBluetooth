
package com.electDiver.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import android.util.Log;
/**
 *
 * @author eduardo
 */
public class ViewDispositivo {

    private final String mcsConectar;
    private final String mcsDesConectar;

    TextView deviceName;
    TextView deviceAddress;
    CheckBox chkValor;
    CheckBox chkSet;
    Button btnConectar;
    BluetoothDevice device;
    private BluetoothSocket mmSocket;
    private ConnectedThread moConexion;
    String mmUUID;
    ScanDispositivosActivity moActividad;

    ViewDispositivo(BluetoothDevice podevice, ScanDispositivosActivity poActividad, String psUUID, String psConectar, String psDesconectar) {
        device = podevice;
        moActividad = poActividad;
        mmUUID = psUUID;
        mcsConectar = psConectar;
        mcsDesConectar = psDesconectar;
    }

    public void rellenar() {
//luego no se conecta        
//            startFetch(device);

        deviceAddress.setText(device.getAddress());
        chkValor.setEnabled(false);
        chkSet.setEnabled(false);

        btnConectar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (btnConectar.getText().toString().equalsIgnoreCase(mcsDesConectar)) {
                    cancel();
                } else {
                    moActividad.cancelBusqueda();
                    btnConectar.setEnabled(false);
                    ConnectAsyncTask connectAsyncTask = new ConnectAsyncTask();
                    connectAsyncTask.execute(device);
                }
            }
        });
        chkSet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                moConexion.write((arg1 ? (byte) 1 : (byte) 0));
            }
        });

//            btnConectar.performClick();
        final String deviceText = device.getName();
        if (deviceText != null && deviceText.length() > 0) {
            deviceName.setText(deviceText);
        } else {
            deviceName.setText(R.string.unknown_device);
        }
    }
//luego no se conecta        
//        public void startFetch( BluetoothDevice device ) {
//            try {
//                device.fetchUuidsWithSdp();
////            } catch( NoSuchMethodException exc2 ) {
//            } catch( Throwable exc1 ) {
//                // Need to use reflection prior to API 15
//                Class cl = null;
//                try {
//                    cl = Class.forName("android.bluetooth.BluetoothDevice");
//                } catch( ClassNotFoundException exc ) {
//                    Log.e("ViewHolder", "android.bluetooth.BluetoothDevice not found." );
//                    Toast.makeText(moActividad, "android.bluetooth.BluetoothDevice not found.", Toast.LENGTH_SHORT).show();
//
//                }
//                if (null != cl) {
//                    Class[] param = {};
//                    Method method = null;
//                    try {
//                        method = cl.getMethod("fetchUuidsWithSdp", param);
//                    } catch( NoSuchMethodException exc ) {
//                        Log.e("ViewHolder", "fetchUuidsWithSdp not found." );
//                        Toast.makeText(moActividad, "fetchUuidsWithSdp not found.", Toast.LENGTH_SHORT).show();
//                    }
//                    if (null != method) {
//                        Object[] args = {};
//                        try {
//                            method.invoke(device, args);
//                        } catch (Exception exc) {
//                            Toast.makeText(moActividad, exc.toString(), Toast.LENGTH_SHORT).show();
//                            Log.e("ViewHolder", "Failed to invoke fetchUuidsWithSdp method." );
//                        }               
//                    }
//                }                     
//            }
//            
//        }        

    /**
     * Will cancel the listening socket, and cause the thread to finish
     */
    public void cancel() {
        try {
            chkSet.setEnabled(false);
            btnConectar.setText(mcsConectar);
            moConexion.cancel();
            moConexion = null;
            mmSocket = null;
        } catch (Throwable e) {
        }

    }

    public ConnectedThread getConexion() {
        return moConexion;
    }

    private class ConnectAsyncTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket> {

        private BluetoothSocket mmSocketL;
        private BluetoothDevice mmDevice;
        private ConnectedThread moConexionL;
        private Exception moError;

//        private byte[] convertPinToBytes(String string) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//
//            Method convert = device.getClass().getMethod("convertPinToBytes", String.class);
//
//            return (byte[]) convert.invoke(device, "1234");
//
//        }
//
//        private void setPin() {
//
//            try {
//                byte[] pinBytes = convertPinToBytes("1234");
//                Log.d("", "Try to set the PIN");
//                Method m = device.getClass().getMethod("setPin", byte[].class);
//                m.invoke(device, pinBytes);
//                Log.d("", "Success to add the PIN.");
//                try {
//                    device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
//                    Log.d("", "Success to setPairingConfirmation.");
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    Log.e("", e.getMessage());
//                    e.printStackTrace();
//                }
//            } catch (Exception e) {
//                Log.e("", e.getMessage());
//                e.printStackTrace();
//            }
//            device.setPairingConfirmation(true);
//            device.setPin("1234".getBytes());
//                try {
//                    Log.d("setPasskey()", "Try to set the PIN");
//                    Method m = device.getClass().getMethod("setPasskey", int.class);
//                    m.invoke(device, 1111);
//                    Log.d("setPasskey()", "Success to add the PIN.");
//                } catch (Exception e) {
//                    Log.e("setPin()", e.getMessage());
//                }
//                try {
//
//                   
//                    Method convert = device.getClass().getMethod("convertPinToBytes", String.class);
//
//                    byte[] pin = (byte[]) convert.invoke(device, "1234");
//
//                    Method setPin = device.getClass().getMethod("setPin", byte[].class);
//                    boolean success = (Boolean) setPin.invoke(device, pin);               
//                } catch (Exception e) {
//                  Log.e("setPin()", e.getMessage());
//                }                
//
//        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... device) {
            mmDevice = device[0];
            try {
//                setPin();
                mmSocketL = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(mmUUID));
                mmSocketL.connect();
                moConexionL = new ConnectedThread(mmSocketL);
                moConexionL.start();
            } catch (Exception e) {
                moError = e;
                e.printStackTrace();
            }
            return mmSocketL;
        }

        @Override
        protected void onPostExecute(BluetoothSocket result) {
            btnConectar.setEnabled(true);
            if (moError != null) {
                Toast.makeText(moActividad, moError.toString(), Toast.LENGTH_SHORT).show();

            } else {
                //Enable
                mmSocket = mmSocketL;                
                moConexion = moConexionL;
                btnConectar.setText(mcsDesConectar);
                chkSet.setEnabled(true);
                deviceName.setText(device.getName());
            }
        }

    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public int mlByte;//byte read
        
        public ConnectedThread(BluetoothSocket socket) throws IOException {
            mmSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    mlByte = mmInStream.read();
                    moActividad.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(moActividad, "read " + mlByte, Toast.LENGTH_SHORT).show();
                            if (mlByte > 0) {
                                chkValor.setChecked(true);
                            } else {
                                chkValor.setChecked(false);
                            }
                        }
                    });
                } catch (Throwable e) {
                    ViewDispositivo.this.cancel();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte pbByte) {
            try {
                Toast.makeText(moActividad, "write " + pbByte, Toast.LENGTH_SHORT).show();
                mmOutStream.write(pbByte);
            } catch (IOException e) {
                Toast.makeText(moActividad, "write " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
            try {
                mmOutStream.close();
            } catch (IOException e) {
            }
            try {
                mmInStream.close();
            } catch (IOException e) {
            }

        }
    }

}
//    class AcceptThreadServer extends Thread {
//        private final BluetoothServerSocket mmServerSocket;
//        private ConnectedThread moConexion;
//
//        public AcceptThreadServer(BluetoothDevice poDevice) {
//            // Use a temporary object that is later assigned to mmServerSocket,
//            // because mmServerSocket is final
//            BluetoothServerSocket tmp = null;
//            try {
//                // MY_UUID is the app's UUID string, also used by the client code
//                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(poDevice.getName(), UUID.fromString(mmUUID));
//            } catch (Exception e) { }
//            mmServerSocket = tmp;
//        }
//
//        public void run() {
//            BluetoothSocket socket = null;
//            // Keep listening until exception occurs or a socket is returned
//            while (true) {
//                try {
//                    socket = mmServerSocket.accept();
//                } catch (IOException e) {
//                    break;
//                }
//                // If a connection was accepted
//                if (socket != null) {
//                    try {
//                        // Do work to manage the connection (in a separate thread)
//                        moConexion = new  ConnectedThread(socket);
//                        mmServerSocket.close();
//                    } catch (IOException e) {
//                        break;
//                    }                
//                    break;
//                }
//            }
//        }
//
//        /** Will cancel the listening socket, and cause the thread to finish */
//        public void cancel() {
//            try {
//                mmServerSocket.close();
//            } catch (IOException e) { }
//            try {
//                moConexion.cancel();
//            } catch (Exception e) { }
//            
//        }
//    } 
//    
