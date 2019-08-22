package vikweck.main;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class VikCursorAdapterSounds extends CursorAdapter {
	private LayoutInflater inflater;
	private String titleColumn;
	private int checkedItemID;
	private int textSize = 25;
	private int textColor = Color.parseColor("#DDDDDD");
	private int textSelectedSize = 32;
	private int textSelectedColor = Color.parseColor("#FFFFFF");
	
	public VikCursorAdapterSounds(Context context, Cursor cursor, String titleColumnIndex, int flags) {
		super(context, cursor, flags);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		titleColumn = titleColumnIndex;
		checkedItemID = -1;
	}
	
	@Override
	public void bindView(View rowView, Context context, Cursor cursor) {
		setAppropriateCheckedUncheckedAppearance( rowView, cursor.getPosition() );
		TextView nameView = (TextView) rowView.findViewById(R.id.list_view_nameview);
		nameView.setText(cursor.getString(cursor.getColumnIndex(titleColumn)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.ringtones_list_row_view, parent, false);
	}

	public void setAppropriateCheckedUncheckedAppearance (View rowView, int viewPositionInList) {
		
		if( rowView.getParent() != null )
		{
			ListView soundsList = (ListView) rowView.getParent();
			updateCheckedItemID( soundsList.getCheckedItemPosition() );
		}
		
		TextView listItem = (TextView) rowView.findViewById(R.id.list_view_nameview);
		
		if( viewPositionInList == checkedItemID )
		{
			listItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSelectedSize);
			listItem.setTextColor(textSelectedColor);
		}
		else
		{
			listItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
			listItem.setTextColor(textColor);	
		}
	}
	
	public void updateCheckedItemID (int id)
	{
		checkedItemID = id;
	}
}
