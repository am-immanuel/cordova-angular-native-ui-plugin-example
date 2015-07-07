package de.apparentmedia.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ionicframework.ionicapp395632.Activity1;
import com.ionicframework.ionicapp395632.Activity2;

public class NativeUIPlugin extends CordovaPlugin {
	public static String TAG = "NativeUIPlugin";
	private static NativeUIPlugin INSTANCE;
	private CallbackContext permanentCallback;
	private Context context;
	
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		context = cordova.getActivity();
		INSTANCE = this;
	}
	
	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
		if ("registerPermanentCallback".equals(action)) {
			permanentCallback = callbackContext;
			PluginResult pluginResult = new PluginResult(Status.OK);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);
		} else if ("stateChangeStart".equals(action)) {
			JSONObject toState = args.getJSONObject(0);
			JSONObject toParams = args.getJSONObject(1);
			JSONObject fromState = args.getJSONObject(2);
			JSONObject fromParams = args.getJSONObject(3);
			Log.i(TAG, toState.getString("name"));
			if ("app.activity2".equals(toState.getString("name"))) {
				context.startActivity(new Intent(context, Activity2.class));
			} else if ("app.activity1".equals(toState.getString("name"))) {
				context.startActivity(new Intent(context, Activity1.class));
			}
		} else {
			Log.i(TAG, "action: " + action);
			callbackContext.success();
		}
		return true;
	}
	
	public static void evaluateScopeExpression(String elementId, String expression) {
		if (INSTANCE == null) {
			Log.e(TAG, "NativeUIPlugin wasn't initialized yet");
			return;
		}
		if (INSTANCE.permanentCallback == null) {
			Log.e(TAG, "Permanent callback hasn't been registered yet");
			return;
		}
		
		JSONObject message = new JSONObject();
		try {
			message.put("elementId", elementId);
			message.put("expression", expression);
			PluginResult result = new PluginResult(Status.OK, message);
			result.setKeepCallback(true);
			INSTANCE.permanentCallback.sendPluginResult(result);
		} catch (JSONException e) {
			throw new RuntimeException("Could not construct JSON message", e);
		}
	}
}
