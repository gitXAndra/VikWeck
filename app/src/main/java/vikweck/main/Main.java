package vikweck.main;

import java.util.Calendar;

import vikweck.main.VikDialogFragmentSoundChooser.SoundChangedNotificationListener;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**Main Class: Manages visualization-, Data updates and alarm intents.*/
public class Main extends FragmentActivity 
	implements 
		VikViewMainGui.TouchNotificationListener, 
		VikDialogFragmentAlarmTime.AlarmTimeNotificationListener, 
		SoundChangedNotificationListener
{
	private static final int THIS_VIEW_ID = 555;
	public static final String ACTION_RING_ALARM="Main.FLAG_RING_ALARM";
	private static WakeLock wakelock;
	private VikData alarmData;
	private AlarmManager alarmManager;
	private PendingIntent alarmIntent;
	private VikManagerSounds soundsManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initAlarmData();
        initAlarmSystemService();
		setVolumeControlStream(AudioManager.STREAM_ALARM);// set volume control to alarm
		initUI();
		initSounds();
		
		if( getIntent().getAction() == ACTION_RING_ALARM )
    	{
    		ringAlarm();
    	}
		
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.layout_main, menu);
        return true;
    }
        
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        
        if( alarmData != null )
        {
	    	//is alarm set?
	        editor.putBoolean( "isAlarmSet", alarmData.isAlarmSet() );
	    	//alarm hour
	        editor.putInt( "alarmHour", alarmData.getAlarmHour() );
	    	//alarm minute
	        editor.putInt( "alarmMinute", alarmData.getAlarmMinute() );
	        //snooze time
	        editor.putInt( "snoozeMinutes", alarmData.getSnoozeMinutes() );
        }
        if( soundsManager.getCurrentUri() != null )
        {
	    	//current alarm sound
        	Uri uri = soundsManager.getCurrentUri();
        	if( AudioFilesHelper.doesFileExist(this, uri) && 
        			AudioFilesHelper.isContentUri(uri) )
        	{
        		Uri fileUri = AudioFilesHelper.getFileUri( this, uri );
        		editor.putString("currentAlarmSoundFileUri", fileUri.toString());
        	}
	        editor.putString( "currentAlarmSound", uri.toString() );
        }
        
        editor.commit();
    }

    @Override
    protected void onNewIntent (Intent intent) {
    	super.onNewIntent(intent);
    	
    	if( intent.getAction() == ACTION_RING_ALARM )
    	{
    		ringAlarm();
    	}
    }
    
    /***/
    private void initAlarmData () {
    	alarmData = new VikData();
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	int hour = settings.getInt("alarmHour", 0);
    	int min = settings.getInt("alarmMinute", 0);
    	alarmData.setAlarm(hour, min);
    	boolean isSet = settings.getBoolean("isAlarmSet", false);
    	alarmData.setAlarm(isSet);
    	int snoozeMinutes = settings.getInt("snoozeMinutes", 5);
    	alarmData.setSnoozeInterval(snoozeMinutes);
    }
    
    /**Initiate alarm system service.*/
    private void initAlarmSystemService () {
		alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, VikOuterAlarmReciever.class);
		alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
    }
    
    /**Initiate user interface.*/
    private void initUI () {
    	 RelativeLayout layout = new RelativeLayout(this);
         layout.setLayoutParams(new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ));
         layout.setBackgroundColor(getResources().getColor(android.R.color.black));
         VikViewMainGui mainView = new VikViewMainGui(this, alarmData);
         mainView.setId(THIS_VIEW_ID);
         layout.addView(mainView, new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ));
         layout.setId(333);
         
         setContentView(layout);
    }
    
    /**Initiate soundsManager and Custom Sounds.*/
    private void initSounds () {
    	soundsManager = new VikManagerSounds(this);
    	
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        try{
        	Uri alarmUri = Uri.parse( settings.getString("currentAlarmSound", null) );
        	if( AudioFilesHelper.doesFileExist(this, alarmUri) )
        	{
        		soundsManager.setCurrentAlarmSound( alarmUri );
        	}
        	else
        	{
        		alarmUri = Uri.parse( settings.getString("currentAlarmSoundFileUri", null) );
        		if( AudioFilesHelper.doesFileExist( this, alarmUri) )
        		{
        			soundsManager.setCurrentAlarmSound(alarmUri);
        		}
        		else
        		{
        			String txt = getString(R.string.update_file_location_warning);
        			Toast toast = Toast.makeText( this, txt, Toast.LENGTH_LONG);
        			toast.show();
        		}
        	}
        }catch(NullPointerException e) {
        	//do nothing. soundsManager will use default alarm sound
        }
    }
    
    /**Prevent device from falling asleep.*/
    public static void aquireWakeLock (Context context) {
    	PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
    	wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
    	        "VikWeckMainWakeLock");
    	wakelock.acquire();
    }
    
    /**Device may fall asleep if not otherwise prevented.*/
    public static void releaseWakeLock () {
    	if( wakelock != null )
    	{
    		wakelock.release();
    		wakelock = null;
    	}
    }
    
    ///
    public VikManagerSounds getSoundsManager () {
    	return soundsManager;
    }
    
    /**returns the current snooze time interval in minutes.*/
    public int getSnoozeInterval() {
    	return alarmData.getSnoozeMinutes();
    }
    
    /**Implements vikGuiView.TouchNotificationListener.onSetNewAlarm().*/
	public void onTouchSetNewAlarm() {
		if( getSupportFragmentManager().findFragmentByTag("alarmTimeDlg") != null )
		{// dlg is already visible
			return;
		}
		VikDialogFragmentAlarmTime alarmTimeDlg = VikDialogFragmentAlarmTime.newInstance( alarmData.getAlarmHour(), alarmData.getAlarmMinute() );
		
		alarmTimeDlg.show(getSupportFragmentManager(), "alarmTimeDlg");
	}

	/**Implements vikGuiView.TouchNotificationListener.onSwitchAlarm().*/
	public void onTouchAlarmSwitch() {
		soundsManager.stopPlayback();
		alarmData.switchAlarm();
		
		VikViewMainGui gui = (VikViewMainGui) this.findViewById(THIS_VIEW_ID);
		gui.updateAlarmData(alarmData);
		
		updateAlarmService();
	}

	/**Implements vikGuiView.TouchNotificationListener.onChangeAlarmtone().*/
	public void onTouchChangeAlarmsound() {
		if( getSupportFragmentManager().findFragmentByTag("soundDlg") != null )
		{// dlg is already visible
			return;
		}
		
		VikDialogFragmentSoundChooser dlg = new VikDialogFragmentSoundChooser();
		dlg.show( getSupportFragmentManager(), "soundDlg" );
	}

	/**Implements vikGuiView.TouchNotificationListener.onChangeVonlume().*/
	public void onTouchChangeVolume() {
		if( getSupportFragmentManager().findFragmentByTag("volumeDlg") != null )
		{// dlg is already visible
			return;
		}
		VikDialogFragmentVolume dlg = new VikDialogFragmentVolume();
		dlg.show( getSupportFragmentManager(), "volumeDlg" );
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ) {
		case R.id.menu_about:
			VikDialogFragmentInfo dlgInfo = new VikDialogFragmentInfo();
			dlgInfo.show( getSupportFragmentManager(), "infoDlg" );
			break;
		case R.id.menu_help:
			VikDialogFragmentHelp dlgHelp = new VikDialogFragmentHelp();
			dlgHelp.show( getSupportFragmentManager(), "helpDlg" );
			break;
		case R.id.menu_snooze:
			VikDialogFragmentSnoozeTime dlgSnooze = new VikDialogFragmentSnoozeTime();
			dlgSnooze.show( getSupportFragmentManager(), "snoozeDlg" );
			break;
		}
		
		return true;
	}

	/**Implements AlarmTimeDialogFragment.NotificationListener.onSubmitNewAlarmTime().*/
	public void submitNewAlarmTime( int hour, int minute ) {
		alarmData.setAlarm(hour, minute);
		
		VikViewMainGui gui = (VikViewMainGui) this.findViewById(THIS_VIEW_ID);
		gui.updateAlarmData(alarmData);
		
		updateAlarmService();
	}
	
	/**Cancel Alarm.*/
	public void cancelAlarm() {
		soundsManager.stopPlayback();
		alarmData.abortAlarm();
		
		updateAlarmService();
	}
	
	/**To change the snooze time interval. Value in minutes eg. 3 for 3 minutes.*/
	public void submitNewSnoozeInterval( int snoozeMinutes ) {
		alarmData.setSnoozeInterval(snoozeMinutes);
	}
    
	/**Cancels all pending alarm intents and starts a new one if alarm is activated.*/
	protected void updateAlarmService () {
		if (alarmManager == null) {
			return;
		}
		
		alarmManager.cancel(alarmIntent);
		
		if( alarmData.isAlarmSet() )
		{
			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmData.getAlarmTimeInMillis(), alarmIntent);
		}
	}
	
	/***/
	protected void ringAlarm () {
		final Dialog dlg = new VikDialogAlarm(this);
		dlg.show();
		
		this.soundsManager.playbackCurrent();
		
		Main.releaseWakeLock();
	}
	
	/**Stops alarm sound, cancels all pending alarm intents and starts a new one for the set snooze time.*/
	public void snooze () {
		soundsManager.stopPlayback();
		
		alarmManager.cancel(alarmIntent);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, alarmData.getSnoozeMinutes());
		alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmIntent);
	}
	
	/**Called to change the current alarm sound. For Example with the Result of SoundChooserDlg*/
	public void changeCurrentAlarmSound(Uri newUri) {
		soundsManager.setCurrentAlarmSound(newUri);
	}
}
