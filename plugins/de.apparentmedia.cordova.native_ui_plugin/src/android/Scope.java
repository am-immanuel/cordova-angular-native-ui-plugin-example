package de.apparentmedia.cordova;


/**
 * Core class for the AngularJS scope bridge between
 * JavaScript and Java.
 *  
 * @author Immanuel Scheerer, apparent media
 */
public class Scope {
	private Scope parent;
	private NativeUIPlugin plugin;
	private String domElementId;
	
	/**
	 * Create new scope as child scope of the given parent scope.
	 * @param parentScope The parent scope for which a new child should be created.
	 */
	public Scope(Scope parentScope, String domElementId) {
		if (parentScope == null) {
			throw new RuntimeException("parent scope is mandatory");
		}
		this.parent = parentScope;
		this.domElementId = domElementId;
		this.plugin = parentScope.plugin;
	}
	
	/**
	 * This constructor is package protected to avoid root scope generation by accident.
	 */
	protected Scope(NativeUIPlugin plugin) {
		this.plugin = plugin;
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
		plugin.evaluateScopeExpression(this.domElementId, expression);
	}
}
