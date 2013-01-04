/**
 * BluetoothReceiver.java
 * @date Jan 4, 2013
 * @author ricky barrette
 * 
 * Copyright 2012 Richard Barrette 
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
 * limitations under the License
 */
package org.RickBarrette.android.LocationRinger.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This class will be used to maintain a log of connected devices
 * @author ricky barrette
 */
public class BluetoothReceiver extends BroadcastReceiver {
	
	public static final String TAG = "BluetoothReceiver";
	public static final String NUMBER_CONNECTED = "number_connected";

	@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        SharedPreferences sp = context.getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS);
        Editor editor = sp.edit();
        int connected = sp.getInt(NUMBER_CONNECTED, 0);
        
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
           connected++;

        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
           connected--;
        
        editor.putInt(NUMBER_CONNECTED, connected);
        editor.apply();
    }

}
