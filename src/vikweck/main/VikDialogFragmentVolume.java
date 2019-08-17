package vikweck.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VikDialogFragmentVolume extends DialogFragment 
	implements OnSeekBarChangeListener, OnTouchListener, OnKeyListener {
	View dlgView;
	LightsDisplay lights;
	
	protected class LightsDisplay {
		Light[] lights;
		SeekBar slider;
		
		private class Light {
			DynamicImageView im;
			
			public Light(Context context) {
				im = new DynamicImageView(context, DynamicImageView.FIXED_HEIGHT);
				
				final float densityScale = getResources().getDisplayMetrics().density;
				int width_dp = 30;
				int width_px = Math.round( densityScale * width_dp );
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, width_px, 1.0f);
				im.setLayoutParams(lp);
				im.setImageResource(R.drawable.slider_licht_aus);
			}
			
			public View getView() {
				return im;
			}
			
			public void setLightOn() {
				im.setImageResource(R.drawable.slider_licht_an);
			}
			
			public void setLightOff() {
				im.setImageResource(R.drawable.slider_licht_aus);
			}
		}
		
		public LightsDisplay(Context context, int maxVolume) {
			LinearLayout lightsView = (LinearLayout) dlgView.findViewById(R.id.vol_linlayout_lights);
			slider = (SeekBar) dlgView.findViewById(R.id.seekbar_volume);
			if(maxVolume>10)
			{
				maxVolume = 10;
			}
			
			lights = new Light[maxVolume];
			for( int i=0; i<maxVolume; i++ )
			{
				lights[i] = new Light(context);
				lightsView.addView( lights[i].getView() );
			}
			
			update();
		}
		
		public void update()
		{
			double sliderCurrent = slider.getProgress();
			double sliderMax = slider.getMax();
			double sliderRelPosition = sliderCurrent / sliderMax;
			
			for( int i=0; i<lights.length; i++ )
			{
				if( ((double)i/(double)lights.length) < sliderRelPosition )
				{
					lights[i].setLightOn();
				}
				else
				{
					lights[i].setLightOff();
				}
			}
		}
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// create alert dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dlgView = inflater.inflate(R.layout.dlg_volume, null);
		builder.setView(dlgView);
		
		AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM);
		int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		
		lights = new LightsDisplay( getActivity(), maxVolume );
		
		SeekBar volBar = (SeekBar) dlgView.findViewById(R.id.seekbar_volume);
		volBar.setMax(maxVolume);
		volBar.setProgress( currentVolume );
		volBar.setOnSeekBarChangeListener(this);
		
		lights.update();
		
		View icon = dlgView.findViewById(R.id.vol_ico);
		icon.setOnTouchListener(this);
		View buttonAccept = dlgView.findViewById(R.id.dlg_volume_accept);
		buttonAccept.setOnTouchListener(this);
		
		builder.setOnKeyListener(this);
		
		return builder.create();
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		audio.setStreamVolume(AudioManager.STREAM_ALARM, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		lights.update();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {}

	public void onStopTrackingTouch(SeekBar seekBar) {}

	public boolean onTouch(View v, MotionEvent event) {
		
		if( v == dlgView.findViewById(R.id.vol_ico) )
		{
			playSound();
		}
		else if (v == dlgView.findViewById(R.id.dlg_volume_accept) )
		{

			this.dismiss();
			return true;
		}
		return false;
	}
	
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		
		if( event.getAction() != KeyEvent.ACTION_DOWN )
		{
			return false;
		}
		
		AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		switch( keyCode )
		{
		case KeyEvent.KEYCODE_VOLUME_UP:
			audio.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			update();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audio.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			update();
			return true;
		default:
			return false;
		}
	}
	
	protected void playSound() {
		try {
		    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    Ringtone r = RingtoneManager.getRingtone( getActivity(), notification);
		    r.setStreamType(AudioManager.STREAM_ALARM);
		    r.play();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	protected void update() {
		AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM);
		SeekBar volBar = (SeekBar) dlgView.findViewById(R.id.seekbar_volume);
		volBar.setProgress( currentVolume );
		
		lights.update();
	}
}
