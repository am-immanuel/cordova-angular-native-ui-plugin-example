package de.apparentmedia.cordova;

import static de.apparentmedia.cordova.NativeUIPlugin.getInstance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import android.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ionicframework.ionicapp395632.Activity1;
import com.ionicframework.ionicapp395632.Activity2;

public class NativeUIPlugin extends CordovaPlugin {
	public static String TAG = "NativeUIPlugin";
	private static NativeUIPlugin INSTANCE;
	protected CallbackContext permanentCallback;
	protected static Activity context;
	protected Scope $rootScope;
	private static SparseArray<Scope> scopeMap = new SparseArray<Scope>();
	private static SparseArray<Callback> callbackMap = new SparseArray<Callback>();
	private static SparseArray<String> nativeIdMap = new SparseArray<String>();
	private static SparseArray<Scope> viewId2ScopeMap = new SparseArray<Scope>();
	private static Map<String, Scope> nativeId2ScopeMap = new HashMap<String, Scope>();
	private static Map<String, InitCallback> initCallbacksMap = new HashMap<String, InitCallback>();
	private static Map<String, Integer> mapNativeId2ViewId = new HashMap<String, Integer>();	// new
	private static int nextCallbackID = 1;
	private static View contentView;
	
	private boolean splashScreen = true;
	
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
			Log.i(TAG, toState.getString("name"));
			if ("app.activity2".equals(toState.getString("name"))) {
				Intent intentActivity2 = new Intent(context, Activity2.class);
				intentActivity2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intentActivity2.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				context.startActivity(intentActivity2);
			} else if ("app.activity1".equals(toState.getString("name"))) {
				Intent intentActivity1 = new Intent(context, Activity1.class);
				intentActivity1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intentActivity1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				//setSplashScreen(false);
				context.startActivity(intentActivity1);
			}
		} else if ("invokeCallback".equals(action)) {
			int callbackId = args.getInt(0);
			final Object obj = args.get(1);
			//final Object obj2 = args.get(2);		// new
			//final Object obj3 = args.get(3);		// new
			final Callback callback = callbackMap.get(callbackId);
			if (callback == null) {
				Log.e(TAG, "Could not find callback with ID " + callbackId);
			} else {
				context.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Message m = new Message();
						if (obj != JSONObject.NULL) {
							//Object[] msgList= {obj, obj2, obj3};	// new
							//m.obj = msgList;
							m.obj = obj;
						}
						callback.handleMessage(m);
					}
				});
			}
		} else {
			Log.i(TAG, "action: " + action);
			callbackContext.success();
		}
		return true;
	}
	
	private void bindInternal(int viewId, Callback callback) {
		String nativeId = getNativeId(viewId);
		Scope scope = getScopeByViewId(viewId);
		String expression = getElementAttribute(nativeId, scope, "Bind", "Model");
		
		if (expression != null) {
			if (expression.matches("1#(.*)")) {
				String[] multipleExpressions = expression.split("#");
				expression = multipleExpressions[1];
				TextView tv = (TextView)contentView.findViewById(viewId);
				tv.setText(expression);
			
			} else if (expression.matches("2#(.*)")) {
				String[] multipleExpressions = expression.split("#");
				for (int i=0; i<multipleExpressions.length-1; i++) {
					invokePermanentCallback("$watch", scope.$id, multipleExpressions[i], callback, expression);
				}
				
			}  else {
				invokePermanentCallback("$watch", scope.$id, expression, callback);
			}
		}
	}
	
    private void bindInternal(View view, String attribute, Callback callback) {
        String nativeId = getNativeId(view.getId());
        Scope scope = getScopeByViewId(view.getId());
        String modelExpression = getElementAttribute(nativeId, scope, attribute);
        if (modelExpression != null) {
            Message msg = new Message();
            msg.obj = modelExpression;
            callback.handleMessage(msg);
        }
    }

	private void initInternal(Activity context, int viewId, InitCallback callback) {
		if (this.context == null) {
			this.context = context;
		}
		Scope scope = getScopeByViewId(viewId);
		if (scope != null) {
			callback.init(viewId, scope);
		} else {
			String nativeId = context.getResources().getResourceEntryName(viewId);
			initCallbacksMap.put(nativeId, callback);
		}
	}

	public Scope getScopeByViewId(int viewId) {
		Scope result = viewId2ScopeMap.get(viewId);
		if (result == null) {
			String nativeId = getNativeId(viewId);
			result = nativeId2ScopeMap.get(nativeId);
			if (result != null) {
				viewId2ScopeMap.put(viewId, result);
			}
		}
		return result;
	}
	
	public String getNativeId(int viewId) {
		String result = nativeIdMap.get(viewId);
		if (result == null) {
			if (context != null) {
				result = context.getResources().getResourceEntryName(viewId);
				nativeIdMap.put(viewId, result);
			}
		}
		return result;
	}
	
	/*private void clickInternal(int viewId) {
		String nativeId = getNativeId(viewId);
		Scope scope = getScopeByViewId(viewId);
		String clickExpression = getElementAttribute(nativeId, scope, "Click");
		if (clickExpression != null) {
			evaluateScopeExpressionByScopeId(scope.$id, clickExpression);
		}
	}*/
	
	// new
	/*public static void radioClick(int viewId) {
		getInstance().radioClickInternal(viewId);
	}*/
	
	public static void checkClick(View view) {
		getInstance().checkClickInternal(view);
	}
	
	// new
	/*private void radioClickInternal(int viewId) {
		String nativeId = getNativeId(viewId);
		Scope scope = getScopeByViewId(viewId);
		String valueExpression = getElementAttribute(nativeId, scope, "value");
		String modelExpression = getElementAttribute(nativeId, scope, "Model");
		if ( (valueExpression != null) && (modelExpression != null) ) {
			evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "='" + valueExpression + "'");
		}
	}*/
	
	// new
		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		private void checkClickInternal(View view) {
			String nativeId = getNativeId(view.getId());
			Scope scope = getScopeByViewId(view.getId());
			
			// new
			Boolean checked = null;
			if (view instanceof CompoundButton) {
				checked = ((CompoundButton) view).isChecked();
			}
			
			String modelExpression = getElementAttribute(nativeId, scope, "Model");
			String valueExpression = getElementAttribute(nativeId, scope, "value");		// new
			String trueExpression = getElementAttribute(nativeId, scope, "TrueValue");
			String falseExpression = getElementAttribute(nativeId, scope, "FalseValue");
			String clickExpression = getElementAttribute(nativeId, scope, "Click");		// new
			
			if (modelExpression != null) {
				if (valueExpression != null) {		// new
					evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "='" + valueExpression + "'");
				} else if (checked && (trueExpression != null)) {
					evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "=" + trueExpression + "");
				} else if (!checked && (falseExpression != null)) {
					evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "=" + falseExpression + "");
				} else if (checked != null){
					evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "=" + checked + "");
				}	
			}
			
			// new
			if (clickExpression != null) {
				evaluateScopeExpressionByScopeId(scope.$id, clickExpression);
			}
		}
	
	private String getElementAttribute(String nativeId, Scope scope, String ngPostfix) {
		return getElementAttribute(nativeId, scope, ngPostfix, null);
	}
	
	private String getElementAttribute(String nativeId, Scope scope, String ngPostfix, String ngPostfix2) {
		if (scope != null) {
			String expression = scope.getElementAttribute(nativeId, "ng" + ngPostfix);
			if (expression != null) {
				return expression;
			}
			expression = scope.getElementAttribute(nativeId, "dataNg" + ngPostfix);
			if (expression != null) {
				return expression;
			}
			
			// new
			expression = scope.getElementAttribute(nativeId, ngPostfix);
			if (expression != null) {
				return expression;
			}
			
			/*expression = scope.getElementAttribute(nativeId, "value");
			if (expression != null) {
				return expression;
			}
			
			expression = scope.getElementAttribute(nativeId, "TrueValue");
			if (expression != null) {
				return expression;
			}
			
			expression = scope.getElementAttribute(nativeId, "FalseValue");
			if (expression != null) {
				return expression;
			}*/
			
			if (ngPostfix2 == null) {
				return null;
			}
			expression = scope.getElementAttribute(nativeId, "ng" + ngPostfix2);
			if (expression != null) {
				return expression;
			}
			expression = scope.getElementAttribute(nativeId, "dataNg" + ngPostfix2);
			if (expression != null) {
				return expression;
			}
			
			expression = scope.getElementAttribute(nativeId, "innerBindingHTML");
			
			// only necessary if an error occurs in an element with ng-bind-template
			if (expression == null) {
				expression = scope.getElementAttribute(nativeId, "ngBindTemplate");
			}
			
			if (expression != null) {
				Pattern p = Pattern.compile("\\{\\{\\w+\\}\\}");
				Matcher m = p.matcher(expression);
				String bindExpression = "";
				while(m.find()) {
					bindExpression = bindExpression + m.group().replace("{", "").replace("}", "") + "#" ; 
				}
				
				if (bindExpression != "") {
					bindExpression = "2#" + bindExpression + expression;
				} else {
					bindExpression = "1#" + expression;
				}
				
				return bindExpression;
			}
			
			

		}
		return null;
	}
	
	/*public static void click(int viewId) {
		getInstance().clickInternal(viewId);
	}*/
	
	public static void bind(int viewId, Callback callback) {
		getInstance().bindInternal(viewId, callback);
	}
	
    public static void bindClick(final View view) {
        getInstance().bindInternal(view, "Click", new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//click(v.getId());
						checkClick(v);
					}
				});
				return false;
			}
		});
    }
    
    // new
    // TODO: instanceof --> getClass()
    public static void bindRadioClick(final View view) {
        getInstance().bindInternal(view, "Model", new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						checkClick(v);		// new
						/*if (v instanceof Switch) {
							checkClick(v);
						} else if (v instanceof ToggleButton) {
							checkClick(v);
						} else if (v instanceof RadioButton) {
							radioClick(v.getId());
						} else if (v instanceof CheckBox) {
							checkClick(v);
						} */
					}
				});
				return false;
			}
		});
    }
    
    public static void bindImgSrc(View view, Callback callback) {
        getInstance().bindInternal(view, "Src", callback);
    }
	
	public static void init(Activity context, int viewId, InitCallback callback) {
		getInstance().initInternal(context, viewId, callback);
	}
	
	public static void init(Activity context, int layoutId){
		context.setContentView(layoutId);
		contentView = context.findViewById(android.R.id.content);
		init(context, contentView);
	}
	
	private static void init(Activity context, View view) {
		if(view != null){
			int childrenCount = ((ViewGroup)view).getChildCount();
			String msg = "NativeUiPlugin: Count of children: " + childrenCount;
			Log.i(TAG, msg);
			for(int i=0; i< childrenCount; ++i) {
				View nextChild = ((ViewGroup)view).getChildAt(i);
				if(nextChild instanceof LinearLayout || nextChild instanceof RelativeLayout){
					init(context, nextChild);
				}else { 
					//new
					String nativeId = context.getResources().getResourceEntryName(nextChild.getId());
					mapNativeId2ViewId.put(nativeId, nextChild.getId());
					// end new
					
					if(nextChild instanceof ImageView) {
                    getInstance().initInternal(context, nextChild.getId(), imageViewCallback);
                }else if(nextChild instanceof Switch) {
					getInstance().initInternal(context, nextChild.getId(), switchButtonCallback);
				}else if(nextChild instanceof ToggleButton) {
					getInstance().initInternal(context, nextChild.getId(), toggleButtonCallback);
				}else if(nextChild instanceof RadioButton) {
					getInstance().initInternal(context, nextChild.getId(), radioButtonCallback);
				}else if(nextChild instanceof CheckBox) {
					getInstance().initInternal(context, nextChild.getId(), checkboxCallback);
				}else if(nextChild instanceof Button) {
                    getInstance().initInternal(context, nextChild.getId(), buttonCallback);
                }else if(nextChild instanceof EditText) {
                    getInstance().initInternal(context, nextChild.getId(), editTextCallback);
                }else if(nextChild instanceof TextView) {
					getInstance().initInternal(context, nextChild.getId(), textViewCallback);
				}
				}
			}
		}else{
			String msg = "NativeUiPlugin: ContentView not found";
			Log.e(TAG, msg);
		}
	}
	
	public static void set(int viewId, Object value) {
		getInstance().setInternal(viewId, value);
	}
	
	private void setInternal(int viewId, Object value) {
		String nativeId = getNativeId(viewId);
		Scope scope = getScopeByViewId(viewId);
		String modelExpression = getElementAttribute(nativeId, scope, "Model");
		if (modelExpression != null) {
			evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "='" + value.toString().replaceAll("'", "\\'") + "'");
		}
	}

	public void evaluateScopeExpression(int viewId, String expression) {
		Scope scope = getScopeByViewId(viewId);
		if (scope != null) {
			evaluateScopeExpressionByScopeId(scope.$id, expression);
		}
	}

	protected void evaluateScopeExpressionByScopeId(int scopeId, String expression) {
		invokePermanentCallback("evaluateScopeExpression", scopeId, expression);
	}

	private void invokePermanentCallback(String action, Object... args) {
		JSONObject message = new JSONObject();
		JSONArray jsonArgs = new JSONArray();
		try {
			message.put("action", action);
			if(args != null) {
				for (Object arg : args) {
					if (arg instanceof Callback) {
						int callbackID = getNextCallbackID();
						jsonArgs.put(callbackID);
						callbackMap.put(callbackID, (Callback) arg);
					} else {
						jsonArgs.put(arg);
					}
				}
				message.put("args", jsonArgs);
			}
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

	public static void backButtonPressed() {
		getInstance().invokePermanentCallback("nativeBackButtonPressed");
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
		scopeToUpdate.$$childHead = getExistingScopeOrCreateNewOne($$childHead, transportScopes);
		scopeToUpdate.$$childTail = getExistingScopeOrCreateNewOne($$childTail, transportScopes);
		scopeToUpdate.$$nextSibling = getExistingScopeOrCreateNewOne($$nextSibling, transportScopes);
		
		JSONObject nativeUIData = transportScope.isNull("nativeUI") ? null: transportScope.getJSONObject("nativeUI");
		InitCallback initCallback = null;
		if (nativeUIData != null) {
			JSONArray nativeIds = nativeUIData.names();
			for (int i = 0; i < nativeIds.length(); i++) {
				String nativeId = nativeIds.getString(i);
				Map<String, String> attributesMap = scopeToUpdate.getElementAttributes(nativeId);
				attributesMap.clear();
				JSONObject attributesJsonObject = nativeUIData.getJSONObject(nativeId);
				JSONArray attributeNames = attributesJsonObject.names();
				for (int j = 0; j < attributeNames.length(); j++) {
					String name = attributeNames.getString(j);
					attributesMap.put(name, attributesJsonObject.getString(name));
				}
				initCallback = initCallbacksMap.get(nativeId);
				
				/*if (attributesMap.containsKey("innerBinding")) {
					if (attributesMap.get("innerBinding").isEmpty()) {
						continue;
					}
				}*/
				
				initCallbacksMap.remove(nativeId);
				nativeId2ScopeMap.put(nativeId, scopeToUpdate);
				if (initCallback != null) {
					Message msg = new Message();
					msg.obj = scopeToUpdate;
					int viewId = mapNativeId2ViewId.get(nativeId);	// new
					initCallback.init(viewId, scopeToUpdate);		// new
					//initCallback.init(0, scopeToUpdate);
				}
			}
		}
		return scopeToUpdate;
	}
	
    private static InitCallback imageViewCallback = new NativeUIPlugin.InitCallback() {
        @Override
        public void init(int viewId, Scope scope) {
            final ImageView imageView = (ImageView)contentView.findViewById(viewId);
            if(imageView != null){
                bindImgSrc(imageView, new Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        String[] parts = msg.obj.toString().split("/");
                        String imgName = parts[parts.length - 1];
                        System.out.println(imgName);
                        parts = imgName.split("\\.");
                        imgName = parts[0];
                        System.out.println(imgName);

                        int resId = context.getResources().getIdentifier(imgName, "drawable", context.getApplicationContext().getPackageName());
                        System.out.println("resourceId : " + resId);
                        imageView.setImageResource(resId);
                        return false;
                    }
                });
            }
        }
        ;
    };
    
	private static InitCallback buttonCallback = new NativeUIPlugin.InitCallback() {
        @Override
        public void init(int viewId, Scope scope) {
            final Button button = (Button)contentView.findViewById(viewId);
            if(button != null){
                bindClick(button);
            }
        }
        ;
    };
    
	private static InitCallback textViewCallback = new NativeUIPlugin.InitCallback() {
		@Override
		public void init(int viewId, Scope scope) {
			final TextView textView = (TextView)contentView.findViewById(viewId);
			if(textView != null){
				bind(textView.getId(), new Handler.Callback() {

					@Override
					public boolean handleMessage(Message msg) {
						if (msg.obj != null) {
							String text = textView.getText().toString();
							//String[] newValues = (String[]) msg.obj;
							//String newValue = newValues[0]; 
							textView.setText(msg.obj.toString());
						} else {
							textView.setText("");
						}
						return false;
					}
				});
			}
		}
		;
	};
	
	private static InitCallback editTextCallback = new InitCallback() {
		protected String lastUpdateCausedByMe;
		protected String lastUpdateReceived;
		@Override
		public void init(int viewId, Scope scope) {
			final EditText editText = (EditText)contentView.findViewById(viewId);
			if(editText != null){
				bind(editText.getId(), new Handler.Callback() {

					@Override
					public boolean handleMessage(Message msg) {
						if (msg.obj != null) {
							lastUpdateReceived = msg.obj.toString();
							String oldText = editText.getText().toString();
							if (!oldText.equals(lastUpdateReceived) && !lastUpdateCausedByMe.equals(lastUpdateReceived)) {
								editText.setText(lastUpdateReceived);
							}
						} else {
							editText.setText("");
						}
						return false;
					}
				});
				editText.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						lastUpdateCausedByMe = s.toString();
						if (!lastUpdateCausedByMe.equals(lastUpdateReceived)) {
							set(editText.getId(), lastUpdateCausedByMe);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void afterTextChanged(Editable s) {

					}
				});
			}
		}
	};
	
	private static InitCallback radioButtonCallback = new NativeUIPlugin.InitCallback() {
        @Override
        public void init(int viewId, Scope scope) {
            final RadioButton radioButton = (RadioButton)contentView.findViewById(viewId);
            if(radioButton != null){
                bindRadioClick(radioButton);
            }
        }
        ;
    };
    
    private static InitCallback checkboxCallback = new NativeUIPlugin.InitCallback() {
        @Override
        public void init(int viewId, Scope scope) {
            final CheckBox checkbox = (CheckBox)contentView.findViewById(viewId);
            if(checkbox != null){
                bindRadioClick(checkbox);
            }
        }
        ;
    };
    
    private static InitCallback toggleButtonCallback = new NativeUIPlugin.InitCallback() {
        @Override
        public void init(int viewId, Scope scope) {
            final ToggleButton toggle = (ToggleButton)contentView.findViewById(viewId);
            if(toggle != null){
                bindRadioClick(toggle);
            }
        }
        ;
    };
    
    private static InitCallback switchButtonCallback = new NativeUIPlugin.InitCallback() {
        @Override
        public void init(int viewId, Scope scope) {
            final Switch s = (Switch)contentView.findViewById(viewId);
            if(s != null){
                bindRadioClick(s);
            }
        }
        ;
    };
	
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
	
	private synchronized static int getNextCallbackID() {
		return nextCallbackID++;
	}
	
	public interface InitCallback {
		void init(int viewId, Scope scope);
	}
	
	public boolean getSplashScreen() {
		return splashScreen;
	}
	
	public void setSplashScreen(boolean value) {
		splashScreen = value;
		
		int splashStyleId = context.getResources().getIdentifier("SplashScreen", "style", context.getApplicationContext().getPackageName());
		Dialog splashScreen = new Dialog(context, splashStyleId);
		if (value) {
			int splashLayoutId = context.getResources().getIdentifier("splashscreen", "layout", context.getApplicationContext().getPackageName());
			splashScreen.setContentView(splashLayoutId);
			int splashIconId = context.getResources().getIdentifier("splashScreenIcon", "id", context.getApplicationContext().getPackageName());
			ImageView img = (ImageView) splashScreen.findViewById(splashIconId);
			int iconId = context.getResources().getIdentifier("nxpsplash", "drawable", context.getApplicationContext().getPackageName());
			img.setImageResource(iconId);	
			splashScreen.show();
		} else {
			splashScreen.dismiss();
			splashScreen = null;
		}
		
	}
}
