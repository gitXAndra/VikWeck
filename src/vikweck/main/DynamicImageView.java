package vikweck.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * This ImageView uses the calculated size of the fixed dimension ("width" or "height") for the ImageView 
 * and adapts the other dimension to show the whole scaled Image without distortion.
 * Use either match_parent or wrap_content as initial size indicator for the adapting dimension 
 * (a fixed size will override the calculated new size).
 * */
public class DynamicImageView extends ImageView {
	final static int FIXED_WIDTH = 0;
	final static int FIXED_HEIGHT = 1;
	int fixedDimension;
	int minH=40;
	
	public DynamicImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray attArr = context.getTheme().obtainStyledAttributes(
				attrs, 
				R.styleable.DynamicImageView, 
				0, 0);
		try{
			fixedDimension = attArr.getInteger(R.styleable.DynamicImageView_fixedDimension, 0);
			
		}finally {
			attArr.recycle();
		}
		setMeasuredDimension( Integer.MAX_VALUE, Integer.MAX_VALUE );
	}
	
	public DynamicImageView(Context context, int fixedSizeSide) {
		super(context);
		fixedDimension = fixedSizeSide;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		final Drawable d = this.getDrawable();
        if (d != null) {
        	int width = MeasureSpec.getSize(widthMeasureSpec);
        	int height = MeasureSpec.getSize(heightMeasureSpec);
        	
            if( View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.AT_MOST &&
            		getMeasuredWidth() < width && getMeasuredWidth() != 0)
    		{
    			width = getMeasuredWidth();// last calculated width value is still valid
    		}
    		if( View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST &&
    				getMeasuredHeight() < height && getMeasuredHeight() != 0 )
    		{
    			height = getMeasuredHeight();// last calculated height value is still valid
    		}
    		
            if( fixedDimension == FIXED_WIDTH )
            {
	        	// ceil not round - avoid thin vertical gaps along the left/right edges
		        height = (int) java.lang.Math.ceil(width * (float) d.getIntrinsicHeight() / d.getIntrinsicWidth());
            }
            else
            {
		        width = (int) java.lang.Math.ceil(height * (float) d.getIntrinsicWidth() / d.getIntrinsicHeight());
            }
            
            // whatever has been calculated, the requirements for exactly or at_most have to be met:
    		if( View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY )
    		{
    			width = View.MeasureSpec.getSize(widthMeasureSpec);
    		}
    		if( View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY )
    		{
    			height = View.MeasureSpec.getSize(heightMeasureSpec);
    		}
    		
    		if( View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.AT_MOST &&
    				width > View.MeasureSpec.getSize(widthMeasureSpec) )
    		{
    			width = View.MeasureSpec.getSize(widthMeasureSpec);
    		}
    		if( View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST &&
    				height > View.MeasureSpec.getSize(heightMeasureSpec) )
    		{
    			height = View.MeasureSpec.getSize(heightMeasureSpec);
    		}
    		
		    this.setMeasuredDimension(width, height);
		    
		    
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
	}
	
}
