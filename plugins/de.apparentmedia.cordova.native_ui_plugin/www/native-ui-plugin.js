var exec = require('cordova/exec');

var angularScopeMap = {};
var viewHistory = [];

// get service instance by using the injector
var $injector = angular.element(document.body).injector();
var $rootScope = $injector.get('$rootScope');
var $ionicPlatform = $injector.get('$ionicPlatform');
var $parse = $injector.get('$parse');
//var $location = $injector.get("$location");
var $state = $injector.get("$state");

/**
 * exec function from Cordova sends action "registerPermanentCallback" over
 * the service "native-ui-plugin" to a native platform
 * @param: Success function receives messages from a native plugin with Status "OK"
 * @param: Error function receives messages from a native plugin with Status "Error"
 * @param: {string} Service name which was defined in the plugin feature
 * @param: {string} Action name which specifies the type of data transmitted
 * @param: {Object[]} Array of arguments
* */
exec(function(call) {                           // success function receives native messages
    var args = call.args;                       // get all arguments from callback
    var action = call.action;                   // gets action name from callback
    var scopeId = args[0];                      // gets scope id from arguments
    var $scope = angularScopeMap[scopeId];      // find scope in angularScopeMap

    // call back button function
    if (action == "nativeBackButtonPressed") {
        back();
    } else {

    $scope.$apply(function() {

        // get expression from arguments and evaluate it with $parse function
        if (action == 'evaluateScopeExpression') {
            var expression = args[1];
            $scope.$parse(expression)($scope);

        /*} else if (action == "nativeBackButtonPressed") {
                // error da keine scope zugewiesen werden kann
                console.log("JS - MY back function - ");
                back();
                //$rootScope.$parse("back()")($rootScope); */

        // get expression and callback from arguments and set new watchers
        } else if (action == '$watch') {
            var expression = args[1];
            var callback = args[2];

            // args[3] contains scope binding variables and full text divided by a separator '#'
            if (typeof args[3] !== "undefined") {

                // set new watcher on an expression that has been sent from the native platform
                $scope.$watch(expression, function(newValue, oldValue) {
                    var multipleExpressions = args[3].split("#");                       // separate all expressions
                    var fullText = multipleExpressions[multipleExpressions.length-1];   // get the total raw text

                    // replace variables with their current values
                    for (var i=0; i<multipleExpressions.length-1; i++) {
                        if (multipleExpressions[i] != expression) {
                            // replace all non watcher expression with their current scope values
                            fullText = fullText.replace("{{" + multipleExpressions[i] + "}}", $scope.$parse(multipleExpressions[i])($scope));
                        } else {
                            // replace old watcher expression with the new value
                            fullText = fullText.replace("{{" + expression + "}}", newValue);
                        }
                    }

                    // send native platform the total text with the new value from the watched expression
                    exec(null, null, "native-ui-plugin", "invokeCallback", [callback, fullText, oldValue]);
                    //exec(null, null, "native-ui-plugin", "invokeCallback", [callback, newValue, oldValue, expression, rawText]);
                });
            } else {
                // set watcher on an expression and send new value to native platform
                $scope.$watch(expression, function (newValue, oldValue) {
                    exec(null, null, "native-ui-plugin", "invokeCallback", [callback, newValue, oldValue]);
                });
            }
        }
    });}
}, function() {     // error function
    console.log("couldn't register permanent callback");
}, "native-ui-plugin", "registerPermanentCallback", []);

// triggered after new view was displayed successfully
/**
 * Event listener on the rootscope which is triggered after a successful state change
 * @param: {string} Contains the event name on which the listener is listening
 * @param: Function contains several data from event triggering
 *      @param: event - Contains the triggered event
 *      @param: toState - Contains the new state
 *      @param: toParams - Contain the parameter from the new state
 *      @param: fromState - Contains the previous state
 *      @param: fromParams - Contains the parameter from the previous state
 * */
$rootScope.$on("$stateChangeSuccess", function (event, toState, toParams, fromState, fromParams) {

    // transfer view change to native platform
    exec(null, function() {
        console.log("Could not send status change to native side");
    }, "native-ui-plugin", "$stateChangeSuccess", [toState, toParams, fromState, fromParams]);

    // add new view in view history stack
    viewHistory.push($state.current.name);
});

//$rootScope.$on("routeChangeSuccess", function(event, current, previous) {});
// $rootScope.back = function() {}

// calls last visited view on screen
/**
 * This function is called after JavaScript gets the action "nativeBackButtonPressed" from a native platform.
 * That means that the previous view was called and has to be called in Angular as well.
 * */
function back() {
    //var previousView = viewHistory.length > 1 ? viewHistory.splice(-2)[0] : "/";
    //$location.path(previousView);

    // if there is a previous view call it
    if (viewHistory.length > 1) {
        viewHistory.splice(-1,1);                               // remove current view
        var previousView = viewHistory[viewHistory.length-1];   // get previous view
        $state.go(previousView);                                // go to previous view
        viewHistory.splice(-1,1);   // remove previous view because this view will be added as current view again
    }
    else {
        //$state.go("app.activity1");
    }
}

// build scope hierarchy
/**
 * This functions adds a scope including its properties into the transportScopeMap
 * @param: $scope - Current Angular scope object
 * @param: angularScopeMap - The ScopeMap from Angular
 * @param: transportScopeMap - Current ScopeMap which will be transported to a native platform
 * */
function addScopeAndChildScopesToScopeMap($scope, angularScopeMap, transportScopeMap) {
    var transportScope = {};
    angularScopeMap[$scope.$id] = $scope;       // add scope to angularScopeMap
    transportScope.id = $scope.$id;             // add scope-id to transportScope
    transportScope.$$childHead = null;
    transportScope.$$nextSibling = null;
    transportScope.$$childTail = null;
    transportScope.nativeUI = $scope.nativeUI ? $scope.nativeUI : null;             // add nativeUI components if existing
    transportScope.$parent = $scope.$parent != null ? $scope.$parent.$id : null;    // add parent scope if existing

    // add first child and call function recursive
    if ($scope.$$childHead) {
        addScopeAndChildScopesToScopeMap($scope.$$childHead, angularScopeMap, transportScopeMap);
        transportScope.$$childHead = $scope.$$childHead.$id;
    }

    // add neighboured sibling and call function recursive
    if ($scope.$$nextSibling) {
        addScopeAndChildScopesToScopeMap($scope.$$nextSibling, angularScopeMap, transportScopeMap);
        transportScope.$$nextSibling = $scope.$$nextSibling.$id;
    }

    // add last child
    if ($scope.$$childTail) {
        transportScope.$$childTail = $scope.$$childTail.$id;
    }

    // add generated transportScope in transportScopeMap
    transportScopeMap[transportScope.id] = transportScope;
}

// get current ScopeMap hierarchy
/**
 * Build the current TransportScopeMap and return the result
 * @param: $scope - Current Angular scope object
 * @return: {Object} TransportScopeMap which represents the current build ScopeMap
 * */
function getTransportScopeMap($scope) {
    var newTransportScopeMap = {};

    // find highest scope which is not in angularScopeMap
    while ($scope.$parent && !angularScopeMap[$scope.$parent.$id]) {
        $scope = $scope.$parent;
    }
    // add this scope, its children and siblings to ScopeMap
    addScopeAndChildScopesToScopeMap($scope, angularScopeMap, newTransportScopeMap);

    return newTransportScopeMap;
}

// send ScopeMap to native platform
/**
 * Send current ScopeMap to a native platform with action "updateTransportScopeMap"
 * @param: $scope - Current Angular scope object
 * */
function updateTransportScopeMap($scope) {
    exec(null, function() {
        console.log("Could not send transport scope map to native side");
    }, "native-ui-plugin", "updateTransportScopeMap", [getTransportScopeMap($scope)]);
}

// log that the ionic platform is ready
$ionicPlatform.ready(function() {
    console.log("$ionicPlatform.ready() in plugin called");
});


/**
 * Initialization process of the native UI Plugin
 * */
// only if nativeUIPluginDeferred is set in app.js, initialize plugin
if (!$rootScope.nativeUIPluginDeferred) {
    throw new Error('nativeUIPlugin is not correctly initialized in app.js');
} else {
    $rootScope.nativeUIPlugin = {};
    updateTransportScopeMap($rootScope);        // update ScopeMap with $rootScope

    // notify a new directive
    $rootScope.$apply(function()
    {
        // register new directive nativeId
        $rootScope.$compileProvider.directive('nativeId', function() {
            return {
                scope: false,       // do not create a new scope
                link: function(scope, element, attributes){
                    // create native ui on scope
                    if (!scope.nativeUI) {
                        scope.nativeUI = {};
                    }

                    // add native id
                    var nativeId = attributes['nativeId'];
                    var nativeUIForElement = scope.nativeUI[nativeId];
                    if (!nativeUIForElement) {
                        scope.nativeUI[nativeId] = nativeUIForElement = {};
                    }

                    // add tag name to native element
                    nativeUIForElement.tagName = element[0].tagName.toLowerCase();
                    //nativeUIForElement.innerBindingHTML = element[0].innerHTML;

                    // add inner text binding to native element, if innerText exists
                    if (element[0].innerText) {
                        nativeUIForElement.innerBindingHTML = element[0].innerText;

                    // get text from ng-bind-template attribute
                    } else if (attributes["ngBindTemplate"]) {
                        nativeUIForElement.innerBindingHTML = element[0].attributes.getNamedItem("ng-bind-template").value;
                    }

                    // add all attributes without a beginning '$' to native element
                    angular.forEach(attributes, function(value, key) {
                        if (key.indexOf('$') != 0) {
                            nativeUIForElement[key] = attributes[key];
                        }
                    });
                    // add $parse instance and update ScopeMap
                    scope.$parse = angular.element(element).injector().get('$parse');
                    updateTransportScopeMap(scope);
                }
            };
        });
        // resolve the promise from app.js
        $rootScope.nativeUIPluginDeferred.resolve($rootScope.nativeUIPlugin);
    });
}
