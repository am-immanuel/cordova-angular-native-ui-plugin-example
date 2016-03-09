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
import org.apache.cordova.splashscreen.SplashScreen;
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

/**
 * This class represents an Android plugin and extends the CordovaPlugin class.
 * It contains a communication bridge between JavaScript. 
 * It is also responsible for the initialization and binding process for all
 * UI elements from invoked activities.
 * 
 * @author: Julius Höfler, Immanuel Scheerer
 * 
 * */
public class NativeUIPlugin extends CordovaPlugin {
	public static String TAG = "NativeUIPlugin";			// tag is used for logging identification
	private static NativeUIPlugin INSTANCE;					// contains the plugin instance
	protected CallbackContext permanentCallback;			// 
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
	
	private static int splashScreen = 1;
	private static Dialog splashScreenDialog = null; 
	
	/**
	 * This constructor initialize the root scope with the plugin instance.
	 * */
	public NativeUIPlugin() {
		$rootScope = new Scope(this);
	}
	
	/**
	 * Initialize the plugin. Therefore saves the context info, the plugin instance.
	 * @param cordova Contains a CordovaInterface object.
	 * @param webview The cordova webview.
	 * */
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		context = cordova.getActivity();				// context is required to launch a new Android Intent
		INSTANCE = this;								// save plugin instance
		scopeMap.put($rootScope.$id, $rootScope);		// put root scope and its id into the scope map
	}
	
	/**
	 * Receives the data from JavaScript's exec function. 
	 * For each action different processes are triggered.
	 * 
	 * @param action Name of the action which should be executed.
	 * @param args Contains the data from the action name.
	 * @param callbackContext Contains the callback from the JavaScript exec function.
	 * @return Returns <code>true</code> if action succeeded successfully.
	 * */
	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
		if ("registerPermanentCallback".equals(action)) {
			permanentCallback = callbackContext;						// save JavaScript exec callback
			PluginResult pluginResult = new PluginResult(Status.OK);	// create a PluginResult instance with status 'OK'
			pluginResult.setKeepCallback(true);							// add a callback instance to PluginResult instance
			callbackContext.sendPluginResult(pluginResult);				// send PluginResult instance over the JavaScript callback to JavaScript
			
		} else if ("updateTransportScopeMap".equals(action)) {
			updateJavaScopes(args.getJSONObject(0));					// pass new scope map through to update process
			
		} else if ("$stateChangeStart".equals(action) || "$stateChangeSuccess".equals(action)) {
			JSONObject toState = args.getJSONObject(0);					// get the new activity state
			JSONObject toParams = args.getJSONObject(1);				// get the parameter from new activity state
			JSONObject fromState = args.getJSONObject(2);				// get the previous activity state
			JSONObject fromParams = args.getJSONObject(3);				// get the parameter from previous activity state
			Log.i(TAG, toState.getString("name"));
			/*if ("app.activity2".equals(toState.getString("name"))) {
				Intent intentActivity2 = new Intent(context, Activity2.class);		// initialize new activity Intent
				intentActivity2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);			// flag checks if requested activity is already on screen
				intentActivity2.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);	// flag checks if requested activity was already created
				context.startActivity(intentActivity2);								// start new activity on current context
			} else if ("app.activity1".equals(toState.getString("name"))) {
				Intent intentActivity1 = new Intent(context, Activity1.class);
				intentActivity1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intentActivity1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				context.startActivity(intentActivity1);
				
				// deactivate splash screen
				if (splashScreen == 2) {
					getInstance().setSplashScreen(splashScreen);
					splashScreen++;
				}	
			}*/
			
			// get activity ID from JavaScript view name from string resource
			int javaActivityId = context.getResources().getIdentifier(toState.getString("name"), "string", context.getApplicationContext().getPackageName());
			// get Java activity name from ID
			String javaActivityName = context.getResources().getString(javaActivityId);
			Class<?> javaActivityClass = null;
			try {
				// get class instance from activity name
				javaActivityClass = Class.forName(javaActivityName);
				
				// initialize new activity Intent
				Intent intentActivity1 = new Intent(context, javaActivityClass);
				
				// flag checks if requested activity is already on screen
				intentActivity1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				
				// flag checks if requested activity was already created
				intentActivity1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				
				// start activity on current context
				context.startActivity(intentActivity1);
					
				// deactivate splash screen
				if (splashScreen == 2) {
					getInstance().setSplashScreen(splashScreen);
					splashScreen++;
				}	
			} catch (ClassNotFoundException e) {
				Log.i(TAG, toState.getString("name") + "or " + javaActivityName + " are incompatible!");
			}

		} else if ("invokeCallback".equals(action)) {
			int callbackId = args.getInt(0);							// get callback ID
			final Object obj = args.get(1);								// get new value
			//final Object obj2 = args.get(2);		// new
			//final Object obj3 = args.get(3);		// new
			final Callback callback = callbackMap.get(callbackId);		// get callback instance from callback ID
			if (callback == null) {
				Log.e(TAG, "Could not find callback with ID " + callbackId);
			} else {
				context.runOnUiThread(new Runnable() {
					
					/**
					 * Because Java needs to interact with the user interface, it has to
					 * run on the main thread. On this Java can send messages to UI elements.
					 * */
					@Override
					public void run() {
						Message m = new Message();
						
						// if the received new value is not empty, put it into message
						if (obj != JSONObject.NULL) {			
							//Object[] msgList= {obj, obj2, obj3};	// new
							//m.obj = msgList;
							m.obj = obj;
						}
						// send message over the UI callback to the corresponding element
						callback.handleMessage(m);
					}
				});
			}
		// if no fitting action name was found, log action and 
		// send a success back to JavaScript
		} else {
			Log.i(TAG, "action: " + action);
			callbackContext.success();
		}
		return true;
	}
	
	/**
	 * Function determine if a TextView needs one or multiple watcher and sends a request
	 * to JavaScript afterwards.
	 * If no watcher is needed set text on the TextView.
	 * 
	 * @param viewId ID from UI element.
	 * @param callback Callback instance from UI element.
	 * */
	private void bindInternal(int viewId, Callback callback) {
		String nativeId = getNativeId(viewId);						// get native ID from view ID
		Scope scope = getScopeByViewId(viewId);						// get scope from view ID
		
		// get desired expression from element with determined native ID
		String expression = getElementAttribute(nativeId, scope, "Bind", "Model");
		
		if (expression != null) {
			
			// no watcher is needed
			if (expression.matches("1#(.*)")) {
				String[] multipleExpressions = expression.split("#");
				expression = multipleExpressions[1];						// get text
				TextView tv = (TextView)contentView.findViewById(viewId);	// get TextView from view ID
				tv.setText(expression);										// set text on TextView
			
			// multiple watcher are needed
			} else if (expression.matches("2#(.*)")) {
				String[] multipleExpressions = expression.split("#");
				for (int i=0; i<multipleExpressions.length-1; i++) {
					invokePermanentCallback("$watch", scope.$id, multipleExpressions[i], callback, expression);
				}
				
			// one watcher is needed
			}  else {
				invokePermanentCallback("$watch", scope.$id, expression, callback);
			}
		}
	}
	
	/**
	 * Function gets an attributes' expression and sends it to a callback instance.
	 * @param view View instance from the UI element.
	 * @param attribute An attribute from an UI element.
	 * @param callback Callback instance from UI element.
	 * */
    private void bindInternal(View view, String attribute, Callback callback) {
        String nativeId = getNativeId(view.getId());								// get native ID from view ID
        Scope scope = getScopeByViewId(view.getId());								// get scope from view ID
        String modelExpression = getElementAttribute(nativeId, scope, attribute);	// get desired expression from element with determined native ID
        
        // if expression is not empty, send a message with the expression to callback instance
        if (modelExpression != null) {
            Message msg = new Message();
            msg.obj = modelExpression;
            callback.handleMessage(msg);
        }
    }

    /**
	 * Function starts initialization process of an UI element or put the
	 * necessary data into an initialization map.
	 * @param context Instance of an activity context.
	 * @param viewId ID of a view from the context.
	 * @param callback Callback instance of an UI element.
	 * */
	private void initInternal(Activity context, int viewId, InitCallback callback) {
		// update context instance
		if (this.context == null) {
			this.context = context;
		}
		// get scope from view ID
		Scope scope = getScopeByViewId(viewId);
		
		// if scope exists start initialization process of the UI element
		if (scope != null) {
			callback.init(viewId, scope);
			
		// put native ID and callback into an init map for a later initialization
		} else {
			String nativeId = context.getResources().getResourceEntryName(viewId);
			initCallbacksMap.put(nativeId, callback);
		}
	}

	/**
	 * Function maps a view ID into a scope.
	 * @param viewId ID from a view element.
	 * @return Returns a scope.
	 * */
	public Scope getScopeByViewId(int viewId) {
		// get scope from map entry
		Scope result = viewId2ScopeMap.get(viewId);
		
		// if first map entry is empty, get scope from another map
		if (result == null) {
			String nativeId = getNativeId(viewId);
			result = nativeId2ScopeMap.get(nativeId);
			
			// add view ID and scope into the first map
			if (result != null) {
				viewId2ScopeMap.put(viewId, result);
			}
		}
		return result;
	}
	
	/**
	 * Function maps a view ID to a native ID.
	 * @param viewId ID from a view element.
	 * @return Returns native ID as a string.
	 * */
	public String getNativeId(int viewId) {
		// get native ID from a map entry
		String result = nativeIdMap.get(viewId);
		
		// if map entry is empty, get native ID from the context instance
		if (result == null) {
			if (context != null) {
				result = context.getResources().getResourceEntryName(viewId);
				
				// put view ID and native ID into the native ID map
				nativeIdMap.put(viewId, result);
			}
		}
		return result;
	}
	
	/**
	 * Get the value from the 'ng-click' attribute and start evaluation process.
	 * @param viewId ID from a view element.
	 * */
	/*private void clickInternal(int viewId) {
		String nativeId = getNativeId(viewId);
		Scope scope = getScopeByViewId(viewId);
		String clickExpression = getElementAttribute(nativeId, scope, "Click");
		if (clickExpression != null) {
			evaluateScopeExpressionByScopeId(scope.$id, clickExpression);
		}
	}*/
	
	/**
	 * Pass parameter through to the private radioClick function.
	 * @param viewId ID from a view element.
	 * */
	// new
	/*public static void radioClick(int viewId) {
		getInstance().radioClickInternal(viewId);
	}*/
	
	/**
	 * Pass parameter through to the private checkClick function.
	 * @param view View instance from a view element.
	 * */
	public static void checkClick(View view) {
		getInstance().checkClickInternal(view);
	}
	
	/**
	 * Get the values from 'ng-model' and 'value' attributes and start evaluation process.
	 * @param viewId ID from a view element.
	 * */
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
	
	/**
	 * Get the needed values from elements' attributes and start evaluation process.
	 * Function can be executed for the android systems 'ICE_CREAM_SANDWICH' or higher
	 * due to using the switch widget.
	 * @param view View instance from a view element.
	 * */
	// new
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void checkClickInternal(View view) {
		String nativeId = getNativeId(view.getId());		// get native ID from view ID
		Scope scope = getScopeByViewId(view.getId());		// get scope from view ID
		
		// new
		// get value from checked if the view instance is a compound button
		Boolean checked = null;
		if (view instanceof CompoundButton) {
			checked = ((CompoundButton) view).isChecked();
		}
		
		// get the values of the needed element attributes
		String modelExpression = getElementAttribute(nativeId, scope, "Model");
		String valueExpression = getElementAttribute(nativeId, scope, "value");		// new
		String trueExpression = getElementAttribute(nativeId, scope, "TrueValue");
		String falseExpression = getElementAttribute(nativeId, scope, "FalseValue");
		String clickExpression = getElementAttribute(nativeId, scope, "Click");		// new
		
		if (modelExpression != null) {
			// evaluation for elements with a 'value' attribute
			if (valueExpression != null) {		// new
				evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "='" + valueExpression + "'");
				
			// evaluation for elements with a 'ng-true-value' attribute
			} else if (checked && (trueExpression != null)) {
				evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "=" + trueExpression + "");
				
			// evaluation for elements with a 'ng-false-value' attribute
			} else if (!checked && (falseExpression != null)) {
				evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "=" + falseExpression + "");
				
			// evaluation for elements with a checked value
			} else if (checked != null){
				evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "=" + checked + "");
			}	
		}
		
		// new
		// evaluation for elements with a 'ng-click' attribute
		if (clickExpression != null) {
			evaluateScopeExpressionByScopeId(scope.$id, clickExpression);
		}
	}
	
	/**
	 * Calls function with the same parameter plus a fourth with a 'null' value.
	 * @param nativeId ID from a view element.
	 * @param scope A scope instance.
	 * @param ngPostfix Attribute name of an element without prefix 'ng'.
	 * @return Returns the value of an attribute from an element.
	 * */
	private String getElementAttribute(String nativeId, Scope scope, String ngPostfix) {
		return getElementAttribute(nativeId, scope, ngPostfix, null);
	}
	
	/**
	 * Function searches for an elements' attribute on the given scope with the also given native ID
	 * and gets the value from that attribute. 
	 * @param nativeId ID from a view element.
	 * @param scope A scope instance.
	 * @param ngPostfix Attribute name of an element without prefix 'ng'.
	 * @param ngPostfix2 Attribute name of an element without prefix 'ng'.
	 * @return Returns the value of an attribute from an element.
	 * */
	private String getElementAttribute(String nativeId, Scope scope, String ngPostfix, String ngPostfix2) {
		if (scope != null) {
			// get the value from an attribute with a prefix 'ng' a the given postfix
			String expression = scope.getElementAttribute(nativeId, "ng" + ngPostfix);
			if (expression != null) {
				return expression;
			}
			// get the value from an attribute with a prefix 'dataNg' a the given postfix
			expression = scope.getElementAttribute(nativeId, "dataNg" + ngPostfix);
			if (expression != null) {
				return expression;
			}
			
			// new
			// get the value from an attribute with the given postfix
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
			
			// return null if ngPostfix has no hit and ngPostfix2 is null
			if (ngPostfix2 == null) {
				return null;
			}
			// get the value from an attribute with a prefix 'ng' a the given postfix2
			expression = scope.getElementAttribute(nativeId, "ng" + ngPostfix2);
			if (expression != null) {
				return expression;
			}
			// get the value from an attribute with a prefix 'dataNg' a the given postfix2
			expression = scope.getElementAttribute(nativeId, "dataNg" + ngPostfix2);
			if (expression != null) {
				return expression;
			}
			
			// get the value from an attribute innerBindingHTML
			expression = scope.getElementAttribute(nativeId, "innerBindingHTML");
			
			// only necessary if an error occurs in an element with ng-bind-template
			if (expression == null) {
				expression = scope.getElementAttribute(nativeId, "ngBindTemplate");
			}
			
			// seperate the '{{*}}' values from the expression
			if (expression != null) {
				Pattern p = Pattern.compile("\\{\\{\\w+\\}\\}");
				Matcher m = p.matcher(expression);
				String bindExpression = "";
				
				// concatenate the '{{*}}' values and split them with the special character '#'
				while(m.find()) {
					bindExpression = bindExpression + m.group().replace("{", "").replace("}", "") + "#" ; 
				}
				
				// add a '1' to the expression if there is only one '{{*}}' value
				// add a '2' and the total raw text if there are multiple '{{*}}' values
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
	
	/**
	 * Pass view ID through to internal private function.
	 * @param viewId ID from a view element.
	 * */
	/*public static void click(int viewId) {
		getInstance().clickInternal(viewId);
	}*/
	
	/**
	 * Pass view ID and callback through to internal private function.
	 * @param viewId ID from a view element.
	 * @param callback Callback instance from a view element.
	 * */
	public static void bind(int viewId, Callback callback) {
		getInstance().bindInternal(viewId, callback);
	}
	
	/**
	 * Pass view through to internal private function.
	 * @param view View instance from a UI element.
	 * */
    public static void bindClick(final View view) {
        getInstance().bindInternal(view, "Click", new Callback() {
        	
        	/**
        	 * Receive messages and set click listener.
        	 * @param msg Message which contains data for a UI element.
        	 * @return Message is passed to Handler implementation.
        	 * */
			@Override
			public boolean handleMessage(Message msg) {
				view.setOnClickListener(new View.OnClickListener() {
					
					/** Receives a click from user interaction
					 * @param v View instance from a UI element.
					 * */
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
    
    /**
	 * Pass view through to internal private function.
	 * @param view View instance from a UI element.
	 * */
    // TODO: merge bindClick and bindRadioClick
    // new
    public static void bindRadioClick(final View view) {
        getInstance().bindInternal(view, "Model", new Callback() {
        	
        	/**
        	 * Receive messages and set click listener.
        	 * @param msg Message which contains data for a UI element.
        	 * @return Message is passed to Handler implementation.
        	 * */
			@Override
			public boolean handleMessage(Message msg) {
				view.setOnClickListener(new View.OnClickListener() {
					
					/** Receives a click from user interaction.
					 * @param v View instance from a UI element.
					 * */
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
    
    /**
	 * Pass view and callback through to internal private function.
	 * Add parameter 'src' to internal function.
	 * @param view View instance from a UI element.
	 * @param callback Callback instance from a view element.
	 * */
    public static void bindImgSrc(View view, Callback callback) {
        getInstance().bindInternal(view, "Src", callback);
    }
	
    /**
	 * Pass context, view ID and callback through to internal private function.
	 * @param context Context of an activity
	 * @param viewId ID from a view element.
	 * @param callback Callback instance from a view element.
	 * */
	public static void init(Activity context, int viewId, InitCallback callback) {
		getInstance().initInternal(context, viewId, callback);
	}
	
	/**
	 * This function is called in an activity to initialize the UI elements.
	 * @param context Context instance of an activity.
	 * @param layoutId Contains the layout identification of an activity.
	 * */
	public static void init(Activity context, int layoutId){
		context.setContentView(layoutId);							// determine the layout of the activity
		contentView = context.findViewById(android.R.id.content);	// get the content view elements
		init(context, contentView);			// invokes a private function with current context and the content view elements
		
		// set splash screen
		if (splashScreen == 1) {
			getInstance().setSplashScreen(splashScreen);
			splashScreen++;
		}
	}
	
	/**
	 * This function starts for all view elements which are not a view group an initialization
	 * process. Furthermore all view elements is given a fitting callback to their type.
	 * @param context Context instance of an activity.
	 * @param view Contains the content view elements of an activity.
	 * */
	private static void init(Activity context, View view) {
		
		if(view != null){
			// get number of all child elements from content view
			int childrenCount = ((ViewGroup)view).getChildCount();
			String msg = "NativeUiPlugin: Count of children: " + childrenCount;
			Log.i(TAG, msg);
			
			// go through all child elements and initialize them
			for(int i=0; i<childrenCount; ++i) {
				// get current child element
				View nextChild = ((ViewGroup)view).getChildAt(i);
				
				// invoke init recursive if child element has childs as well
				if(nextChild instanceof ViewGroup){
					init(context, nextChild);
					
				} else { 
					//new
					// add native ID and corresponding view element into a map
					String nativeId = context.getResources().getResourceEntryName(nextChild.getId());
					mapNativeId2ViewId.put(nativeId, nextChild.getId());
					// end new
					
					// start initialization process for all elements from a supporting class
					// according to the class the view element is given a different type of callback
					if(nextChild instanceof ImageView) {
						getInstance().initInternal(context, nextChild.getId(), imageViewCallback);
	                } else if(nextChild instanceof Switch) {
						getInstance().initInternal(context, nextChild.getId(), switchButtonCallback);
					} else if(nextChild instanceof ToggleButton) {
						getInstance().initInternal(context, nextChild.getId(), toggleButtonCallback);
					} else if(nextChild instanceof RadioButton) {
						getInstance().initInternal(context, nextChild.getId(), radioButtonCallback);
					} else if(nextChild instanceof CheckBox) {
						getInstance().initInternal(context, nextChild.getId(), checkboxCallback);
					} else if(nextChild instanceof Button) {
	                    getInstance().initInternal(context, nextChild.getId(), buttonCallback);
	                } else if(nextChild instanceof EditText) {
	                    getInstance().initInternal(context, nextChild.getId(), editTextCallback);
	                } else if(nextChild instanceof TextView) {
						getInstance().initInternal(context, nextChild.getId(), textViewCallback);
					}
				}
			}
		// log message that content view is empty	
		} else {
			String msg = "NativeUiPlugin: ContentView not found";
			Log.e(TAG, msg);
		}
	}
	
	/**
	 * Pass view ID and value through to an internal private function.
	 * @param viewId ID from a view element.
	 * @param value This object contains a value which should be set.
	 * */
	public static void set(int viewId, Object value) {
		getInstance().setInternal(viewId, value);
	}
	
	/**
	 * This function determines the model expression which should be set with the new value.
	 * @param viewId ID from a view element.
	 * @param value This object contains a value which should be set.
	 * */
	private void setInternal(int viewId, Object value) {
		String nativeId = getNativeId(viewId);			// get native ID from view ID
		Scope scope = getScopeByViewId(viewId);			// get scope from view ID
		
		// get the value of the element attribute postfix 'Model'
		String modelExpression = getElementAttribute(nativeId, scope, "Model");
		
		// send expression with the new value for an evaluation
		if (modelExpression != null) {
			evaluateScopeExpressionByScopeId(scope.$id, modelExpression + "='" + value.toString().replaceAll("'", "\\'") + "'");
		}
	}

	/**
	 * Calls evaluation process for given expression.
	 * @param viewId ID from a view element.
	 * @param expression String which should be evaluated.
	 * */
	@Deprecated
	public void evaluateScopeExpression(int viewId, String expression) {
		// get scope from view ID
		Scope scope = getScopeByViewId(viewId);
		
		// send expression for an evaluation
		if (scope != null) {
			evaluateScopeExpressionByScopeId(scope.$id, expression);
		}
	}

	/**
	 * Passes scope ID and expression through to a function which will send the data to JavaScript.
	 * Furthermore the action name is added as a parameter.
	 * @param scopeId Number of the scope identification.
	 * @param expression String which should be evaluated.
	 * */
	protected void evaluateScopeExpressionByScopeId(int scopeId, String expression) {
		invokePermanentCallback("evaluateScopeExpression", scopeId, expression);
	}

	/**
	 * This function collects the given data and send them as a message to JavaScript.
	 * @param action Name which distinguish the different actions.
	 * @param args Contains data which should be sent to JavaScript.
	 * */
	private void invokePermanentCallback(String action, Object... args) {
		JSONObject message = new JSONObject();
		JSONArray jsonArgs = new JSONArray();
		try {
			// put the action name into the message
			message.put("action", action);
			
			if(args != null) {
				// run through all arguments and put them into an array
				for (Object arg : args) {
					
					// put the callback instance also into the callback map for usage
					// after receiving data from JavaScript back
					if (arg instanceof Callback) {
						int callbackID = getNextCallbackID();
						jsonArgs.put(callbackID);
						callbackMap.put(callbackID, (Callback) arg);
					} else {
						// put the current argument into the array
						jsonArgs.put(arg);
					}
				}
				// put all arguments into the message object
				message.put("args", jsonArgs);
			}
			PluginResult result = new PluginResult(Status.OK, message);		// put status and message into the sending instance
			result.setKeepCallback(true);									// add the Java callback instance
			
			// send all data over the permanent callback instance to Java
			// this data will be received by the success function from the 'registerPermanentCallback' action
			getInstance().permanentCallback.sendPluginResult(result);
			
		} catch (JSONException e) {
			throw new RuntimeException("Could not construct JSON message", e);
		}
	}
	
	/**
	 * Gets back the root scope
	 * @return The AngularJS root scope.
	 */
	public Scope getRootScope() {
		return $rootScope;
	}
	
	/**
	 * Passes on the instance of the plugin. If no instance exists this function will create an instance.
	 * @return The instance of the plugin.
	 * */
	public static NativeUIPlugin getInstance() {
		// create a new plugin instance
		if (INSTANCE == null) {
			String msg = "NativeUIPlugin wasn't initialized yet";
			Log.e(TAG, msg);
			return new NativeUIPlugin();
		}
		// the 'registerPermanentCallback' action was not executed yet
		if (INSTANCE.permanentCallback == null) {
			String msg = "NativeUIPlugin: Permanent callback hasn't been registered yet";
			Log.e(TAG, msg);
		}
		return INSTANCE;
	}

	/**
	 * This function is triggered after the user has pushed the android device back button.
	 * Therefore the action name 'nativeBackButtonPressed' is pushed to a function which
	 * send it to JavaScript.
	 * */
	public static void backButtonPressed() {
		getInstance().invokePermanentCallback("nativeBackButtonPressed");
	}
	
	/**
	 * 
	 * @param scopeId
	 * @param method
	 * @param args
	 * */
	@Deprecated
	protected void invokeScopeMethod(int scopeId, String method,
			Object... args) {
		invokePermanentCallback("invokeMethod", method, args);
	}
	
	/**
	 * Function divides the received scope map into single scopes and calls a function
	 * which updates them individual.
	 * @param transportScopes An object which contains the scope map from JavaScript.
	 * */
	@SuppressWarnings("unchecked")
	private void updateJavaScopes(JSONObject transportScopes) throws JSONException {
		if (transportScopes == null)
			return;

		// update the information for every received scope
		Iterator<String> iter = transportScopes.keys();
		while (iter.hasNext()) {
			updateJavaScope(Integer.parseInt(iter.next()), transportScopes);
		}
	}
	
	/**
	 * This function updates the Java scope hierarchy with all missing scopes and native UI information.
	 * Furthermore starts initialization process for all non-initialized view elements.
	 * @param id Scope identification number.
	 * @param transportScopes An object which contains the scope map from JavaScript.
	 * @return Returns an updated scope object.
	 * */
	private Scope updateJavaScope(int id, JSONObject transportScopes) throws JSONException {
		// get transport scope from scope ID
		JSONObject transportScope = transportScopes.getJSONObject("" + id);
		
		// get scope object
		Scope scopeToUpdate = getExistingScopeOrCreateNewOne(id, transportScopes);
		
		// get parent ID if a parent scope exists
		Integer $parent = transportScope.isNull("$parent") ? null : transportScope.getInt("$parent");
		
		// check if both parent IDs are consistent
		if ($parent != null && scopeToUpdate.getParent().$id != $parent) {
			throw new IllegalStateException("parent relation is inconsistent");
		}
		
		// check if transport scope object occupies scope hierarchy information
		// if so, get all IDs and hierarchy informations about child and sibling scopes
		Integer $$childHead = transportScope.isNull("$$childHead") ? null : transportScope.getInt("$$childHead");
		Integer $$childTail = transportScope.isNull("$$childTail") ? null : transportScope.getInt("$$childTail");
		Integer $$nextSibling = transportScope.isNull("$$nextSibling") ? null : transportScope.getInt("$$nextSibling");
		scopeToUpdate.$$childHead = getExistingScopeOrCreateNewOne($$childHead, transportScopes);
		scopeToUpdate.$$childTail = getExistingScopeOrCreateNewOne($$childTail, transportScopes);
		scopeToUpdate.$$nextSibling = getExistingScopeOrCreateNewOne($$nextSibling, transportScopes);
		
		// get all native UI data from the scope if existing
		JSONObject nativeUIData = transportScope.isNull("nativeUI") ? null: transportScope.getJSONObject("nativeUI");
		InitCallback initCallback = null;
		
		if (nativeUIData != null) {
			
			// put all native IDs into an array
			JSONArray nativeIds = nativeUIData.names();
			
			// run through all native IDs to put all attribute information into a map and 
			// start the initialization process of the corresponding view element
			for (int i = 0; i < nativeIds.length(); i++) {
				// get current native ID
				String nativeId = nativeIds.getString(i);
				
				// TODO: check clear()
				// create a attribute map which contains all attribute informations from the element with this native ID 
				Map<String, String> attributesMap = scopeToUpdate.getElementAttributes(nativeId);
				attributesMap.clear();
				
				// get the object contains all informations from the element with this native ID 
				JSONObject attributesJsonObject = nativeUIData.getJSONObject(nativeId);
				
				// get all attribute names
				JSONArray attributeNames = attributesJsonObject.names();
				
				// run through all attributes and put the name and the value into the attribute map
				for (int j = 0; j < attributeNames.length(); j++) {
					String name = attributeNames.getString(j);
					attributesMap.put(name, attributesJsonObject.getString(name));
				}
				
				// get the corresponding callback instance from the init callback map
				initCallback = initCallbacksMap.get(nativeId);
				
				// remove the entry
				initCallbacksMap.remove(nativeId);
				
				// put the native ID and the scope object as a mapping entry into a map
				nativeId2ScopeMap.put(nativeId, scopeToUpdate);
				
				// if the native IDs object has to be initialized start it over the callbacks' init function
				if (initCallback != null) {
					//Message msg = new Message();
					//msg.obj = scopeToUpdate;
					
					// get view ID from native ID
					int viewId = mapNativeId2ViewId.get(nativeId);	// new
					
					// start init function for a view element over its callback
					initCallback.init(viewId, scopeToUpdate);		// new
					//initCallback.init(0, scopeToUpdate);
				}
			}
		}
		return scopeToUpdate;
	}
	
	// initialize a callback instance for a ImageView widget component
    private static InitCallback imageViewCallback = new NativeUIPlugin.InitCallback() {
    	
    	/**
    	 * Implements initialization function for a ImageView element.
    	 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
    	 * */
        @Override
        public void init(int viewId, Scope scope) {
        	// get ImageView instance from view ID
            final ImageView imageView = (ImageView)contentView.findViewById(viewId);
            
            // start binding process
            if(imageView != null){
                bindImgSrc(imageView, new Callback() {
                	
                	/**
                	 * Receive messages and set image source to ImageView instance.
                	 * @param msg Message which contains data for a UI element.
                	 * @return Message is passed to Handler implementation.
                	 * */
                    @Override
                    public boolean handleMessage(Message msg) {
                    	// split image path
                        String[] parts = msg.obj.toString().split("/");
                        
                        // get image name
                        String imgName = parts[parts.length - 1];
                        System.out.println(imgName);
                        parts = imgName.split("\\.");
                        imgName = parts[0];
                        System.out.println(imgName);

                        // get source from image 
                        int resId = context.getResources().getIdentifier(imgName, "drawable", context.getApplicationContext().getPackageName());
                        System.out.println("resourceId : " + resId);
                        
                        // set source in ImageView
                        imageView.setImageResource(resId);
                        
                        return false;
                    }
                });
            }
        }
        ;
    };
    
    // initialize a callback instance for a Button widget component
	private static InitCallback buttonCallback = new NativeUIPlugin.InitCallback() {
		
		/**
		 * Implements initialization function for a Button element.
		 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
		 * */
        @Override
        public void init(int viewId, Scope scope) {
        	// get Button instance from view ID
            final Button button = (Button)contentView.findViewById(viewId);
            
            // start binding process
            if(button != null){
                bindClick(button);
            }
        }
        ;
    };
    
    // initialize a callback instance for a TextView widget component
	private static InitCallback textViewCallback = new NativeUIPlugin.InitCallback() {
		
		/**
		 * Implements initialization function for a TextView element.
		 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
		 * */
		@Override
		public void init(int viewId, Scope scope) {
			// get TextView instance from view ID
			final TextView textView = (TextView)contentView.findViewById(viewId);
			
			// start binding process
			if(textView != null){
				bind(textView.getId(), new Handler.Callback() {

					/**
					 * Receive messages and set the TextView element with the message text.
					 * @param msg Message which contains data for a UI element.
					 * @return Message is passed to Handler implementation.
					 * */
					@Override
					public boolean handleMessage(Message msg) {
						if (msg.obj != null) {
							// get old text from TextView
							String text = textView.getText().toString();
							//String[] newValues = (String[]) msg.obj;
							//String newValue = newValues[0]; 
							
							// update TextView with text from received message
							textView.setText(msg.obj.toString());
							
						// if message is null set TextView text to empty	
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
	
	// initialize a callback instance for a EditText widget component
	private static InitCallback editTextCallback = new InitCallback() {
		protected String lastUpdateCausedByMe;
		protected String lastUpdateReceived;
		
		/**
		 * Implements initialization function for a EditText element.
		 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
		 * */
		@Override
		public void init(int viewId, Scope scope) {
			// get EditText instance from view ID
			final EditText editText = (EditText)contentView.findViewById(viewId);
			
			// start binding process
			if(editText != null){
				bind(editText.getId(), new Handler.Callback() {

					/**
					 * Receive messages and set the EditText element with the message text.
					 * @param msg Message which contains data for a UI element.
					 * @return Message is passed to Handler implementation.
					 * */
					@Override
					public boolean handleMessage(Message msg) {
						if (msg.obj != null) {
							// get text from message
							lastUpdateReceived = msg.obj.toString();
							
							// save old text from EditText
							String oldText = editText.getText().toString();
							
							// update EditText text if old text is different to new one
							if (!oldText.equals(lastUpdateReceived) && !lastUpdateCausedByMe.equals(lastUpdateReceived)) {
								editText.setText(lastUpdateReceived);
							}
						// if message is null set EditText text to empty
						} else {
							editText.setText("");
						}
						return false;
					}
				});
				// add event listener to recognize text changes
				editText.addTextChangedListener(new TextWatcher() {

					/**
					 * Triggered if input text is changed.
					 * @param s Character sequence of edited characters.
					 * @param start Start number.
					 * @param before Last number from count.
					 * @param count Number of characters.
					 * */
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						lastUpdateCausedByMe = s.toString();
						if (!lastUpdateCausedByMe.equals(lastUpdateReceived)) {
							set(editText.getId(), lastUpdateCausedByMe);
						}
					}

					/**
					 * Triggered before input text is changed.
					 * @param s Character sequence before the last edited character.
					 * @param start Start number.
					 * @param count Number of characters.
					 * @param after Number of characters after typing.
					 * */
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					
					}

					/**
					 * Triggered after input text is changed.
					 * @param s Contains character sequence of the edited characters.
					 * */
					@Override
					public void afterTextChanged(Editable s) {
	
					}
				});
			}
		}
	};
	
	// initialize a callback instance for a RadioButton widget component
	private static InitCallback radioButtonCallback = new NativeUIPlugin.InitCallback() {
		
		/**
		 * Implements initialization function for a RadioButton element.
		 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
		 * */
        @Override
        public void init(int viewId, Scope scope) {
        	// get RadioButton instance from view ID
            final RadioButton radioButton = (RadioButton)contentView.findViewById(viewId);
            
            // start binding process
            if(radioButton != null){
                bindRadioClick(radioButton);
            }
        }
        ;
    };
    
    // initialize a callback instance for a CheckBox widget component
    private static InitCallback checkboxCallback = new NativeUIPlugin.InitCallback() {
    	
    	/**
    	 * Implements initialization function for a CheckBox element.
    	 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
    	 * */
        @Override
        public void init(int viewId, Scope scope) {
        	// get CheckBox instance from view ID
            final CheckBox checkbox = (CheckBox)contentView.findViewById(viewId);
            
            // start binding process
            if(checkbox != null){
                bindRadioClick(checkbox);
            }
        }
        ;
    };
    
    // initialize a callback instance for a ToggleButton widget component
    private static InitCallback toggleButtonCallback = new NativeUIPlugin.InitCallback() {
    	
    	/**
    	 * Implements initialization function for a ToggleButton element.
    	 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
    	 * */
        @Override
        public void init(int viewId, Scope scope) {
        	// get ToggleButton instance from view ID
            final ToggleButton toggle = (ToggleButton)contentView.findViewById(viewId);
            
            // start binding process
            if(toggle != null){
                bindRadioClick(toggle);
            }
        }
        ;
    };
    
    // initialize a callback instance for a Switch widget component
    private static InitCallback switchButtonCallback = new NativeUIPlugin.InitCallback() {
    	
    	/**
    	 * Implements initialization function for a Switch element.
    	 * @param viewId ID from a view element.
    	 * @param scope A scope instance.
    	 * */
        @Override
        public void init(int viewId, Scope scope) {
        	// get Switch instance from view ID
            final Switch s = (Switch)contentView.findViewById(viewId);
            
            // start binding process
            if(s != null){
                bindRadioClick(s);
            }
        }
        ;
    };
	
    /**
	 * This function check if a scope with the receiving ID exists and returns it if it exists.
	 * If there is no scope get the transport scope object from JavaScipt and create a new
	 * Java scope object and put them into the scope map
	 * Furthermore call this function recursively to get all parent scopes.
	 * @param id Scope identification number.
	 * @param transportScopes An object which contains the scope map from JavaScript.
	 * @return A scope object.
	 * */
	private Scope getExistingScopeOrCreateNewOne(Integer id, JSONObject transportScopes) throws JSONException {
		// exit condition
		if (id == null) 
			return null;
		
		// get scope if it exists already in the Java scope map
		Scope scopeToUpdate = scopeMap.get(id);
		
		// if not in scope map, get scope object from scope ID and put into scope map
		if (scopeToUpdate == null) {
			// get scope ID as string
			String strId = "" + id;
			if (transportScopes.isNull(strId)) {
				throw new IllegalStateException("Scope with ID " + id + " does not exist and is not provided from JavaScript");
			}
			
			// get transport scope object from scope ID
			JSONObject transportScope = transportScopes.getJSONObject("" + id);
			
			// call function recursive to determine all parent scopes to get scope hierarchy
			Scope parentScope = getExistingScopeOrCreateNewOne(transportScope.getInt("$parent"), transportScopes);
			
			// create new java scope object
			scopeToUpdate = new Scope(parentScope, id);
			
			// put scope ID and scope into the scope map
			scopeMap.put(id, scopeToUpdate);
		}
		return scopeToUpdate;
	}
	
	/**
	 * This function make sure that all callbacks which request for a callback ID
	 * get different IDs.
	 * @return Callback ID number.
	 * */
	private synchronized static int getNextCallbackID() {
		return nextCallbackID++;
	}
	
	/**
	 * Provides an initial callback plus init function for a UI element.
	 * */
	public interface InitCallback {
		/**
		 * Binds the UI element.
		 * @param viewId The ID of the UI element.
		 * @param scope Scope in which the UI element is located.
		 * */
		void init(int viewId, Scope scope);
	}
	
	/**
	 * Gets the boolean value of the splashscreen variable.
	 * @return Returns <code>true</code> if splash screen is enabled.
	 * */
	public int getSplashScreen() {
		return splashScreen;
	}
	
	/**
	 * Set and dismiss the splash screen.
	 * @param value If <code>true</code> show splash screen, else dismiss it.
	 * */
	public void setSplashScreen(int value) {
		splashScreen = value;
		
		// get splash screen style ID from /res/values directory
		int splashStyleId = context.getResources().getIdentifier("SplashScreen", "style", context.getApplicationContext().getPackageName());
		
		// create dialog as splash screen
		if (splashScreenDialog == null) {
			splashScreenDialog = new Dialog(context, splashStyleId);
		}	
		if (value == 1) {
			// get layout ID from /res/layout directory and content view with it
			int splashLayoutId = context.getResources().getIdentifier("splashscreen", "layout", context.getApplicationContext().getPackageName());
			splashScreenDialog.setContentView(splashLayoutId);
			
			// get splash screen icon and define an image with it
			int splashIconId = context.getResources().getIdentifier("splashScreenIcon", "id", context.getApplicationContext().getPackageName());
			ImageView img = (ImageView) splashScreenDialog.findViewById(splashIconId);
			
			// get source from splash screen image
			int iconId = context.getResources().getIdentifier("nxpsplash", "drawable", context.getApplicationContext().getPackageName());
			img.setImageResource(iconId);	
			
			// show splash screen
			splashScreenDialog.show();
			
		} else {
			// dismiss dialog and remove splash screen
			splashScreenDialog.dismiss();
			splashScreenDialog = null;
		}
		
	}
}
