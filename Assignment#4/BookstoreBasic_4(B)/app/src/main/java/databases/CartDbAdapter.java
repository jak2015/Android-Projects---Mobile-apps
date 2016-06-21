package databases;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class CartDbAdapter extends ResourceCursorAdapter {

    protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_activated_2;

    public CartDbAdapter(Context context, Cursor c) {
        super(context,ROW_LAYOUT, c);
    }


    public View newView(Context context,Cursor c,ViewGroup parent)
    {
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(ROW_LAYOUT,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleline = (TextView) view.findViewById(android.R.id.text1);
        titleline.setText("Title : "+cursor.getString(cursor.getColumnIndexOrThrow("Title")));
        TextView authorline = (TextView) view.findViewById(android.R.id.text2);
        authorline.setText("Author : "+cursor.getString(cursor.getColumnIndexOrThrow("Authors")));
    }
}
