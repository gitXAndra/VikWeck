package vikweck.main;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class VikDialogFragmentAlarmTime extends DialogFragment implements OnTouchListener {
	
	public interface AlarmTimeNotificationListener {
		public void submitNewAlarmTime ( int hour, int minute );
	}
	
	protected class AnimatedAlarmText {
		protected TextView h0;
		protected TextView h1;
		protected TextView m0;
		protected TextView m1;
		Runnable textAnimation;
		boolean blink; //blinking status 
		
		public AnimatedAlarmText( TextView hour0, TextView hour1, TextView dots, TextView min0, TextView min1 ) {
			h0 = hour0;
			h1 = hour1;
			m0 = min0;
			m1 = min1;
			blink = false;
			
			// change alarmtime view typeface to century schoolbook
			Typeface typefaceCenturySchoolbook = Typeface.createFromAsset(getActivity().getAssets(), "fonts/century_schoolbook_bold_italic.ttf");
			h0.setTypeface(typefaceCenturySchoolbook);
			h1.setTypeface(typefaceCenturySchoolbook);
			dots.setTypeface(typefaceCenturySchoolbook);
			m0.setTypeface(typefaceCenturySchoolbook);
			m1.setTypeface(typefaceCenturySchoolbook);
			
			h0.setText( "" + alarmHourDigit0 );
			h1.setText( "" + alarmHourDigit1 );
			m0.setText( "" + alarmMinuteDigit0 );
			m1.setText( "" + alarmMinuteDigit1 );
			
			//animation
			final Handler animationHandler = new Handler();
			textAnimation = new Runnable() {
				public void run() {
					int hue_on = 170;//187, 153
					int hue_off = 187;
					
					h0.setTextColor(Color.rgb(hue_on, hue_on, hue_on));
					h1.setTextColor(Color.rgb(hue_on, hue_on, hue_on));
					m0.setTextColor(Color.rgb(hue_on, hue_on, hue_on));
					m1.setTextColor(Color.rgb(hue_on, hue_on, hue_on));
					
					animationHandler.postDelayed(this, 530);
					if( focus == -1 )
					{
						return;
					}
					
					if( blink )
					{
						if( focus < 2 )
						{	
							h1.setTextColor(Color.argb(48, hue_off, hue_off, hue_off));
							if( focus == 0 )
							{
								h0.setTextColor(Color.argb(48, hue_off, hue_off, hue_off));
							}
						}
						else
						{
							m1.setTextColor(Color.argb(48, hue_off, hue_off, hue_off));
							if( focus == 2 )
							{
								m0.setTextColor(Color.argb(48, hue_off, hue_off, hue_off));
							}
						}
					}
					blink = !blink;
				}
			};
			
			animationHandler.post(textAnimation);
		}
		
		public void setNumber (int nr) {
			switch (focus)
			{
			case 0:
				if( nr <= 2 )// hours to the twenties
				{
					alarmHourDigit0 = nr;
					h0.setText(""+nr);
					focus++;
					if( nr == 2 && alarmHourDigit1 > 3 )//if in the 20ies and over 23 change to 20 to prevent alarm hours like 28
					{
						alarmHourDigit1 = 0;
						h1.setText("0");
					}
				}
				break;
			case 1:
				if( alarmHourDigit0 == 2 && nr > 4 )//hours in the twenties only to 24:00
				{
					return;
				}
				alarmHourDigit1 = nr;
				h1.setText(""+nr);
				focus++;
				if( alarmHourDigit0 ==2 && alarmHourDigit1 ==4 )// there's no 24:01
				{
					alarmMinuteDigit0 = 0;
					m0.setText("0");
					alarmMinuteDigit1 = 0;
					m1.setText("0");
					focus = -1;
				}
				break;
			case 2:
				if( nr <= 5 ){
					alarmMinuteDigit0 = nr;
					m0.setText(""+nr);
					focus++;
				}
				break;
			case 3:
				alarmMinuteDigit1 = nr;
				m1.setText(""+nr);
				focus = -1;
				break;
			}
		}
	}
	
	int alarmHourDigit0;
	int alarmHourDigit1;
	int alarmMinuteDigit0;
	int alarmMinuteDigit1;
	AlarmTimeNotificationListener dlgNotificationImpl;
	int focus;// -1: no fucus, 0: first hour digit, 1: second hour digit, 2: first min digit, 3: second min digit 
	AnimatedAlarmText text;
	
	public static VikDialogFragmentAlarmTime newInstance ( int alarmHour, int alarmMinute ) {
		VikDialogFragmentAlarmTime newDlgFragment = new VikDialogFragmentAlarmTime();
		
	    Bundle args = new Bundle();
	    args.putInt("alarmHour", alarmHour);
	    args.putInt("alarmMinute", alarmMinute);
	    newDlgFragment.setArguments(args);
	    
	    return newDlgFragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int alarmHour = getArguments().getInt("alarmHour");
		int alarmMinute = getArguments().getInt("alarmMinute");
		alarmHourDigit0 = Math.round( alarmHour/10 );
		alarmHourDigit1 = alarmHour % 10;
		alarmMinuteDigit0 = Math.round( alarmMinute/10 );
		alarmMinuteDigit1 = alarmMinute % 10;
		
		focus = 0; //focus on hours
		
		// create alert dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dlg_alarmtime, null);
		builder.setView(view);
		
		text = new AnimatedAlarmText(
				((TextView) view.findViewById(R.id.dlg_alarmtime_input_alarm_hour0)),
				((TextView) view.findViewById(R.id.dlg_alarmtime_input_alarm_hour1)),
				((TextView) view.findViewById(R.id.dlg_alarmtime_dots)),
				((TextView) view.findViewById(R.id.dlg_alarmtime_input_alarm_minute0)),
				((TextView) view.findViewById(R.id.dlg_alarmtime_input_alarm_minute1))
				);
		
		//touch listeners
		(view.findViewById(R.id.dlg_alarmtime_input_alarm_hour0)).setOnTouchListener(this);
		(view.findViewById(R.id.dlg_alarmtime_input_alarm_hour1)).setOnTouchListener(this);
		(view.findViewById(R.id.dlg_alarmtime_input_alarm_minute0)).setOnTouchListener(this);
		(view.findViewById(R.id.dlg_alarmtime_input_alarm_minute1)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_accept)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_0)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_1)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_2)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_3)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_4)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_5)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_6)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_7)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_8)).setOnTouchListener(this);
		(view.findViewById(R.id.alarm_dlg_nr_9)).setOnTouchListener(this);
		
		return builder.create();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			dlgNotificationImpl = (AlarmTimeNotificationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AlarmTimeDialogFragment.NotificationListener");
        }
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		
		//react only to touch downs
		if( event.getAction() != MotionEvent.ACTION_DOWN )
		{
			return false;
		}
		
		switch (v.getId()) 
		{
		case R.id.dlg_alarmtime_input_alarm_hour0:
		case R.id.dlg_alarmtime_input_alarm_hour1:
			focus = 0;
			break;
		case R.id.dlg_alarmtime_input_alarm_minute0:
		case R.id.dlg_alarmtime_input_alarm_minute1:
			focus = 2;
			break;
		case R.id.alarm_dlg_accept:
			
			dlgNotificationImpl.submitNewAlarmTime((10*alarmHourDigit0+alarmHourDigit1), (10*alarmMinuteDigit0+alarmMinuteDigit1));
			getDialog().dismiss();
			break;
		case R.id.alarm_dlg_nr_0:
			text.setNumber(0);
			break;
		case R.id.alarm_dlg_nr_1:
			text.setNumber(1);
			break;
		case R.id.alarm_dlg_nr_2:
			text.setNumber(2);
			break;
		case R.id.alarm_dlg_nr_3:
			text.setNumber(3);
			break;
		case R.id.alarm_dlg_nr_4:
			text.setNumber(4);
			break;
		case R.id.alarm_dlg_nr_5:
			text.setNumber(5);
			break;
		case R.id.alarm_dlg_nr_6:
			text.setNumber(6);
			break;
		case R.id.alarm_dlg_nr_7:
			text.setNumber(7);
			break;
		case R.id.alarm_dlg_nr_8:
			text.setNumber(8);
			break;
		case R.id.alarm_dlg_nr_9:
			text.setNumber(9);
			break;
		}
		
		return true;
	}
	
}
