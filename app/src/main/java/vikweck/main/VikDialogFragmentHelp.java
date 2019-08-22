package vikweck.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class VikDialogFragmentHelp extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
		
		builder.setTitle( R.string.menu_help );
		
		LinearLayout linLay = new LinearLayout( getActivity() );
		LayoutParams linLayParams = new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
		linLay.setLayoutParams( linLayParams );
		linLay.setGravity( Gravity.CENTER );
		linLay.setOrientation(LinearLayout.VERTICAL);
		
		ImageView img = new ImageView( getActivity() );
		img.setImageResource(R.drawable.manual);
		linLay.addView(img);
		
		final TextView tx1=new TextView( getActivity() );
		tx1.setText(R.string.help_text);
		tx1.setTextSize(18);
		tx1.setPadding(25, 10, 20, 10);
		linLay.addView(tx1);
		
		ScrollView scroll = new ScrollView( getActivity() );
		scroll.addView(linLay);
		
		builder.setView(scroll);
		
		builder.setNeutralButton(R.string.zurueck_btn, null);
		
		return builder.create();
	}
}
