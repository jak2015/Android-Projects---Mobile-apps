package edu.stevens.cs522.helpers;

import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.requests.Register;
import edu.stevens.cs522.services.RequestService;

import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

public class ServiceHelper {

	public static final String TAG = ServiceHelper.class.getCanonicalName();
	public Context ctx;
	private static Object lock = new Object();
	private static ServiceHelper instance;

	private ServiceHelper(Context ctx) {
		this.ctx = ctx.getApplicationContext();
	}

	public static ServiceHelper getInstance(Context ctx) {
		synchronized (lock) {
            Log.i(TAG, "getInstance(): new Service Helper instance created");
			if (instance == null) {
				instance = new ServiceHelper(ctx);
			}else{
				instance.ctx = ctx.getApplicationContext();
			}
		}
		return instance;
	}

	public void register(Register register, ResultReceiver receiver) {
		//Intent i = new Intent(constant.REGISTER_ACTION);
		Intent i = new Intent(ctx, RequestService.class);
		Log.i(TAG, "Registering: " + this.ctx.getClass().toString());
		i.putExtra(constant.REGISTER, register);
		i.putExtra(constant.RECEIVER, receiver);
		this.ctx.startService(i);
	}

    public void sync(ResultReceiver receiver) {
        Log.i(TAG, "Syncing: " + this.ctx.getClass().toString());
        Intent i = new Intent(ctx, RequestService.class);
        i.putExtra(constant.RECEIVER, receiver);
        i.putExtra(constant.TYPE, 1);
        this.ctx.startService(i);
    }

	public void unregister() {
		Intent i = new Intent(constant.UNREGISTER_ACTION);
		Log.i(TAG, "unRegistering: "+this.ctx.getClass().toString());
		i.putExtra(constant.UNREGISTER, 1);
		this.ctx.startService(i);
	}
}