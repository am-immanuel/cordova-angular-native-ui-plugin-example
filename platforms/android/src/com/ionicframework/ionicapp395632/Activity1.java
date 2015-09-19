package com.ionicframework.ionicapp395632;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import de.apparentmedia.cordova.NativeUIPlugin;

public class Activity1 extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity1);
	}
	
	public void gotoActivity2(View view) {
//		NativeUIPlugin.getInstance().getScopeByDomElementId(getResources().getResourceEntryName(view.getId())).evaluateExpression("gotoActivity2()");
	}

	public void updateTextField(View view) {
//		NativeUIPlugin.getInstance().getScopeByDomElementId(getResources().getResourceEntryName(view.getId())).evaluateExpression("updateTextField()");
	}
}
