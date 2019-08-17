package vikweck.main;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;

/**
 * 
 */
public class VikManagerSounds implements OnCompletionListener {
	private Context context;
	private RingtoneManager ringManager;
	final AudioManager audioManager;
	private MediaPlayer player;
	private Vibrator vibrator;
	
	public VikManagerSounds(Context activityContext) {
		
		context = activityContext;
		
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		
		Uri uri = RingtoneManager.getValidRingtoneUri( context );
		setCurrentAlarmSound( uri );
		
		vibrator = (Vibrator) activityContext.getSystemService(Context.VIBRATOR_SERVICE);
		
		ringManager = new RingtoneManager(context);
		ringManager.setType(RingtoneManager.TYPE_ALARM);
	}
	
	public String getCursorTitleColumnName () {
		return ringManager.getCursor().getColumnName(RingtoneManager.TITLE_COLUMN_INDEX);
	}
	
	public Cursor getCursor () {
		return ringManager.getCursor();
	}
	
	public int getCurrentID () {
		Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
		return ringManager.getRingtonePosition( uri );
	}
	
	public String getCurrentTitle () {
		Uri uri = RingtoneManager.getActualDefaultRingtoneUri( context, RingtoneManager.TYPE_ALARM );
		if( AudioFilesHelper.doesFileExist( context, uri ) )
		{
			return AudioFilesHelper.getAudioTitle( context, uri );
		}
		else
		{
			return context.getString( R.string.ringtone_not_found );
		}
		
	}
	
	public Uri getCurrentUri () {
		return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
	}
	
	public void setCurrentAlarmSound (Uri uri) {
		RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, uri);
	}
	
	public void play (Uri uri, boolean loop) {
		stopPlayback();

		if( !AudioFilesHelper.doesFileExist(context, uri) )
		{
			uri = RingtoneManager.getValidRingtoneUri( context );
		}
		
		try {
			int audioRequestAnswer = audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN);
			if( audioRequestAnswer != AudioManager.AUDIOFOCUS_REQUEST_GRANTED )
			{
				return;
			}
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				audioManager.setStreamSolo(AudioManager.STREAM_ALARM, true);
				
				player = new MediaPlayer();
				
				player.setAudioStreamType(AudioManager.STREAM_ALARM);
				player.setLooping(loop);
				player.setDataSource(context, uri);
				if( !loop )
				{
					player.setOnCompletionListener(this);
				}
				player.prepare();
				player.start();
				
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//vibrate: either an exeption occured with media player or volume is 0
		long[] pattern = { 0, 400, 2000 };
		vibrator.vibrate(pattern, 0);
	}
	
	public void playbackCurrent () {
		Uri alarmUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
		play(alarmUri, true);
	}
	
	public boolean isPlaying () {
		if( player != null )
		{
			return player.isPlaying();
		}
		else
		{
			return false;
		}
	}
	
	public void stopPlayback () {
		if( player!= null && player.isPlaying() )
		{
			try
			{
				player.stop();
				player.reset();
				player.release();
				player = null;
				audioManager.setStreamSolo(AudioManager.STREAM_ALARM, false);
			} catch( IllegalStateException e )
			{
				e.printStackTrace();
			}
		}
		
		if( vibrator != null )
		{
			vibrator.cancel();
		}
		audioManager.abandonAudioFocus(null);
	}
	
	/*Abandon audio focus on completion. Set only when not looping playback.*/
	public void onCompletion(MediaPlayer mp) {
		audioManager.setStreamSolo(AudioManager.STREAM_ALARM, false);
		audioManager.abandonAudioFocus(null);
	}
	
	public String toString() {
		String soundsTxt = "vikSounds class content: ";
		
		Uri defaultUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
		Ringtone ringtone = RingtoneManager.getRingtone(context, defaultUri);
		String defaultTitle = ringtone.getTitle(context);
		int defaultId = ringManager.getRingtonePosition( defaultUri );
		soundsTxt += "\n- default: "+defaultTitle+" -> "+defaultUri+"; id="+defaultId;
		
		Cursor ringtoneManagerCursor = ringManager.getCursor();
		soundsTxt += "\n- nr. ringtones = "+ringtoneManagerCursor.getCount();
		soundsTxt += "\n- ringtones (title, uri, id):";
		for( int i=0; i<ringtoneManagerCursor.getCount(); i++ )
		{
			ringtoneManagerCursor.moveToPosition(i);
			soundsTxt += "\n	- "+ringtoneManagerCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			soundsTxt += ", "+ringtoneManagerCursor.getString(RingtoneManager.URI_COLUMN_INDEX);
			soundsTxt += ", "+ringtoneManagerCursor.getInt(RingtoneManager.ID_COLUMN_INDEX);
		}

		return soundsTxt;
	}
	
}

