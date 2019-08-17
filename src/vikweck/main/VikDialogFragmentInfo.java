package vikweck.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class VikDialogFragmentInfo extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

		String infoText;
		Resources res = getResources();
		infoText = String.format(
		    res.getString(R.string.info_text), getAppVersionName());
		
		final TextView tx1=new TextView( getActivity() );
		tx1.setText(infoText);
		tx1.setAutoLinkMask(Linkify.ALL);
		tx1.setMovementMethod(LinkMovementMethod.getInstance());
		tx1.setTextSize(18);
		tx1.setPadding(20, 10, 20, 10);
		
		builder.setTitle( R.string.info_title );
		builder.setView(tx1);
		builder.setNeutralButton(R.string.zurueck_btn, null);
		
		return builder.create();
	}
	
	private String getAppVersionName () {
		
		String pName = this.getActivity().getPackageName();
		PackageInfo pInfo;
		try {
			pInfo = this.getActivity().getPackageManager().getPackageInfo(pName, 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			return "X.X";
		}
	}
}
