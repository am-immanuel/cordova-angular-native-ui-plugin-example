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
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.ionicframework.ionicapp395632.Activity1;
import com.ionicframework.ionicapp395632.Activity2;

public class NativeUIPlugin extends CordovaPlugin {
	public static String TAG = "NativeUIPlugin";
	private static NativeUIPlugin INSTANCE;
	protected CallbackContext permanentCallback;
	protected Context context;
	protected Scope $rootScope;
	private SparseArray<Scope> scopeMap = new SparseArray<Scope>();
	private SparseArray<Callback> callbackMap = new SparseArray<Callback>();
	private Map<String, Scope> nativeId2ScopeMap = new HashMap<String, Scope>();
	private Map<String, InitCallback> initCallbacksMap = new HashMap<String, InitCallback>();
	
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
		} else if ("invokeCallback".equals(action)) {
			int callbackHashCode = args.getInt(0);
			Callback callback = callbackMap.get(callbackHashCode);
			if (callback == null) {
				Log.e(TAG, "Could not find callback with hash " + callbackHashCode);
			} else {
				Message m = new Message();
				m.obj = args.get(1);
				callback.handleMessage(m);
			}
		} else {
			Log.i(TAG, "action: " + action);
			callbackContext.success();
		}
		return true;
	}
	
	private void clickInternal(int viewId) {
		clickInternal(getScopeByViewId(viewId));
	}

	private void bindInternal(int viewId, Callback callback) {
		Scope scope = getScopeByViewId(viewId);
		String expression = getElementAttribute(scope, "Bind", "Model");
		if (expression != null) {
			invokePermanentCallback("$watch", scope.$id, expression, callback);
		}
	}

	private void initInternal(int viewId, InitCallback callback) {
		Scope scope = getScopeByViewId(viewId);
		if (scope != null) {
			callback.init(viewId, scope);
		} else {
			String nativeId = context.getResources().getResourceEntryName(viewId);
			initCallbacksMap.put(nativeId, callback);
		}
	}

	public Scope getScopeByViewId(int viewId) {
		if (context == null) {
			return null;
		}
		return getScopeByNativeId(context.getResources().getResourceEntryName(viewId));
	}
	
	private void clickInternal(Scope scope) {
		String clickExpression = getElementAttribute(scope, "Click");
		if (clickExpression != null) {
			evaluateScopeExpression(scope.$id, clickExpression);
		}
	}
	
	private String getElementAttribute(Scope scope, String ngPostfix) {
		return getElementAttribute(scope, ngPostfix, null);
	}
	
	private String getElementAttribute(Scope scope, String ngPostfix, String ngPostfix2) {
		if (scope != null) {
			String expression = scope.getElementAttributes().get("ng" + ngPostfix);
			if (expression != null) {
				return expression;
			}
			expression = scope.getElementAttributes().get("dataNg" + ngPostfix);
			if (expression != null) {
				return expression;
			}
			if (ngPostfix2 == null) {
				return null;
			}
			expression = scope.getElementAttributes().get("ng" + ngPostfix2);
			if (expression != null) {
				return expression;
			}
			expression = scope.getElementAttributes().get("dataNg" + ngPostfix2);
			if (expression != null) {
				return expression;
			}
		}
		return null;
	}
	
	public static void click(int viewId) {
		getInstance().clickInternal(viewId);
	}
	
	public static void bind(int viewId, Callback callback) {
		getInstance().bindInternal(viewId, callback);
	}
	
	public static void init(int viewId, InitCallback callback) {
		getInstance().initInternal(viewId, callback);
	}

	public static void set(int viewId, Object value) {
		getInstance().setInternal(viewId, value);
	}
	
	private void setInternal(int viewId, Object value) {
		Scope scope = getScopeByViewId(viewId);
		String modelExpression = getElementAttribute(scope, "Model");
		if (modelExpression != null) {
			evaluateScopeExpression(scope.$id, modelExpression + "='" + value.toString().replaceAll("'", "\\'") + "'");
		}
	}

	public void evaluateScopeExpression(String nativeId, String expression) {
		Scope scope = getScopeByNativeId(nativeId);
		if (scope != null) {
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
				if (arg instanceof Callback) {
					int hashCode = arg.hashCode();
					jsonArgs.put(hashCode);
					callbackMap.put(hashCode, (Callback) arg);
				} else {
					jsonArgs.put(arg);
				}
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
	
	protected Scope getScopeByNativeId(String nativeId) {
		Scope result = nativeId2ScopeMap.get(nativeId);
		if (result == null) {
			Log.e(TAG, "native ID " + nativeId + " is not yet registered!");
		}
		return result;
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

	protected void invokeScopeMethod(int scopeId, String method,
			Object... args) {
		invokePermanentCallback("invokeMethod", method, args);
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
		JSONObject nativeUIData = transportScope.isNull("nativeUI") ? null: transportScope.getJSONObject("nativeUI");
		scopeToUpdate.getElementAttributes().clear();
		InitCallback initCallback = null;
		if (nativeUIData != null) {
			scopeToUpdate.nativeId = nativeUIData.getString("nativeId");
			initCallback = initCallbacksMap.get(scopeToUpdate.nativeId);
			initCallbacksMap.remove(scopeToUpdate.nativeId);
			JSONArray names = nativeUIData.names();
			for (int i = 0; i < names.length(); i++) {
				String name = names.getString(i);
				scopeToUpdate.getElementAttributes().put(name, nativeUIData.getString(name));
			}
		}
		scopeToUpdate.$$childHead = getExistingScopeOrCreateNewOne($$childHead, transportScopes);
		scopeToUpdate.$$childTail = getExistingScopeOrCreateNewOne($$childTail, transportScopes);
		scopeToUpdate.$$nextSibling = getExistingScopeOrCreateNewOne($$nextSibling, transportScopes);
		if (scopeToUpdate.nativeId != null) {
			nativeId2ScopeMap.put(scopeToUpdate.nativeId, scopeToUpdate);
		}
		if (initCallback != null) {
			Message msg = new Message();
			msg.obj = scopeToUpdate;
			initCallback.init(0, scopeToUpdate);
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
	
	public interface InitCallback {
		void init(int viewId, Scope scope);
	}
}
