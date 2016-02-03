package com.ionicframework.ionicapp395632;

import static de.apparentmedia.cordova.NativeUIPlugin.*;
import de.apparentmedia.cordova.NativeUIPlugin;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class Activity1 extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init(this, R.layout.activity1);
		//getInstance().setSplashScreen(true);
		//activateSplashScreen(getInstance().getSplashScreen());
	}
	
	public void activateSplashScreen(boolean value) {
		Dialog splashScreen = new Dialog(this, R.style.SplashScreen);
		if (value) {
			splashScreen.setContentView(R.layout.splashscreen);
			ImageView img = (ImageView) splashScreen.findViewById(R.id.splashScreenIcon);
			img.setImageResource(R.drawable.nxpsplash);	
			splashScreen.show();
		} else {
			splashScreen.dismiss();
			splashScreen = null;
		}
	}

}
