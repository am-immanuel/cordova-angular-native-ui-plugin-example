package com.ionicframework.ionicapp395632;

import de.apparentmedia.cordova.NativeUIPlugin;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class Activity1 extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity1);
	}
	
	public void gotoActivity2(View view) {
		NativeUIPlugin.evaluateScopeExpression(getResources().getResourceEntryName(view.getId()), "gotoActivity2()");
	}

	public void updateTextField(View view) {
		NativeUIPlugin.evaluateScopeExpression(getResources().getResourceEntryName(view.getId()), "updateTextField()");
	}
}
