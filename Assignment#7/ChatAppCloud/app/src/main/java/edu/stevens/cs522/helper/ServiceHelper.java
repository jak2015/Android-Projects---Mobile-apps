package edu.stevens.cs522.helper;

import edu.stevens.cs522.requests.Register;
import edu.stevens.cs522.service.RequestService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class ServiceHelper {

	// private static final ServiceHelper INSTANCE = new ServiceHelper();
	public static String host = "http://localhost:8080";
	int type;
	ResultReceiver resultReceiver;
	Register register;
	public Context ctx;
	private static Object lock = new Object();
	private static ServiceHelper instance;

	private ServiceHelper(Context ctx) {
		this.ctx = ctx.getApplicationContext();
	}

	public static ServiceHelper getInstance(Context ctx) {
		synchronized (lock) {
			if (instance == null) {
				instance = new ServiceHelper(ctx);
			}else{
				instance.ctx = ctx.getApplicationContext();
			}
		}
		return instance;
	}

	public void register(Register register, ResultReceiver receiver) {
		Intent intent = new Intent(RequestService.REGISTER_ACTION);
		Log.d("register helper", this.ctx.getClass().toString());
		intent.putExtra("register", register);
		intent.putExtra("receiver", receiver);
		this.ctx.startService(intent);
		// TODO Auto-generated method stub
	}

	public void sync() {
		Intent i = new Intent(ctx, RequestService.class);
		Log.d("Sync helper", this.ctx.getClass().toString());
		i.putExtra("type", 1);
		this.ctx.startService(i);
	}

	public void unregister() {
		// TODO Auto-generated method stub
		Intent i = new Intent(RequestService.UNREGISTER_ACTION);
		Log.d("unRegister helper", this.ctx.getClass().toString());
		i.putExtra("unregister", 1);
		this.ctx.startService(i);
	}
}