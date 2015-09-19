package com.ionicframework.ionicapp395632;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import de.apparentmedia.cordova.NativeUIPlugin;

public class Activity2 extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity2);
	}
	
	public void gotoActivity1(View view) {
		NativeUIPlugin.getInstance().evaluateScopeExpression(getResources().getResourceEntryName(view.getId()), "gotoActivity1()");
	}
}
