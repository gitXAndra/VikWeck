package vikweck.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

public class VikDialogFragmentSoundChooser extends DialogFragment implements OnItemClickListener, OnClickListener {
	
	public interface SoundChangedNotificationListener {
		public void changeCurrentAlarmSound(Uri newUri);
	}
	
	private Dialog thisDialog;
	private SoundChangedNotificationListener mainActivity;
	private VikManagerSounds soundsManager;
	private VikCursorAdapterSounds soundsListadapter;
	private Uri currentlyChosenRingtoneUri;
	private static final int ALARM_PICKER_ID = 222;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		thisDialog = new Dialog(getActivity()) {
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				VikDialogFragmentSoundChooser.this.soundsManager.stopPlayback();
				return super.onTouchEvent(event);
			}
			@Override
			public void onBackPressed() {
				VikDialogFragmentSoundChooser.this.soundsManager.stopPlayback();
				super.onBackPressed();
			}
		};
		thisDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		thisDialog.setContentView(R.layout.dlg_soundchooser);
        
		View acceptButton = thisDialog.findViewById(R.id.dlg_sounds_accept);
		acceptButton.setOnClickListener(this);
		
		soundsManager = ((Main) getActivity()).getSoundsManager();
		currentlyChosenRingtoneUri = null;
		
		ListView listView = (ListView) thisDialog.findViewById(R.id.dlg_sounds_listview);
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View footerView = inflater.inflate(R.layout.ringtones_list_footer, null, false);
		listView.addFooterView(footerView);
		
		soundsListadapter = new VikCursorAdapterSounds(
				getActivity(),
				soundsManager.getCursor(),
				soundsManager.getCursorTitleColumnName(),
				0
				);
		
		listView.setAdapter(soundsListadapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setOnItemClickListener(this);
		
		int defaultId = soundsManager.getCurrentID();
		listView.setItemChecked( defaultId, true );
		soundsListadapter.updateCheckedItemID(defaultId);
		TextView displayCurrent = (TextView) thisDialog.findViewById(R.id.title_currently_chosen_ringtone);
		displayCurrent.setText( soundsManager.getCurrentTitle() );
		displayCurrent.setOnClickListener(this);
		
		currentlyChosenRingtoneUri = soundsManager.getCurrentUri();
		
		return thisDialog;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mainActivity = (SoundChangedNotificationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SoundChooserDialogFragment.SoundChangedNotificationListener");
        }
	}
	
	///is called when ringtone lsititem is clicked: playback, display and highlight currently playing
	public void onItemClick(AdapterView<?> listView, View clickedView, int clickedPositionInList, long clickedRowID) {
		
		ListView soundsList = (ListView) listView;
		
		if( clickedPositionInList == soundsList.getCount()-1 )
		{
			soundsManager.stopPlayback();
			Intent musicPickerIntent = new Intent( Intent.ACTION_GET_CONTENT );
			musicPickerIntent.setType("audio/*");
			
			startActivityForResult(musicPickerIntent, ALARM_PICKER_ID);
			return;
		}
		
		//change List appearance
		for( int i=0; i<soundsList.getChildCount()-1; i++ )//-1 footer will not be changed
		{
			View listChild = listView.getChildAt(i);
			int childPosition = soundsList.getPositionForView(listChild);
			
			WrapperListAdapter wla = (WrapperListAdapter) soundsList.getAdapter();
			VikCursorAdapterSounds adapter = (VikCursorAdapterSounds) wla.getWrappedAdapter() ;
			adapter.setAppropriateCheckedUncheckedAppearance( listChild, childPosition );
		}
		
		//update title display
		TextView displayCurrent = (TextView) thisDialog.findViewById(R.id.title_currently_chosen_ringtone);
		TextView clickedTextView = (TextView) clickedView.findViewById(R.id.list_view_nameview);
		displayCurrent.setText( clickedTextView.getText() );
		
		Cursor cursor = soundsManager.getCursor();
		cursor.moveToPosition(clickedPositionInList);
		Uri newlyChosenUri = ContentUris.withAppendedId(
				Uri.parse(cursor.getString(RingtoneManager.URI_COLUMN_INDEX)),
				cursor.getLong(RingtoneManager.ID_COLUMN_INDEX));
		
		if( currentlyChosenRingtoneUri != null && soundsManager.isPlaying() && newlyChosenUri.compareTo(currentlyChosenRingtoneUri) == 0)
		{
			soundsManager.stopPlayback();
			return;
		}
		else
		{
			currentlyChosenRingtoneUri = newlyChosenUri;
			soundsManager.play( currentlyChosenRingtoneUri, false );
		}
	}

	///Stop playback and accept dialog if click event came from accept button.
	public void onClick(View v) {
		
		if( !soundsManager.isPlaying() && v == this.thisDialog.findViewById(R.id.title_currently_chosen_ringtone))
		{
			soundsManager.play( currentlyChosenRingtoneUri, false );
			return;
		}
		
		soundsManager.stopPlayback();
		
		if( v == this.thisDialog.findViewById(R.id.dlg_sounds_accept) )
		{
			if( currentlyChosenRingtoneUri != null )
			{
				mainActivity.changeCurrentAlarmSound(currentlyChosenRingtoneUri);
			}
			this.dismiss();
		}
	}
	
	/** Handles the return values of a externally chosen alarm sound. */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if( requestCode != ALARM_PICKER_ID ||
				resultCode != Activity.RESULT_OK ||
				data == null )
		{
			return;
		}
		
		Uri pickedRingtoneUri = data.getData();
		currentlyChosenRingtoneUri = pickedRingtoneUri;
		
		TextView displayCurrent = (TextView) thisDialog.findViewById(R.id.title_currently_chosen_ringtone);
		String title = AudioFilesHelper.getAudioTitle( getActivity(), currentlyChosenRingtoneUri );
		displayCurrent.setText(title);
		
		soundsManager.play( currentlyChosenRingtoneUri, false );
	}
}
