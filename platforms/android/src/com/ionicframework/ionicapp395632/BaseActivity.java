package com.ionicframework.ionicapp395632;

import android.app.Activity;

import static de.apparentmedia.cordova.NativeUIPlugin.*;

/*******************************************
 * Created by katrinwistuba on 14.10.15.
 *
 * @android
 * This class extends the Android activity class by adding.
 * 
 */
public class BaseActivity extends Activity {

	/**
	 * Called after a user has pressed the device back button.
	 * Invokes a NativeUIPlugin function which indicates the
	 * back button click on the current activity.
	 * */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        // calls the NativeUIPlugin function
        backButtonPressed();
    }
}
