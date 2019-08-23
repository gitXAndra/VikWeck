package vikweck.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.content.DialogInterface.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;

public class VikDialogAlarm extends Dialog implements OnTouchListener, OnKeyListener {
	Main main;

	public VikDialogAlarm(Context context) {
		super(context);
		main = (Main) context;
		getWindow().addFlags( 
				LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
				LayoutParams.FLAG_KEEP_SCREEN_ON |
				LayoutParams.FLAG_TURN_SCREEN_ON);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View wakeView = inflater.inflate(R.layout.wake_up_screen, null);
        wakeView.setOnTouchListener(this);
        setOnKeyListener(this);
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(wakeView);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		getWindow().setBackgroundDrawable(null);//default background results in small padding around the dialog
	}

	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if( event.getAction() == KeyEvent.ACTION_DOWN )
		{
			switch( keyCode )
			{
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				main.cancelAlarm();
				this.dismiss();
				return true;
			default:
				main.snooze();
				this.dismiss();
			}
		}
		return false;
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		main.snooze();
		this.dismiss();
		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(!hasFocus)
		{
			main.snooze();
			this.dismiss();
		}
		super.onWindowFocusChanged(hasFocus);
	}
}
