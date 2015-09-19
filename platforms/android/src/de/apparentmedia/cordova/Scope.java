package de.apparentmedia.cordova;

import java.util.HashMap;
import java.util.Map;

import android.os.Handler;


/**
 * Core class for the AngularJS scope bridge between
 * JavaScript and Java.
 *  
 * @author Immanuel Scheerer, apparent media
 */
public class Scope {
	private Scope parent;
	private NativeUIPlugin plugin;
	public final int $id;
	public Scope $$childHead;
	public Scope $$childTail;
	public Scope $$nextSibling;
	public String nativeId;
	private Map<String, String> attributes = new HashMap<String, String>(3);
	
	/**
	 * Create new scope as child scope of the given parent scope.
	 * @param parentScope The parent scope for which a new child should be created.
	 */
	public Scope(Scope parentScope, int id) {
		if (parentScope == null) {
			throw new RuntimeException("parent scope is mandatory");
		}
		this.parent = parentScope;
		this.$id = id;
		this.plugin = parentScope.plugin;
	}
	
	/**
	 * This constructor is package protected to avoid root scope generation by accident.
	 */
	protected Scope(NativeUIPlugin plugin) {
		this.plugin = plugin;
		this.$id = 1;
		this.nativeId = null;
	}
	
	/**
	 * @return The parent scope of this scope.
	 */
	public Scope getParent() {
		return parent;
	}
	
	/**
	 * @return Return the root scope
	 */
	public Scope getRoot() {
		Scope parent = getParent();
		return parent == null ? this : parent.getRoot();
	}
	
	/**
	 * @return Returns <code>true</code> when this scope is the root scope
	 */
	public boolean isRoot() {
		return this == getRoot();
	}
	
	public void evaluateExpression(String expression) {
		plugin.evaluateScopeExpression(this.$id, expression);
	}
	
	public void $on(String eventName, Handler.Callback callback) {
		plugin.invokeScopeMethod(this.$id, "$on", eventName, callback);
	}
	
	public void $watch(String expression, Handler.Callback callback) {
		plugin.invokeScopeMethod(this.$id, "$watch", expression, callback);
	}
	
	public Map<String, String> getElementAttributes() {
		return attributes;
	}
}
