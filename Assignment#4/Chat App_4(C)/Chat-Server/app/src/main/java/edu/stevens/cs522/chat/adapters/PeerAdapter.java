package edu.stevens.cs522.chat.adapters;

import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.contracts.PeerContract;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class PeerAdapter extends ResourceCursorAdapter {

	protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_2;
	
	private SparseBooleanArray mSelectedItemsIds;

	public void selectView(int position, boolean value) {
		if (value)
			mSelectedItemsIds.put(position, value);
		else
			mSelectedItemsIds.delete(position);

		notifyDataSetChanged();
	}

	public void toggleSelection(int id) {
		selectView(id, !mSelectedItemsIds.get(id));
	}

	public void removeSelection() {
		mSelectedItemsIds = new SparseBooleanArray();
		notifyDataSetChanged();
	}

	public int getSelectedCount() {
		return mSelectedItemsIds.size();
	}

	public SparseBooleanArray getSelectedIds() {
		return mSelectedItemsIds;
	}

	public PeerAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
		mSelectedItemsIds = new SparseBooleanArray();
	}
	
	@Override
	public View newView(Context context, Cursor cur, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(ROW_LAYOUT, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView titleline = (TextView)view.findViewById(android.R.id.text1);
		TextView authorline = (TextView)view.findViewById(android.R.id.text2);
		
		titleline.setText(cursor.getString(cursor.getColumnIndex(PeerContract.PEER_NAME)));
		String message = cursor.getString(cursor.getColumnIndex(MessageContract.MESSAGE_MESSAGE));
		authorline.setText(message);
	
	}
}