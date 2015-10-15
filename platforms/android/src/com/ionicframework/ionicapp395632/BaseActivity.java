package com.ionicframework.ionicapp395632;

import android.app.Activity;
import android.os.Bundle;

import static de.apparentmedia.cordova.NativeUIPlugin.*;

/*******************************************
 * Created by katrinwistuba on 14.10.15.
 *
 * @android
 */
public class BaseActivity extends Activity {



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backButtonPressed();
    }
}
