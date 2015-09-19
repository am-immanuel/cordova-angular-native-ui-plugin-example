package de.apparentmedia.cordova;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler.Callback;
import android.util.Log;

import com.ionicframework.ionicapp395632.Activity1;
import com.ionicframework.ionicapp395632.Activity2;

public class NativeUIPlugin extends CordovaPlugin {
	public static String TAG = "NativeUIPlugin";
	private static NativeUIPlugin INSTANCE;
	protected CallbackContext permanentCallback;
	protected Context context;
	protected Scope $rootScope;
	protected Map<Integer, Scope> scopeMap = new HashMap<Integer, Scope>();
	protected Map<String, Scope> nativeId2ScopeMap = new HashMap<String, Scope>();
	
	public NativeUIPlugin() {
		$rootScope = new Scope(this);
	}
	
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		context = cordova.getActivity();
		INSTANCE = this;
		scopeMap.put($rootScope.$id, $rootScope);
	}
	
	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
		if ("registerPermanentCallback".equals(action)) {
			permanentCallback = callbackContext;
			PluginResult pluginResult = new PluginResult(Status.OK);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);
		} else if ("updateTransportScopeMap".equals(action)) {
			updateJavaScopes(args.getJSONObject(0));
		} else if ("$stateChangeStart".equals(action) || "$stateChangeSuccess".equals(action)) {
			JSONObject toState = args.getJSONObject(0);
			JSONObject toParams = args.getJSONObject(1);
			JSONObject fromState = args.getJSONObject(2);
			JSONObject fromParams = args.getJSONObject(3);
            JSONObject transportScopes = args.optJSONObject(4);
           	updateJavaScopes(transportScopes);
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
	
	public void evaluateScopeExpression(String nativeId, String expression) {
		Scope scope = nativeId2ScopeMap.get(nativeId);
		if (scope == null) {
			Log.e(TAG, "native ID " + nativeId + " is not yet registered!");
		} else {
			evaluateScopeExpression(scope.$id, expression);
		}
	}

	protected void evaluateScopeExpression(int scopeId, String expression) {
		invokePermanentCallback("evaluateScopeExpression", scopeId, expression);
	}

	private void invokePermanentCallback(String action, Object... args) {
		JSONObject message = new JSONObject();
		JSONArray jsonArgs = new JSONArray();
		try {
			message.put("action", action);
			for (Object arg : args) {
				jsonArgs.put(arg);
			}
			message.put("args", jsonArgs);
			PluginResult result = new PluginResult(Status.OK, message);
			result.setKeepCallback(true);
			getInstance().permanentCallback.sendPluginResult(result);
		} catch (JSONException e) {
			throw new RuntimeException("Could not construct JSON message", e);
		}
	}
	
	/**
	 * @return The AngularJS root scope.
	 */
	public Scope getRootScope() {
		return $rootScope;
	}
	
	public Scope getScopeByNativeId(String scopeId) {
		return nativeId2ScopeMap.get(scopeId);
	}
	
	public static NativeUIPlugin getInstance() {
		if (INSTANCE == null) {
			String msg = "NativeUIPlugin wasn't initialized yet";
			Log.e(TAG, msg);
			return new NativeUIPlugin();
		}
		if (INSTANCE.permanentCallback == null) {
			String msg = "NativeUIPlugin: Permanent callback hasn't been registered yet";
			Log.e(TAG, msg);
		}
		return INSTANCE;
	}

	public void invokeScopeMethod(int scopeId, String string,
			String eventName, Callback callback) {
		
	}
	
	@SuppressWarnings("unchecked")
	private void updateJavaScopes(JSONObject transportScopes) throws JSONException {
		if (transportScopes == null)
			return;

		Iterator<String> iter = transportScopes.keys();
		while (iter.hasNext()) {
			updateJavaScope(Integer.parseInt(iter.next()), transportScopes);
		}
	}
	
	private Scope updateJavaScope(int id, JSONObject transportScopes) throws JSONException {
		JSONObject transportScope = transportScopes.getJSONObject("" + id);
		Scope scopeToUpdate = getExistingScopeOrCreateNewOne(id, transportScopes);
		Integer $parent = transportScope.isNull("$parent") ? null : transportScope.getInt("$parent");
		if ($parent != null && scopeToUpdate.getParent().$id != $parent) {
			throw new IllegalStateException("parent relation is inconsistent");
		}
		Integer $$childHead = transportScope.isNull("$$childHead") ? null : transportScope.getInt("$$childHead");
		Integer $$childTail = transportScope.isNull("$$childTail") ? null : transportScope.getInt("$$childTail");
		Integer $$nextSibling = transportScope.isNull("$$nextSibling") ? null : transportScope.getInt("$$nextSibling");
		scopeToUpdate.nativeId = transportScope.isNull("nativeId") ? null: transportScope.getString("nativeId");
		scopeToUpdate.$$childHead = getExistingScopeOrCreateNewOne($$childHead, transportScopes);
		scopeToUpdate.$$childTail = getExistingScopeOrCreateNewOne($$childTail, transportScopes);
		scopeToUpdate.$$nextSibling = getExistingScopeOrCreateNewOne($$nextSibling, transportScopes);
		if (scopeToUpdate.nativeId != null) {
			nativeId2ScopeMap.put(scopeToUpdate.nativeId, scopeToUpdate);
		}
		return scopeToUpdate;
	}
	
	private Scope getExistingScopeOrCreateNewOne(Integer id, JSONObject transportScopes) throws JSONException {
		if (id == null) 
			return null;
		Scope scopeToUpdate = scopeMap.get(id);
		if (scopeToUpdate == null) {
			String strId = "" + id;
			if (transportScopes.isNull(strId)) {
				throw new IllegalStateException("Scope with ID " + id + " does not exist and is not provided from JavaScript");
			}
			JSONObject transportScope = transportScopes.getJSONObject("" + id);
			Scope parentScope = getExistingScopeOrCreateNewOne(transportScope.getInt("$parent"), transportScopes);
			scopeToUpdate = new Scope(parentScope, id);
			scopeMap.put(id, scopeToUpdate);
		}
		return scopeToUpdate;
	}
}
