package vikweck.main;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class VikDialogFragmentSnoozeTime extends DialogFragment implements OnClickListener{
	
	private List<Integer> idToSnoozeMin;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		idToSnoozeMin = new ArrayList<Integer>();
		idToSnoozeMin.add(0, 1);
		idToSnoozeMin.add(1, 2);
		idToSnoozeMin.add(2, 3);
		idToSnoozeMin.add(3, 5);
		idToSnoozeMin.add(4, 7);
		idToSnoozeMin.add(5, 10);
		idToSnoozeMin.add(6, 15);
		idToSnoozeMin.add(7, 20);
		idToSnoozeMin.add(8, 30);
		
		Main mainActivity = (Main) this.getActivity();
		int currentSnoozeID = idToSnoozeMin.indexOf( mainActivity.getSnoozeInterval() );
		
		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
		
		builder.setTitle( R.string.title_dlg_snoozetime );
		builder.setSingleChoiceItems( R.array.array_snoozetimes, currentSnoozeID, this);
		
		return builder.create();
	}

	public void onClick(DialogInterface dialog, int which) {
		int snoozeTime = idToSnoozeMin.get(which);
		
		Main mainActivity = (Main) this.getActivity();
		mainActivity.submitNewSnoozeInterval(snoozeTime);
		this.dismiss();
	}
}
