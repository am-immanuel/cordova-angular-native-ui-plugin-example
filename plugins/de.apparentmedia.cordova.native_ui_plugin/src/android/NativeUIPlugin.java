package de.apparentmedia.cordova;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
	
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		context = cordova.getActivity();
		INSTANCE = this;
		$rootScope = new Scope(this);
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
	
	protected void evaluateScopeExpression(int scopeId, String expression) {
		JSONObject message = new JSONObject();
		try {
			message.put("scopeId", scopeId);
			message.put("expression", expression);
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
	
	public Scope getScopeByScopeId(int scopeId) {
		return new Scope($rootScope, scopeId);
	}
	
	public static NativeUIPlugin getInstance() {
		if (INSTANCE == null) {
			String msg = "NativeUIPlugin wasn't initialized yet";
			Log.e(TAG, msg);
			throw new IllegalStateException(msg);
		}
		if (INSTANCE.permanentCallback == null) {
			String msg = "NativeUIPlugin: Permanent callback hasn't been registered yet";
			Log.e(TAG, msg);
			throw new IllegalStateException(msg);
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

		Set<Integer> oldScopeIds = new HashSet<Integer>(scopeMap.keySet());
		Set<Integer> newScopeIds = new HashSet<Integer>(transportScopes.length());
		Iterator<String> iter = transportScopes.keys();
		while (iter.hasNext()) {
			newScopeIds.add(updateJavaScope(Integer.parseInt(iter.next()), transportScopes).$id);
		}
		
		// remove obsolete scopes
		oldScopeIds.removeAll(newScopeIds);
		for (Integer idToDelete : oldScopeIds) {
			scopeMap.remove(idToDelete);
		}
	}
	
	private Scope updateJavaScope(int id, JSONObject transportScopes) throws JSONException {
		JSONObject transportScope = transportScopes.getJSONObject("" + id);
		Scope scopeToUpdate = getExistingScopeOrCreateNewOne(id, transportScopes);
		Integer $parent = transportScope.isNull("$parent") ? null : transportScope.getInt("$parent");
		Integer $$childHead = transportScope.isNull("$$childHead") ? null : transportScope.getInt("$$childHead");
		Integer $$childTail = transportScope.isNull("$$childTail") ? null : transportScope.getInt("$$childTail");
		Integer $$nextSibling = transportScope.isNull("$$nextSibling") ? null : transportScope.getInt("$$nextSibling");
		if ($parent != null && scopeToUpdate.getParent().$id != $parent) {
			throw new IllegalStateException("parent relation is inconsistent");
		}
		scopeToUpdate.$$childHead = getExistingScopeOrCreateNewOne($$childHead, transportScopes);
		scopeToUpdate.$$childTail = getExistingScopeOrCreateNewOne($$childTail, transportScopes);
		scopeToUpdate.$$nextSibling = getExistingScopeOrCreateNewOne($$nextSibling, transportScopes);
		return scopeToUpdate;
	}
	
	private Scope getExistingScopeOrCreateNewOne(Integer id, JSONObject transportScopes) throws JSONException {
		if (id == null) 
			return null;
		Scope scopeToUpdate = scopeMap.get(id);
		if (scopeToUpdate == null) {
			JSONObject transportScope = transportScopes.getJSONObject("" + id);
			Scope parentScope = getExistingScopeOrCreateNewOne(transportScope.getInt("$parent"), transportScopes);
			scopeToUpdate = new Scope(parentScope, id);
			scopeMap.put(id, scopeToUpdate);
		}
		return scopeToUpdate;
	}
}
