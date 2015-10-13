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
		init(this, R.layout.activity1);

	}

}
