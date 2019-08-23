package vikweck.main;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

public class VikData implements Parcelable {
	private boolean alarmSet;
	private int alarmHour;
	private int alarmMinute;
	private int snoozeMinutes;
	
	public VikData() {
		
		alarmSet = false;
		alarmHour = 0;
		alarmMinute = 0;
		snoozeMinutes = 3;
	}
	
	
	public VikData (Parcel p) {
		alarmSet = p.readByte() != 0;     //alarmSet == true if byte != 0
		alarmHour = p.readInt();
		alarmMinute = p.readInt();
	}
	
	public int getAlarmHour () {
		return alarmHour;
	}
	
	public int getAlarmMinute () {
		return alarmMinute;
	}
	
	public long getAlarmTimeInMillis () {
		Calendar c = Calendar.getInstance();
		long currentTime = c.getTimeInMillis();
		c.set( Calendar.HOUR_OF_DAY, alarmHour );
		c.set( Calendar.MINUTE, alarmMinute );
		c.set( Calendar.SECOND, 0);
		long alarmTime = c.getTimeInMillis();
		
		if( (alarmTime - currentTime) <0 )//alarmTime is for the next day
		{
			c.roll( Calendar.DAY_OF_YEAR, 1);
		}
		
		return c.getTimeInMillis();
	}
	
	public int getSnoozeMinutes () {
		return snoozeMinutes;
	}
	
	public boolean isAlarmSet () {
		return alarmSet;
	}
	
	public void setAlarm () {
		setAlarm(true);
	}
	
	///Sets the alarm time.
	public void setAlarm (int hour, int minute) {
		alarmHour = hour;
		alarmMinute = minute;
		setAlarm();
	}
	
	public void setAlarm (boolean set) {
		alarmSet = set;
	}
	
	/**Set the snooze time interval. Value in minutes eg. 3 for 3 minutes.*/
	public void setSnoozeInterval(int snoozeInterval) {
		snoozeMinutes = snoozeInterval;
	}
	
	public void abortAlarm () {
		setAlarm(false);
	}
	
	public void switchAlarm () {
		alarmSet = !alarmSet;
		if( alarmSet )
		{
			setAlarm();
		}
		else
		{
			abortAlarm();
		}
	}
	
	///Parcelable implementation: is ignored.
	public int describeContents() {
		return 0;
	}

	///Parcelable implementation: writer.
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (alarmSet ? 1 : 0));//if alarmSet == true, byte == 1
		dest.writeInt(alarmHour);
		dest.writeInt(alarmMinute);
	}
	
	public static final Parcelable.Creator<VikData> CREATOR = new Parcelable.Creator<VikData>() {

		public VikData createFromParcel(Parcel source) {
			return new VikData(source);
		}

		public VikData[] newArray(int size) {
			return new VikData[size];
		}
	};
}
