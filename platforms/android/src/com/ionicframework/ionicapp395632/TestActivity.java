package com.ionicframework.ionicapp395632;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class TestActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity2);
	}
	
	public void onButton2Click(View view) {
		CordovaApp.webView.sendJavascript("console.log('test')");
	}
}
