package de.apparentmedia.cordova;

import java.util.HashMap;
import java.util.Map;

import android.os.Handler;


/**
 * Core class for the AngularJS scope bridge between
 * JavaScript and Java.
 *  
 * @author Immanuel Scheerer (apparent media), Julius Höfler
 */
public class Scope {
	private Scope parent;
	private NativeUIPlugin plugin;
	public final int $id;
	public Scope $$childHead;
	public Scope $$childTail;
	public Scope $$nextSibling;
	private Map<String, Map<String, String>> nativeId2Attributes;
	
	/**
	 * Creates new scope as child scope of the given parent scope.
	 * 
	 * @param parentScope The parent scope for which a new child should be created.
	 * @param id The identification of the new child scope.
	 */
	public Scope(Scope parentScope, int id) {
		// throws exception if there is no parent scope
		if (parentScope == null) {
			throw new RuntimeException("parent scope is mandatory");
		}
		this.parent = parentScope;				// save parent scope in child scope
		this.$id = id;							// set scope id with passed id
		this.plugin = parentScope.plugin;		// save plugin instance
	}
	
	/**
	 * This constructor is package protected to avoid root scope generation by accident.
	 * Only used once to generate the root scope.
	 * 
	 * @param plugin The NativeUIPlugin instance.
	 */
	protected Scope(NativeUIPlugin plugin) {
		this.plugin = plugin;					// save plugin instance
		this.$id = 1;							// assign the root scope the first (lowest) scope id
	}
	
	/**
	 * Called to get the parent scope from the current scope.
	 * @return The parent scope of this scope.
	 */
	public Scope getParent() {
		return parent;
	}
	
	/**
	 * Called to get the root scope from this application.
	 * @return Return the root scope.
	 */
	public Scope getRoot() {
		// get parent scope until root scope is reached which has no parent
		Scope parent = getParent();
		return parent == null ? this : parent.getRoot();
	}
	
	/**
	 * Called to evaluate if the current scope is the root scope.
	 * @return Returns <code>true</code> when this scope is the root scope.
	 */
	public boolean isRoot() {
		return this == getRoot();
	}
	
	/**
	 * Invokes a NativeUIPlugin function to evaluate an expression
	 * on the current scope id.
	 * @param expression A string that should be evaluated.
	 * */
	public void evaluateExpression(String expression) {
		plugin.evaluateScopeExpressionByScopeId(this.$id, expression);
	}
	
	/**
	 * Called after an event was triggered and pass it through to plugin. 
	 * @param eventName Name of the triggered event.
	 * @param callback Callback from the triggered UI element.
	 * */
	@Deprecated
	public void $on(String eventName, Handler.Callback callback) {
		plugin.invokeScopeMethod(this.$id, "$on", eventName, callback);
	}
	
	/**
	 * Called after a watcher was triggered and pass it through to plugin.
	 * @param expression Watched expression string.
	 * @param callback Callback from the watched UI element.
	 * */
	@Deprecated
	public void $watch(String expression, Handler.Callback callback) {
		plugin.invokeScopeMethod(this.$id, "$watch", expression, callback);
	}
	
	/**
	 * This Function puts all attributes from a given native ID into a Map and gets this one back.
	 * @param nativeId Contains the native ID of a UI element.
	 * @return Returns a Map with native ID as key and an array of all corresponding UI element attributes.
	 * */
	public Map<String, String> getElementAttributes(String nativeId) {
		// if empty fill Map with a new HashMap
		if (nativeId2Attributes == null) {
			nativeId2Attributes = new HashMap<String, Map<String,String>>(3);
		}
		// get Attributes from native ID
		Map<String, String> result = nativeId2Attributes.get(nativeId);
		if (result == null) {
			result = new HashMap<String, String>(3);
			nativeId2Attributes.put(nativeId, result);
		}
		return result;
	}

	/**
	 * This Function gets the value of an attribute from a given native ID back as a string.
	 * @param nativeId Contains the native ID of a UI element.
	 * @param attributeName Name of the attributes' desired value.
	 * @return Return the value of the attribute.
	 * */
	public String getElementAttribute(String nativeId, String attributeName) {
		return getElementAttributes(nativeId).get(attributeName);
	}
}
