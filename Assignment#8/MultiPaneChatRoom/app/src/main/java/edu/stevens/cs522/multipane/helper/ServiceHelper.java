package edu.stevens.cs522.multipane.helper;

import edu.stevens.cs522.multipane.request.Register;
import edu.stevens.cs522.multipane.request.Unregister;
import edu.stevens.cs522.multipane.service.RequestService;
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
		//Intent i = new Intent(this.ctx, RequestService.class);
		Intent i = new Intent(RequestService.REGISTER_ACTION);
		Log.d("register helper", this.ctx.getClass().toString());
		i.putExtra("register", register);
		i.putExtra("receiver", receiver);
		this.ctx.startService(i);
		// TODO Auto-generated method stub

	}

	public void sync() {
		Intent i = new Intent(ctx, RequestService.class);
		Log.d("Sync helper", this.ctx.getClass().toString());
		i.putExtra("type", 1);
		this.ctx.startService(i);

	}
	
	

	public void unregister(Unregister unregister) {
		Intent i = new Intent(RequestService.REGISTER_ACTION);
		Log.d("unregister helper", this.ctx.getClass().toString());
		i.putExtra("unregister", unregister);
		i.putExtra("type", 2);
		this.ctx.startService(i);

	}

}
