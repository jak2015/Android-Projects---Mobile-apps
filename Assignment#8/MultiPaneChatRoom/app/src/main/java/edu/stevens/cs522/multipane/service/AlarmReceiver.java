package edu.stevens.cs522.multipane.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import edu.stevens.cs522.multipane.helper.ServiceHelper;

public class AlarmReceiver extends BroadcastReceiver{

    @Override
       public void onReceive(Context context, Intent intent)
       {
    	
   	 		ServiceHelper.getInstance(context).sync();
               //Toast.makeText(context, "Alarm Triggered, syncing message", Toast.LENGTH_LONG).show();
               }
       // }

}
