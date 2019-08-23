package vikweck.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VikOuterAlarmReciever extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// set WakeLock
		Main.aquireWakeLock(context);
		
		// start main activity
		Intent startMainIntent = new Intent(context, Main.class);
		startMainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startMainIntent.setAction(Main.ACTION_RING_ALARM);
		context.startActivity(startMainIntent);
	}
}
