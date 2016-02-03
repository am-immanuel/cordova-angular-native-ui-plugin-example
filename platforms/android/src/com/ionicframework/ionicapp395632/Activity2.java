package com.ionicframework.ionicapp395632;

import static de.apparentmedia.cordova.NativeUIPlugin.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import de.apparentmedia.cordova.NativeUIPlugin;
import de.apparentmedia.cordova.Scope;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

public class Activity2 extends BaseActivity  {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity2);
		init(this, R.layout.activity2);
		
		//activityInitBinding(this, R.id.outputNumberOfClicks);
		//activityInitBinding(this, R.id.outputWarning);
	}
	
	/*
	public void gotoActivity1(View view) {
		click(view.getId());
	}
	
	public void countClicks(View view) {
		click(view.getId());
	}
	
	public void calc(View view) {
		click(view.getId());
	}
	*/
	
	
	/*private void activityInitBinding(Activity context, final int id) {
		init(context, id, new NativeUIPlugin.InitCallback() {
			@Override
			public void init(int viewId, Scope scope) {
				bind(id, new Handler.Callback() {
					
					@Override
					public boolean handleMessage(Message msg) {
						if (msg.obj != null) {
							((TextView) findViewById(id)).setText(msg.obj.toString());
						} else {
							((TextView) findViewById(id)).setText("Error");
						}
						return false;
					}
				});
			}
		});
	}*/
}
