package edu.stevens.cs522.multipane.activity;

import java.util.Date;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import edu.stevens.cs522.multipane.R;
import edu.stevens.cs522.multipane.entity.ChatMessage;
import edu.stevens.cs522.multipane.fragment.AddRoomDialog;
import edu.stevens.cs522.multipane.fragment.SendMessageDialog;
import edu.stevens.cs522.multipane.provider.MessageProviderCloud;
import edu.stevens.cs522.multipane.service.AlarmReceiver;

public class FragmentLayout extends FragmentActivity  implements SendMessageDialog.EditNameDialogListener, AddRoomDialog.EditAddRoomDialogListener {
	private String clientName;
	private String portNo;
	private String hostStr;
	private String uuidStr;
	private ContentResolver cr;
	final static public String TAG = FragmentLayout.class.getCanonicalName();
	String clientId;
	AlarmManager alarmManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_layout);
		 SharedPreferences prefs = this.getSharedPreferences("multipanechatapp",
					Context.MODE_PRIVATE);
		 Intent myIntent = getIntent();
		 clientId = myIntent.getStringExtra("clientId");
			uuidStr = prefs.getString("UUID", "00000000-0000-11e3-a5e2-0800201c9a66");
			clientName = prefs.getString("clientName", "myself");
			portNo = prefs.getString("portNo", "8080");
			hostStr = prefs.getString("hostStr", "10.0.2.2");
			// set the alarm for particular time
			Long time = (long) (15 * 1000);
			Intent intentAlarm = new Intent(this, AlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(this, 12, intentAlarm, 0);
			intentAlarm.putExtra("clientId", clientId);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime(), time, sender);
			Toast.makeText(this, "Syncing with server within 5s", Toast.LENGTH_SHORT)
					.show();
			
	}
	@Override
	public void onStop() {
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(this, 12,
				intentAlarm, 0);
		try {
			alarmManager.cancel(sender);
		} catch (Exception e) {
			Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
		}
		super.onStop();
	}

	@Override
	public void onDestroy() {

		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(this, 12,
				intentAlarm, 0);
		try {
			alarmManager.cancel(sender);
		
//		cr.delete(MessageProviderCloud.CONTENT_URI, null, null);
//		cr.delete(MessageProviderCloud.CONTENT_URI_PEER, null, null);
//		cr.delete(MessageProviderCloud.CONTENT_URI_CHATROOM, null, null);
		} catch (Exception e) {
			Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
		}		
		SharedPreferences prefs = this.getSharedPreferences("multipanechatapp", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("clientName", clientName);
		super.onDestroy();
	}
	public static class DetailsActivity extends FragmentActivity implements SendMessageDialog.EditNameDialogListener, AddRoomDialog.EditAddRoomDialogListener {
		int id;
		ListView msgList;
		ContentResolver cr;
		SimpleCursorAdapter msgAdapter;
		public String[] from = new String[] { MessageProviderCloud.SENDER,
				MessageProviderCloud.TEXT };
		public int[] to = new int[] { R.id.cart_row_sender,
				R.id.cart_row_message };
		String clientId;
		private String clientName;
		private String portNo;
		private String hostStr;
		private String uuidStr;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// If the screen is now in landscape mode, we can show the
				// dialog in-line with the list so we don't need this activity.
				finish();
				return;
			}

			if (savedInstanceState == null) {
				// During initial setup, plug in the details fragment.
				DetailsFragment details = new DetailsFragment();
				id = getIntent().getIntExtra("index",0);
				details.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction()
						.add(android.R.id.content, details).commit();
			}
			 SharedPreferences prefs = this.getSharedPreferences("multipanechatapp",
						Context.MODE_PRIVATE);
			 Intent myIntent = getIntent();
			 clientId = myIntent.getStringExtra("clientId");
				uuidStr = prefs.getString("UUID",
						"00000000-0000-11e3-a5e2-0800201c9a66");
				clientName = prefs.getString("clientName", "myself");
				portNo = prefs.getString("portNo", "8080");
				hostStr = prefs.getString("hostStr", "10.0.0.6");
				// set the alarm for particular time
		}
		
		 private void showSendDialog() {
		        FragmentManager fm = getSupportFragmentManager();
		        SendMessageDialog sendMsgDialog = new SendMessageDialog();
		        sendMsgDialog.show(fm, "fragment_edit_name");
		    }
		    
		    private void showRoomDialog() {
		        FragmentManager fm = getSupportFragmentManager();
		        AddRoomDialog addRoomDialog = new AddRoomDialog();
		        addRoomDialog.show(fm, "fragment_send_msg");
		    }
		    @Override
		    public void onFinishEditDialog(String chatroom, String msg) {
		     //  Log.d("sending to" , chatroom + ": " +msg);
		       Toast.makeText(this, "sending to " + chatroom + ": " + msg, Toast.LENGTH_SHORT).show();
		       cr = this.getContentResolver();
		    	ContentValues values = new ContentValues();
		    	Cursor  c = cr.query(MessageProviderCloud.CONTENT_URI_CHATROOM, null, "name=?", new String[]{chatroom}, null);
		    	
		    	long roomId = -1;
		    	if(c.moveToFirst()){
		    		roomId =c.getLong(0);
		    	}
                AlarmManager alarmManager;
                Long time = (long) (15 * 1000);
                Intent intentAlarm = new Intent(this, AlarmReceiver.class);
                PendingIntent sender = PendingIntent.getBroadcast(this, 12,
                        intentAlarm, 0);
                intentAlarm.putExtra("clientId", clientId);
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(), time, sender);
                Toast.makeText(this, "Syncing with server within 5s", Toast.LENGTH_SHORT)
                        .show();
		   // 	Log.d("getting id for chatroom",String.valueOf(roomId));
		       //	values.put("name", chatroom);
		    	ContentValues cv = new ContentValues();
		    	ChatMessage cm = new ChatMessage(0, msg, clientName, 0, roomId, new Date().getTime(),1 );
		    	cm.writeToProvider(cv);
				try {
					getContentResolver().insert(MessageProviderCloud.CONTENT_URI,cv);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
		    }
		    
		    @Override
		    public void onFinishAddDialog(String chatroom) {
		       Toast.makeText(this, "Chat room " +chatroom+" added", Toast.LENGTH_SHORT).show();
		   	cr = this.getContentResolver();
		   	ContentValues values = new ContentValues();
		   	values.put("name", chatroom);
		   	Uri result = cr.insert(MessageProviderCloud.CONTENT_URI_CHATROOM, values);
		//        Log.d("INSERTED!", result.toString());
		        TitlesFragment tf = (TitlesFragment) this.getSupportFragmentManager().findFragmentById(R.id.titles);
		        tf.fresh();
		       
		    }
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);

			this.getMenuInflater().inflate(R.menu.chatroom, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			super.onOptionsItemSelected(item);
			switch (item.getItemId()) {
			case (R.id.action_add_room):
				showRoomDialog();
			     
				return true;
			case (R.id.action_compose):
				showSendDialog();
				return true;

            case (R.id.Back):
                Intent i=new Intent(getBaseContext(),SetHead.class);
                startActivityForResult(i,0);
                return true;
            }
			return false;
		}
	}
    public void goBack()
    {
        Intent i=new Intent(this,SetHead.class);
        startActivity(i);

    }
	public static class TitlesFragment extends ListFragment implements
			LoaderManager.LoaderCallbacks<Cursor> {
		boolean mDualPane;
		int mCurCheckPosition = 0;
		int mShownCheckPosition = -1;

		ListView msgList;
		// static ArrayAdapter<String> messagesAdapter;
		// static ArrayList<String> message;
		// ArrayList<String> messageAll;
		ContentResolver cr;
		SimpleCursorAdapter msgAdapter;
		public String[] from = new String[] { MessageProviderCloud.SENDER,
				MessageProviderCloud.TEXT };
		public int[] to = new int[] { R.id.cart_row_sender,
				R.id.cart_row_message };
		String clientId;

		public void fresh(){
			
			cr = getActivity().getContentResolver();
			Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI_CHATROOM,
					null, null, null, null);
			msgAdapter.changeCursor(cursor);
			setListAdapter(msgAdapter);
		}
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			cr = getActivity().getContentResolver();
			Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI_CHATROOM,
					null, null, null, null);

			msgAdapter = new SimpleCursorAdapter(getActivity(),
					android.R.layout.simple_list_item_1, null,
					new String[] { "name" }, new int[] { android.R.id.text1 });
			// Create an array adapter for the list view, using the Ipsum
			// headlines array
			// setListAdapter(new ArrayAdapter<String>(getActivity(), layout,
			// Ipsum.Headlines));

			// Toast.makeText(getActivity(), "querying chatroom",
			// Toast.LENGTH_SHORT).show();
			msgAdapter.changeCursor(cursor);
			// Populate list with our static array of titles.
			setListAdapter(msgAdapter);

			// Check to see if we have a frame in which to embed the details
			// fragment directly in the containing UI.
			View detailsFrame = getActivity().findViewById(R.id.details);
			mDualPane = detailsFrame != null
					&& detailsFrame.getVisibility() == View.VISIBLE;

			if (savedInstanceState != null) {
				// Restore last state for checked position.
				mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
				mShownCheckPosition = savedInstanceState.getInt("shownChoice",-1);
			}

			if (mDualPane) {
				// In dual-pane mode, the list view highlights the selected
				// item.
				getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				// Make sure our UI is in the correct state.
				showDetails(mCurCheckPosition,0);
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("curChoice", mCurCheckPosition);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			SharedPreferences prefs = getActivity().getSharedPreferences("multipanechatapp", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			Cursor c = ((SimpleCursorAdapter) l.getAdapter()).getCursor();
			c.moveToPosition(position);
			String selection ="_default";
			
//			String room="_default";
			
				selection = c.getString(1);
			
	//		Log.d("Select item ", selection);
			editor.putString("currchatroom", selection);
	//		Log.d("putting to spfr",selection);
			editor.commit();
			showDetails(position, id);
			Log.d("item clicked", "position:"+ String.valueOf(position) + " id:" + String.valueOf(id));
		}

		/**
		 * Helper function to show the details of a selected item, either by
		 * displaying a fragment in-place in the current UI, or starting a whole
		 * new activity in which it is displayed.
		 */
		void showDetails(int position, long id) {
			mCurCheckPosition = position;
	//		Log.d("Show Detail for", String.valueOf(position));
			if (mDualPane) {
				// We can display everything in-place with fragments, so update
				// the list to highlight the selected item and show the data.
				getListView().setItemChecked(position, true);

				// Check what fragment is currently shown, replace if needed.
				if (mShownCheckPosition != mCurCheckPosition) {
					// If we are not currently showing a fragment for the new
					// position, we need to create and install a new one.
					DetailsFragment df = DetailsFragment.newInstance((int)id);

					// Execute a transaction, replacing any existing fragment
					// with this one inside the frame.
					FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.details, df);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
					mShownCheckPosition = position;
				}
			} else {
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Intent intent = new Intent();
				intent.setClass(getActivity(), DetailsActivity.class);
				intent.putExtra("index",(int) id);
		//		Log.d("Fire intent", String.valueOf(id));
				startActivity(intent);
			}
		}

		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

			return new CursorLoader(getActivity(),
					MessageProviderCloud.CONTENT_URI_CHATROOM,
					MessageProviderCloud.chatroomProjection, null, null, null);
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			this.msgAdapter.swapCursor(cursor);

		}

		public void onLoaderReset(Loader<Cursor> loader) {
			this.msgAdapter.swapCursor(null);
		}
	}

	public static class DetailsFragment extends ListFragment implements
			LoaderManager.LoaderCallbacks<Cursor> {
		ListView msgList;
		ContentResolver cr;
		SimpleCursorAdapter msgAdapter;
		public String[] from = new String[] { MessageProviderCloud.SENDER,
				MessageProviderCloud.TEXT };
		public int[] to = new int[] { R.id.cart_row_sender,
				R.id.cart_row_message };
		String clientId;

		/**
		 * Create a new instance of DetailsFragment, initialized to show the
		 * text at 'index'.
		 */
		public void fresh(long mCurrentPosition){
			updateMsgListAdapter(mCurrentPosition);
		}
		
		public static DetailsFragment newInstance(int index) {
			DetailsFragment f = new DetailsFragment();

			// Supply index input as an argument.
			Bundle args = new Bundle();
			args.putInt("index", index);
			f.setArguments(args);

			return f;
		}

		public int getShownIndex() {
			int temp;
			Bundle b = getArguments();
		//	Log.d("detail Frag", String.valueOf(b.size()));
			temp = b.getInt("index", 0);
			
			return temp;
		}

		int mCurrentPosition ;

		@Override
		public void onStart() {
			super.onStart();

			// During startup, check if there are arguments passed to the
			// fragment.
			// onStart is a good place to do this because the layout has already
			// been
			// applied to the fragment at this point so we can safely call the
			// method
			// below that sets the article text.
			Bundle args = getArguments();
			if (args != null) {
				// Set article based on argument passed in
				updateMsgListAdapter(getShownIndex());
			} else if (mCurrentPosition != -1) {
				// Set article based on saved instance state defined during
				// onCreateView
				updateMsgListAdapter(mCurrentPosition);
			}
		}

		
		public void updateMsgListAdapter(long itemId) {
			// TextView article = (TextView)
			// getActivity().findViewById(R.id.article);
			// article.setText(Ipsum.Articles[position]);
			//mCurrentPosition = itemId;
			cr = getActivity().getContentResolver();
			Cursor cursor = cr.query(
					Uri.parse(MessageProviderCloud.CONTENT_URI_CHATROOM
							.toString() + "/0"),
					new String[] { "cols", "timestamp" }, " chatroom_fk=? ",
					new String[] { String.valueOf(itemId) }, null);

			msgAdapter = new SimpleCursorAdapter(getActivity(),
					android.R.layout.simple_list_item_2, null, new String[] {
							"cols", "timestamp" }, new int[] { android.R.id.text1,
							android.R.id.text2 });
			// Create an array adapter for the list view, using the Ipsum
			// headlines array
			// setListAdapter(new ArrayAdapter<String>(getActivity(), layout,
			// Ipsum.Headlines));

			// Toast.makeText(getActivity(), "querying chatroom",
			// Toast.LENGTH_SHORT).show();
			msgAdapter.changeCursor(cursor);
			setListAdapter(msgAdapter);

		}

		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

			return new CursorLoader(getActivity(),
					MessageProviderCloud.CONTENT_URI,
					MessageProviderCloud.messageProjection, null, null, null);
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);

			// Save the current article selection in case we need to recreate
			// the fragment
			outState.putInt("index", mCurrentPosition);
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			this.msgAdapter.swapCursor(cursor);

		}

		public void onLoaderReset(Loader<Cursor> loader) {
			this.msgAdapter.swapCursor(null);
		}

	}
	
    private void showSendDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SendMessageDialog sendMsgDialog = new SendMessageDialog();
        sendMsgDialog.show(fm, "fragment_edit_name");
    }
    
    private void showRoomDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddRoomDialog addRoomDialog = new AddRoomDialog();
        addRoomDialog.show(fm, "fragment_send_msg");
    }

    @Override
    public void onFinishEditDialog(String chatroom, String msg) {
   //    Log.d("sending to" , chatroom + ": " +msg);
       Toast.makeText(this, "sending to" + chatroom + ": " +msg, Toast.LENGTH_SHORT).show();
       cr = this.getContentResolver();
    	ContentValues values = new ContentValues();
    	Cursor  c = cr.query(MessageProviderCloud.CONTENT_URI_CHATROOM, null, "name=?", new String[]{chatroom}, null);
    	
    	long roomId = -1;
    	if(c.moveToFirst()){
    		roomId =c.getLong(0);
    	}
 //   	Log.d("getting id for chatroom",String.valueOf(roomId));
       //	values.put("name", chatroom);
    	ContentValues cv = new ContentValues();
    	ChatMessage cm = new ChatMessage(0, msg, clientName, 0, roomId, new Date().getTime(),1 );
    	cm.writeToProvider(cv);
		try {
			getContentResolver().insert(MessageProviderCloud.CONTENT_URI,cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DetailsFragment tf = (DetailsFragment) this.getSupportFragmentManager().findFragmentById(R.id.details);
        tf.fresh(roomId);
    }
    
    @Override
    public void onFinishAddDialog(String chatroom) {
    //   Log.d("add room" , chatroom );
       Toast.makeText(this, "send room" +chatroom, Toast.LENGTH_SHORT).show();
   	cr = this.getContentResolver();
   	ContentValues values = new ContentValues();
   	values.put("name", chatroom);
   	Uri result = cr.insert(MessageProviderCloud.CONTENT_URI_CHATROOM, values);
     //   Log.d("INSERTED!", result.toString());
        TitlesFragment tf = (TitlesFragment) this.getSupportFragmentManager().findFragmentById(R.id.titles);
        tf.fresh();
       
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		this.getMenuInflater().inflate(R.menu.chatroom, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.action_add_room):
			showRoomDialog();
		     
			return true;
		case (R.id.action_compose):
			showSendDialog();
			return true;
		}
		return false;
	}

}
