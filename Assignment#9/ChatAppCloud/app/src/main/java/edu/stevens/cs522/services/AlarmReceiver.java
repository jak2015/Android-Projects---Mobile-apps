package edu.stevens.cs522.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import edu.stevens.cs522.activities.ChatApp;
import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.helpers.ServiceHelper;

public class AlarmReceiver extends BroadcastReceiver{

    public static final String TAG = AlarmReceiver.class.getCanonicalName();
    @Override
    public void onReceive(Context context, Intent intent)
    {

        ServiceHelper.getInstance(context).sync((ResultReceiver) intent.getExtras().getParcelable(constant.RECEIVER));
        Log.i(TAG, "onReceive: Syncing Messages");
        Toast.makeText(context, "Syncing Messages", Toast.LENGTH_SHORT).show();
    }
}
