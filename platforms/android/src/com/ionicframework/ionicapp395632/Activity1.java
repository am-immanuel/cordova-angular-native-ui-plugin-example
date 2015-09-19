package com.ionicframework.ionicapp395632;

import static de.apparentmedia.cordova.NativeUIPlugin.*;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import de.apparentmedia.cordova.NativeUIPlugin;
import de.apparentmedia.cordova.Scope;

public class Activity1 extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity1);
		init(this, R.id.textView1, new NativeUIPlugin.InitCallback() {
			@Override
			public void init(int viewId, Scope scope) {
				bind(R.id.textView1, new Handler.Callback() {
					
					@Override
					public boolean handleMessage(Message msg) {
						((TextView) findViewById(R.id.textView1)).setText(msg.obj.toString());
						return false;
					}
				});
			}
		});
		final EditText editText = ((EditText) findViewById(R.id.editText1));
		init(this, editText.getId(), new NativeUIPlugin.InitCallback() {
			@Override
			public void init(int viewId, Scope scope) {
				bind(editText.getId(), new Handler.Callback() {
					
					@Override
					public boolean handleMessage(Message msg) {
						editText.setText(msg.obj.toString());
						return false;
					}
				});
				editText.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						set(editText.getId(), s);
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						
					}
				});
			}
		});	
	}
	
	public void gotoActivity2(View view) {
		click(view.getId());
	}

	public void updateTextField(View view) {
		click(view.getId());
	}
}
