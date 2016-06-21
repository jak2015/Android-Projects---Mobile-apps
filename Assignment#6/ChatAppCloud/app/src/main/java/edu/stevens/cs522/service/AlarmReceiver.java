package edu.stevens.cs522.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import edu.stevens.cs522.helper.ServiceHelper;

public class AlarmReceiver extends BroadcastReceiver{

    @Override
       public void onReceive(Context context, Intent intent)
       {
           ServiceHelper.getInstance(context).sync();
           Toast.makeText(context, "syncing", Toast.LENGTH_SHORT).show();
       }
}
