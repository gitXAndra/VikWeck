package vikweck.main;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class VikViewMainGui extends View implements OnTouchListener {
	
	public interface TouchNotificationListener {
		public void onTouchSetNewAlarm ();
		public void onTouchAlarmSwitch ();
		public void onTouchChangeAlarmsound ();
		public void onTouchChangeVolume ();
	}
	
	protected class Background {
		private Bitmap backgroundBmp;
		private Rect backgroundVisible;
		private Paint borderPaint;
		private float backgroundBmp2ViewportScaleFactor;
		
		public Background(Resources res, int id) {
			backgroundBmp = BitmapFactory.decodeResource(res,id);
			backgroundVisible = new Rect();
			borderPaint = new Paint();
			borderPaint.setStyle(Style.STROKE);
			borderPaint.setStrokeWidth(1);
		}
		
		public void onCanvasSizeChanged(Canvas canvas) {
			//calculate the rect of visible background
			//-> for different devices different width to height ratios mean slight differences in the width of the border around the designed components.
			double imageAspectRatio = (double) (backgroundBmp.getHeight()) / (double) (backgroundBmp.getWidth());
			double viewportAspectRatio = (double) (currentViewport.height()) / (double) (currentViewport.width());
			
			if( imageAspectRatio > viewportAspectRatio )
			{
				double backgroundVisibleWidth = backgroundBmp.getHeight() * 0.84 / viewportAspectRatio;
				backgroundVisible.left = (int) ( (backgroundBmp.getHeight() - backgroundVisibleWidth) / 2 );
				backgroundVisible.top = (int) (backgroundBmp.getHeight() * 0.08);
				backgroundVisible.right = backgroundVisible.left + (int) (backgroundVisibleWidth);
				backgroundVisible.bottom = backgroundVisible.top + (int) (backgroundBmp.getHeight() * 0.84);
			}
			else
			{
				double backgroundVisibleHeight = backgroundBmp.getWidth() * 0.84 * viewportAspectRatio;
				backgroundVisible.left = (int) (backgroundBmp.getWidth() * 0.08);
				backgroundVisible.top = (int) ( (backgroundBmp.getHeight() - backgroundVisibleHeight) / 2 );
				backgroundVisible.right = backgroundVisible.left + (int) (backgroundBmp.getWidth() * 0.84);
				backgroundVisible.bottom = backgroundVisible.top + (int) (backgroundVisibleHeight);
			}
			
			//ratio between image and viewport size
			backgroundBmp2ViewportScaleFactor = (float) (currentViewport.height()) / (float) (backgroundVisible.bottom - backgroundVisible.top);
		}
		
		public void draw(Canvas canvas) {
			//draw background
			canvas.drawBitmap(backgroundBmp, backgroundVisible, currentViewport, null);
			
			//draw border
			int borderWidthInPx = 30;
			float borderFadeOutGradient = 255.0f / borderWidthInPx;
			for( int i=0; i<borderWidthInPx; i++)
			{
				borderPaint.setAlpha((int) ( 255 - i*borderFadeOutGradient ));
				canvas.drawRect(currentViewport.left+i, currentViewport.top+i, currentViewport.right-i, currentViewport.bottom-i, borderPaint);
			}
		}
		
		public PointF relativeImgCoords2viewportCoords(PointF relPt) {
			PointF returnPt = new PointF();
			
			//determine absolute coordinates in background bitmap
			returnPt.x = relPt.x * backgroundBmp.getWidth();
			returnPt.y = relPt.y * backgroundBmp.getHeight();
			
			//move point of origin to top-left corner of visible background
			returnPt.x = returnPt.x - backgroundVisible.left;
			returnPt.y = returnPt.y - backgroundVisible.top;
			
			//scale coordinates according to viewport size
			returnPt.x = backgroundBmp2ViewportScaleFactor * returnPt.x;
			returnPt.y = backgroundBmp2ViewportScaleFactor * returnPt.y;
			
			return returnPt;
		}
		
		public float getBckgrBmp2ViewportScaleFactor() {
			return backgroundBmp2ViewportScaleFactor;
		}
	}
	
	protected class Clock {
		
		private class Hand {
			private Bitmap bmp;
			private Matrix transformation;
			
			public Hand( Resources res, int id ) {
				bmp = BitmapFactory.decodeResource(res,id);
				transformation = new Matrix();
			}
			
			public void draw( Canvas canvas, float angle ) {
				int handBmpCenter = (int)(bmp.getWidth()/2);
				transformation.setTranslate(-handBmpCenter, -handBmpCenter);
				transformation.postRotate(angle);
				transformation.postConcat(matScaleAndTranslToClockCenterOnScreen);
				canvas.drawBitmap(bmp, transformation, null);
			}
		}
		
		private Hand littleHand;
		private Hand bigHand;
		private Hand alarmHand;
		private PointF clockCenterOnScreen;
		private Matrix matScaleAndTranslToClockCenterOnScreen;
		private float alarmAngle;
		
		public Clock(Resources res, int littleHandID, int bigHandID, int alarmID) {
			clockCenterOnScreen = new PointF();
			matScaleAndTranslToClockCenterOnScreen = new Matrix();
			littleHand = new Hand(res, littleHandID);
			bigHand = new Hand(res, bigHandID);
			alarmHand = new Hand(res, alarmID);
			updateAlarmHand();
		}

		public void onCanvasSizeChanged(Canvas canvas) {
			clockCenterOnScreen = backgroundImpl.relativeImgCoords2viewportCoords(new PointF(0.404375f, 0.332988f));
			
			//transformation help matrix
			matScaleAndTranslToClockCenterOnScreen.setScale(backgroundImpl.getBckgrBmp2ViewportScaleFactor(), backgroundImpl.getBckgrBmp2ViewportScaleFactor());
			matScaleAndTranslToClockCenterOnScreen.postTranslate(clockCenterOnScreen.x, clockCenterOnScreen.y);
		}
		
		public void updateAlarmHand () {
			alarmAngle = 180 + (alarmData.getAlarmHour() * 30) + (alarmData.getAlarmMinute() * 0.5f);//180->images point down, *6 -> 6degrees per minute
		}

		public void draw(Canvas canvas) {
			Calendar time = Calendar.getInstance();
			int hours = time.get(Calendar.HOUR);
			int minutes = time.get(Calendar.MINUTE);
			int seconds = time.get(Calendar.SECOND);
			
			float littleAngle = 180 + (hours * 30) + (minutes * 0.5f);//180->images point down, *6 -> 6degrees per minute
			float bigAngle = 180 + (minutes * 6) + (seconds * 0.1f); //180->images point down, *30 -> 30degrees per hour, 
						
			littleHand.draw(canvas, littleAngle);
			bigHand.draw(canvas, bigAngle);
			alarmHand.draw(canvas, alarmAngle);
			
			defaultPaint.setAlpha(255);
			defaultPaint.setStyle(Style.FILL);
			int radius = (int) (0.015*canvas.getWidth());
			canvas.drawCircle(clockCenterOnScreen.x, clockCenterOnScreen.y, radius, defaultPaint);
		}
	}
	
	protected class Switch {
		private Matrix matON;
		private Matrix matOFF;
		private Bitmap bmp;
		private PointF centerOnScreen;
		
		public Switch (Resources res) {
			matON = new Matrix();
			matOFF = new Matrix();
			bmp = BitmapFactory.decodeResource(res, R.drawable.hebel);
		}
		
		public void onCanvasSizeChanged (Canvas canvas) {
			float scale = 0.9f * backgroundImpl.getBckgrBmp2ViewportScaleFactor();
			centerOnScreen =  backgroundImpl.relativeImgCoords2viewportCoords(new PointF(0.80663373f, 0.23f));
			
			matON.setScale(scale, scale);
			float left = centerOnScreen.x - scale * ( bmp.getWidth() / 2 );
			float top = centerOnScreen.y - scale * ( bmp.getWidth() / 3 );
			matON.postTranslate( left, top );
			
			top += scale * ( 2 * bmp.getWidth() / 3);
			matOFF.setScale(scale, -scale);
			matOFF.postTranslate( left, top);
			
		}
		
		public void draw (Canvas canvas) {
			if( alarmData.isAlarmSet() )
			{
				canvas.drawBitmap( bmp, matON, null);
			}
			else
			{
				canvas.drawBitmap(bmp, matOFF, null);
			}
		}
	}
	
	protected class AlarmDisplay {
		private Bitmap digitBmps[][];//[0:off, 1:on][0-9,:,-,h,m,am,pm]
		private PointF dispCenterOnScreen;
		private AlarmTime alarmTimeDisplay;
		private TimeLeft timeLeftDisplay;
		
		///Stores number, size and position of a digit.
		private class Digit {
			private Matrix mat;
			private int bitmapID;
			private float scale;
			
			public Digit (int bmpID, float digitScale) {
				bitmapID = bmpID;
				scale = digitScale;
				mat = new Matrix();
			}
			
			public float getWidth () {
				return digitBmps[0][bitmapID].getWidth() * scale;
			}
			
			public float getHeight() {
				return digitBmps[0][bitmapID].getHeight() * scale;
			}
			
			public void setBitmapID (int id) {
				bitmapID = id;
			}
			
			public void setTopLeftPosition (PointF tl) {
				mat.setScale(backgroundImpl.getBckgrBmp2ViewportScaleFactor()*scale, backgroundImpl.getBckgrBmp2ViewportScaleFactor()*scale);
				mat.postTranslate(tl.x, tl.y);
			}
			
			public void draw (Canvas canvas) {
				if( alarmData.isAlarmSet() )
				{
					canvas.drawBitmap(digitBmps[1][bitmapID], mat, null);
				}
				else
				{
					canvas.drawBitmap(digitBmps[0][bitmapID], mat, null);
				}
				
			}
		}
		
		private class AlarmTime {
			private Digit digits[];
			
			public AlarmTime () {
				digits = new Digit[5];
				digits[0] = new Digit( 0, 1.0f );
				digits[1] = new Digit( 0, 1.0f );
				digits[2] = new Digit( 10, 1.0f );
				digits[3] = new Digit( 0, 1.0f );
				digits[4] = new Digit( 0, 1.0f );
			}
			
			public void update () {
				//update digit IDs
				int alarmHour = alarmData.getAlarmHour();
				int alarmMinute = alarmData.getAlarmMinute();
				
				int d0 =(int) (Math.floor( alarmHour/10 ));
				digits[0].setBitmapID(d0);
				
				int d1 = alarmHour % 10;
				digits[1].setBitmapID(d1);
				
				int d2 = (int) (Math.floor( alarmMinute/10 ));
				digits[3].setBitmapID(d2);
				
				int d3 = alarmMinute % 10;
				digits[4].setBitmapID(d3);
				
				//update digit position and size
				float height = backgroundImpl.getBckgrBmp2ViewportScaleFactor() * digits[0].getHeight() * 0.8f;
				float width_AlarmTime = 0;
				for( int i=0; i<5; i++ )
				{
					width_AlarmTime += digits[i].getWidth();
				}
				width_AlarmTime = backgroundImpl.getBckgrBmp2ViewportScaleFactor() * width_AlarmTime * 0.7f;
				float center_AlarmTime = width_AlarmTime/2;
				float distTLAlarmTimeDisp = 0;
				for( int i=0; i<5; i++ )
				{
					PointF tl = new PointF( ( dispCenterOnScreen.x - center_AlarmTime + distTLAlarmTimeDisp ), (dispCenterOnScreen.y - height ) );
					digits[i].setTopLeftPosition(tl);
					distTLAlarmTimeDisp += digits[i].getWidth() * backgroundImpl.getBckgrBmp2ViewportScaleFactor() * 0.7f;
				}
			}
		
			public void draw(Canvas canvas) {
				for( int i=0; i<5; i++ )
				{
					digits[i].draw(canvas);
				}
			}
		}
		
		private class TimeLeft {
			private Digit digits[];
			
			public TimeLeft () {
				digits = new Digit[7];
				float scale = 0.6f;
				digits[0] = new Digit( 11, scale );
				digits[1] = new Digit( 0, scale );
				digits[2] = new Digit( 0, scale );
				digits[3] = new Digit( 12, scale );
				digits[4] = new Digit( 0, scale );
				digits[5] = new Digit( 0, scale );
				digits[6] = new Digit( 13, scale );
			}
			
			public void update () {
				//update digit IDs
				int id0 = 0;
				int id1 = 0;
				int id2 = 0;
				int id3 = 0;
				
				if( alarmData.isAlarmSet() )
				{
					Calendar c = Calendar.getInstance();
					c.set( Calendar.SECOND, 0 );
					long currentTime = c.getTimeInMillis();
					long alarmTime = alarmData.getAlarmTimeInMillis();
					
					long diff = alarmTime - currentTime;
					int minutes = (int) ( diff / (1000*60) );
					int hours = (int) Math.floor( minutes / 60 );
					minutes = minutes - ( hours*60 );
					
					id0 =(int) (Math.floor( hours/10 ));
					id1 = hours % 10;
					id2 = (int) (Math.floor( minutes/10 ));
					id3 = minutes % 10;
				}
				
				digits[1].setBitmapID(id0);
				digits[2].setBitmapID(id1);
				digits[4].setBitmapID(id2);
				digits[5].setBitmapID(id3);
				
				//update digit size and position
				float height_AlarmTime = backgroundImpl.getBckgrBmp2ViewportScaleFactor() * digits[0].getHeight() * 0.25f;
				float width_AlarmTime = 0;
				for( int i=0; i<7; i++ )
				{
					width_AlarmTime += digits[i].getWidth();
				}
				width_AlarmTime = backgroundImpl.getBckgrBmp2ViewportScaleFactor() * width_AlarmTime * 0.7f;
				float center_AlarmTime = width_AlarmTime/2;
				float distTLAlarmTimeDisp = 0;
				for( int i=0; i<7; i++ )
				{
					PointF tl = new PointF( ( dispCenterOnScreen.x - center_AlarmTime + distTLAlarmTimeDisp ), dispCenterOnScreen.y + height_AlarmTime );
					digits[i].setTopLeftPosition(tl);
					distTLAlarmTimeDisp += digits[i].getWidth() * backgroundImpl.getBckgrBmp2ViewportScaleFactor() * 0.7f;
				}				
			}
			
			public void draw(Canvas canvas) {
				update();
				for( int i=0; i<7; i++ )
				{
					digits[i].draw(canvas);
				}
			}
		}
		
		public AlarmDisplay (Resources res) {
			alarmTimeDisplay = new AlarmTime();
			timeLeftDisplay = new TimeLeft();
			digitBmps = new Bitmap[4][16];
			digitBmps[0][0] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_0);
			digitBmps[0][1] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_1);
			digitBmps[0][2] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_2);
			digitBmps[0][3] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_3);
			digitBmps[0][4] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_4);
			digitBmps[0][5] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_5);
			digitBmps[0][6] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_6);
			digitBmps[0][7] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_7);
			digitBmps[0][8] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_8);
			digitBmps[0][9] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_9);
			digitBmps[0][10] = BitmapFactory.decodeResource(res,R.drawable.alarm_off_dots);
			digitBmps[0][11] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_minus);
			digitBmps[0][12] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_h);
			digitBmps[0][13] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_m);
			digitBmps[0][14] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_am);
			digitBmps[0][15] = BitmapFactory.decodeResource(res, R.drawable.alarm_off_pm);
			
			digitBmps[1][0] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_0);
			digitBmps[1][1] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_1);
			digitBmps[1][2] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_2);
			digitBmps[1][3] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_3);
			digitBmps[1][4] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_4);
			digitBmps[1][5] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_5);
			digitBmps[1][6] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_6);
			digitBmps[1][7] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_7);
			digitBmps[1][8] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_8);
			digitBmps[1][9] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_9);
			digitBmps[1][10] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_dots);
			digitBmps[1][11] = BitmapFactory.decodeResource(res,  R.drawable.alarm_on_minus);
			digitBmps[1][12] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_h);
			digitBmps[1][13] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_m);
			digitBmps[1][14] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_am);
			digitBmps[1][15] = BitmapFactory.decodeResource(res, R.drawable.alarm_on_pm);
		}
		
		public void update () {
			alarmTimeDisplay.update();
			timeLeftDisplay.update();
		}
		
 		public void onCanvasSizeChanged (Canvas canvas) {
			dispCenterOnScreen = backgroundImpl.relativeImgCoords2viewportCoords(new PointF(0.68f, 0.6442257f));
			
			alarmTimeDisplay.update();
			timeLeftDisplay.update();
		}
		
		public void draw (Canvas canvas) {
			alarmTimeDisplay.draw(canvas);
			timeLeftDisplay.draw(canvas);
		}
	}
	
	protected VikData alarmData;
	protected Rect currentViewport;
	protected Paint defaultPaint;
	protected Background backgroundImpl;
	protected Clock clockImpl;
	protected Switch switchImpl;
	protected AlarmDisplay alarmDisplayImpl;
	protected TouchNotificationListener notifyTouchImpl;
	
	public VikViewMainGui(Context context, VikData data) {
		super(context);
		initVikGui( context, data);
	}

	public VikViewMainGui(Context context, AttributeSet attrs, VikData data) {
		super(context, attrs);
		initVikGui( context, data);
	}

	public VikViewMainGui(Context context, AttributeSet attrs, int defStyle, VikData data) {
		super(context, attrs, defStyle);
		initVikGui( context, data);
	}
	
	protected void initVikGui( Context context, VikData data ) {
		alarmData = data;
		currentViewport = new Rect();
		defaultPaint = new Paint();
		backgroundImpl = new Background(getResources(), R.drawable.gui_background);
		clockImpl = new Clock(getResources(), R.drawable.clockhand_little, R.drawable.clockhand_big, R.drawable.clockhand_alarm);
		switchImpl = new Switch(getResources());
		alarmDisplayImpl = new AlarmDisplay(getResources());
		try {
			notifyTouchImpl = (TouchNotificationListener) context;
		} 
		catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
		
		//animation
		final Handler handler20secEvent = new Handler();
		Runnable runnable20secEvent = new Runnable() {
			public void run() {
				invalidate();
				requestLayout();
				handler20secEvent.postDelayed(this, 100);
			}
		};
		handler20secEvent.post(runnable20secEvent);
		
		//interaction
		this.setOnTouchListener(this);
	}

	public void updateAlarmData(VikData data) {
		alarmData = data;
		clockImpl.updateAlarmHand();
		alarmDisplayImpl.update();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//check for changes
		if (!canvas.getClipBounds().equals(currentViewport))
		{
			canvas.getClipBounds(currentViewport);
			backgroundImpl.onCanvasSizeChanged(canvas);
			clockImpl.onCanvasSizeChanged(canvas);
			switchImpl.onCanvasSizeChanged(canvas);
			alarmDisplayImpl.onCanvasSizeChanged(canvas);
		}
		
		//draw
		backgroundImpl.draw(canvas);
		clockImpl.draw(canvas);
		switchImpl.draw(canvas);
		alarmDisplayImpl.draw(canvas);
	}

	public boolean onTouch(View v, MotionEvent event) {
		
		//react only to touch downs
		if( event.getAction() != MotionEvent.ACTION_DOWN )
		{
			return false;
		}
		
		//boundary points transformed to absolute viewport coordinates for comparability with touch position
		PointF switch_TopLeftBoundary = new PointF( 0.70218f, 0.1f );
		switch_TopLeftBoundary = backgroundImpl.relativeImgCoords2viewportCoords(switch_TopLeftBoundary);
		PointF switch_BottomRightBoundary = new PointF( 0.9f, 0.383221f );
		switch_BottomRightBoundary = backgroundImpl.relativeImgCoords2viewportCoords(switch_BottomRightBoundary);
		PointF clock_TLBoundary = new PointF( 0.2223f, 0.18643f );
		clock_TLBoundary = backgroundImpl.relativeImgCoords2viewportCoords(clock_TLBoundary);
		PointF clock_BRBoundary = new PointF( 0.61397f, 0.517866f );
		clock_BRBoundary = backgroundImpl.relativeImgCoords2viewportCoords(clock_BRBoundary);
		PointF alarmDisp_TLBoundary = new PointF( 0.5081f, 0.517866f );
		alarmDisp_TLBoundary = backgroundImpl.relativeImgCoords2viewportCoords(alarmDisp_TLBoundary);
		PointF alarmDisp_BRBoundary = new PointF( 0.9f, 0.766442f );
		alarmDisp_BRBoundary = backgroundImpl.relativeImgCoords2viewportCoords(alarmDisp_BRBoundary);
		PointF sound_TLBoundary = new PointF( 0.1482f, 0.590367f );
		sound_TLBoundary = backgroundImpl.relativeImgCoords2viewportCoords(sound_TLBoundary);
		PointF sound_BRBoundary = new PointF( 0.5081f, 0.841015f );
		sound_BRBoundary = backgroundImpl.relativeImgCoords2viewportCoords(sound_BRBoundary);
		PointF volume_TLBoundary = new PointF( 0.5504f, 0.766442f );
		volume_TLBoundary = backgroundImpl.relativeImgCoords2viewportCoords(volume_TLBoundary);
		PointF volume_BRBoundary = new PointF( 0.9f, 0.9f );
		volume_BRBoundary = backgroundImpl.relativeImgCoords2viewportCoords(volume_BRBoundary);
		
		
		//testing touch position starting from top to bottom
		//margin
		if( event.getY() < switch_TopLeftBoundary.y ) 
		{
			return false; 
		}
		
		//switch only
		if( event.getY() < clock_TLBoundary.y)
		{
			//testing from left to right
			//margin
			if( event.getX() < switch_TopLeftBoundary.x )
			{
				return false;
			}
			
			//switch
			if( event.getX() < switch_BottomRightBoundary.x )
			{
				notifyTouchImpl.onTouchAlarmSwitch();
				return true;
			}
			
			//margin
			return false;
		}
		
		//switch and clock
		if( event.getY() < switch_BottomRightBoundary.y)
		{
			//testing from left to right
			//margin
			if( event.getX() < clock_TLBoundary.x )
			{
				return false;
			}
			
			//clock
			if( event.getX() < clock_BRBoundary.x )
			{
				notifyTouchImpl.onTouchSetNewAlarm();
				return true;
			}
			
			//inner margin
			if( event.getX() < switch_TopLeftBoundary.x )
			{
				return false;
			}
			
			//switch
			if( event.getX() < switch_BottomRightBoundary.x )
			{
				notifyTouchImpl.onTouchAlarmSwitch();
				return true;
			}
			
			//margin
			return false;
		}
		
		//clock only
		if( event.getY() < clock_BRBoundary.y )
		{
			//testing from left to right
			//margin
			if( event.getX() < clock_TLBoundary.x )
			{
				return false;
			}
			
			//clock
			if( event.getX() < clock_BRBoundary.x )
			{
				notifyTouchImpl.onTouchSetNewAlarm();
				return true;
			}
			
			//margin
			return false;
		}
		
		//alarmDisplay only
		if( event.getY() < sound_TLBoundary.y )
		{
			//testing from left to right
			//margin
			if( event.getX() < alarmDisp_TLBoundary.x )
			{
				return false;
			}
			
			//alarmDisplay
			if( event.getX() < alarmDisp_BRBoundary.x )
			{
				notifyTouchImpl.onTouchSetNewAlarm();
				return true;
			}
			
			//margin
			return false;
		}
		
		//alarmDisplay and sound
		if( event.getY() < volume_TLBoundary.y )
		{
			//testing from left to right
			//margin
			if( event.getX() < sound_TLBoundary.x )
			{
				return false;
			}
			
			//sound
			if( event.getX() < sound_BRBoundary.x )
			{
				notifyTouchImpl.onTouchChangeAlarmsound();
				return true;
			}
			
			//alarmDisplay
			if( event.getX() < alarmDisp_BRBoundary.x )
			{
				notifyTouchImpl.onTouchSetNewAlarm();
				return true;
			}
			
			//margin
			return false;
		}
		
		//sound and volume
		if( event.getY() < sound_BRBoundary.y )
		{
			//testing from left to right
			//margin
			if( event.getX() < sound_TLBoundary.x )
			{
				return false;
			}
			
			//sound
			if( event.getX() < sound_BRBoundary.x )
			{
				notifyTouchImpl.onTouchChangeAlarmsound();
				return true;
			}
			
			//inner margin
			if( event.getX() < volume_TLBoundary.x )
			{
				return false;
			}
			
			//volume
			if( event.getX() < volume_BRBoundary.x )
			{
				notifyTouchImpl.onTouchChangeVolume();
				return true;
			}
			
			//margin
			return false;
		}
		
		//volume only
		if( event.getY() < volume_BRBoundary.y )
		{
			//testing from left to right
			//margin
			if( event.getX() < volume_TLBoundary.x )
			{
				return false;
			}
			
			//volume
			if( event.getX() < volume_BRBoundary.x )
			{
				notifyTouchImpl.onTouchChangeVolume();
				return true;
			}
			
			//margin
			return false;
		}
		
		//only margin left
		return true;
	}
	
}
