package vikweck.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * This Layout uses the space, which it occupies due to its child (only one child!), to show a background image.
 * The image is distorted and scaled to fit exactly into the view.
 * Without a child it dosen't request any space, thus will not be visible.
 * @author Sandra Sichting
 */
public class BackgroundImageLayout extends ViewGroup {
	
	protected Drawable backgroundDrawable;
	protected boolean allowDistortion;
	
	public BackgroundImageLayout(Context context) {
		super(context);
	}
	
	public BackgroundImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray attArr = context.getTheme().obtainStyledAttributes(
				attrs, 
				R.styleable.BackgroundImageLayout, 
				0, 0);
		
		try{
			backgroundDrawable = attArr.getDrawable(R.styleable.BackgroundImageLayout_backgroundImage);
			allowDistortion = attArr.getBoolean(R.styleable.BackgroundImageLayout_allowDistortion, true);
		}finally {
			attArr.recycle();
		}
	}
	
	public BackgroundImageLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int count = getChildCount();
		if( count != 1 )
		{
			return;
		}
		
		int newWidth = 0;
		int newHeight = 0;
		
		final View child = getChildAt(0);
		if (child.getVisibility() != GONE) {
            // Measure the child.
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
            
            newWidth = child.getMeasuredWidth();
            newHeight = child.getMeasuredHeight();
		}
		
		if( View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY )
		{
			newWidth = View.MeasureSpec.getSize(widthMeasureSpec);
		}
		if( View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY )
		{
			newHeight = View.MeasureSpec.getSize(heightMeasureSpec);
		}
		
		if( View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.AT_MOST &&
				newWidth > View.MeasureSpec.getSize(widthMeasureSpec) )
		{
			newWidth = View.MeasureSpec.getSize(widthMeasureSpec);
		}
		if( View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST &&
				newHeight > View.MeasureSpec.getSize(heightMeasureSpec) )
		{
			newHeight = View.MeasureSpec.getSize(heightMeasureSpec);
		}
		
		setMeasuredDimension( newWidth, newHeight );
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if( backgroundDrawable != null )
		{
			int left = 0;
			int top = 0;
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			
			if( !allowDistortion )
			{
				float bmpSidesRatio = ((float) backgroundDrawable.getIntrinsicWidth()) / backgroundDrawable.getIntrinsicHeight();
				float canvasSidesRatio = ((float) canvas.getWidth()) / canvas.getHeight();
				if( bmpSidesRatio > canvasSidesRatio )
				{
					height = (int) ( canvas.getWidth() / bmpSidesRatio );
				}
				else
				{
					width = (int) ( canvas.getHeight() * bmpSidesRatio );
				}
				left = (int) ( (canvas.getWidth()-width) / 2 );
				top = (int) ( (canvas.getHeight()-height) / 2 );
			}
			backgroundDrawable.setBounds( left, top, left+width, top+height);
			backgroundDrawable.draw(canvas);
		}
		
		super.dispatchDraw(canvas);
	}
	
	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		int count = getChildCount();
		if( count != 1 )
		{
			return;
		}
		
		getChildAt(0).layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
	}
}
