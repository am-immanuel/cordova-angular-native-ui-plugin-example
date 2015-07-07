package de.apparentmedia.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import android.util.Log;

public class NativeUIPlugin extends CordovaPlugin {
	public static String TAG = "NativeUIPlugin";
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}
	
	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
		Log.i(TAG, "action: " + action);
		callbackContext.success();
		return true;
	}
}
