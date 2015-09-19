package com.ionicframework.ionicapp395632;

import static de.apparentmedia.cordova.NativeUIPlugin.*;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class Activity2 extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity2);
	}
	
	public void gotoActivity1(View view) {
		click(view.getId());
	}
}
