var exec = require('cordova/exec');

var angularScopeMap = {};

var $injector = angular.element(document.body).injector();
var $rootScope = $injector.get('$rootScope');
var $ionicPlatform = $injector.get('$ionicPlatform');
var $parse = $injector.get('$parse');

// register permanent callback
exec(function(call) {
    var args = call.args;
    var action = call.action;
    if (action == 'evaluateScopeExpression') {
        var scopeId = args[0];
        var expression = args[1];
        var $scope = angularScopeMap[scopeId];
        $scope.$parse(expression)($scope);
    } else if (action == '$watch') {
        var scopeId = args[0];
        var expression = args[1];
        var callback = args[2];
        var $scope = angularScopeMap[scopeId];
        $scope.$watch(expression, function(newValue, oldValue) {
            exec(null, null, "native-ui-plugin", "invokeCallback", [callback, newValue, oldValue]);
        });
    }
}, function() {
    console.log("couldn't register permanent callback");
}, "native-ui-plugin", "registerPermanentCallback", []);

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "native-ui-plugin", "coolMethod", [arg0]);
};

$rootScope.$on("$stateChangeSuccess", function (event, toState, toParams, fromState, fromParams) {
    exec(null, function() {
        console.log("Could not send status change to native side");
    }, "native-ui-plugin", "$stateChangeSuccess", [toState, toParams, fromState, fromParams]);
});

function addScopeAndChildScopesToScopeMap($scope, angularScopeMap, transportScopeMap, nativeId) {
    var transportScope = {};
    angularScopeMap[$scope.$id] = $scope;
    transportScope.id = $scope.$id;
    transportScope.$$childHead = null;
    transportScope.$$nextSibling = null;
    transportScope.$$childTail = null;
    transportScope.nativeUI = $scope.nativeUI ? $scope.nativeUI : null;
    transportScope.$parent = $scope.$parent != null ? $scope.$parent.$id : null;

    if ($scope.$$childHead) {
        addScopeAndChildScopesToScopeMap($scope.$$childHead, angularScopeMap, transportScopeMap);
        transportScope.$$childHead = $scope.$$childHead.$id;
    }

    if ($scope.$$nextSibling) {
        addScopeAndChildScopesToScopeMap($scope.$$nextSibling, angularScopeMap, transportScopeMap);
        transportScope.$$nextSibling = $scope.$$nextSibling.$id;
    }

    if ($scope.$$childTail) {
        transportScope.$$childTail = $scope.$$childTail.$id;
    }

    transportScopeMap[transportScope.id] = transportScope;
}

function getTransportScopeMap($scope) {
    var newTransportScopeMap = {};
    while ($scope.$parent && !angularScopeMap[$scope.$parent.$id]) {
        $scope = $scope.$parent;
    }
    addScopeAndChildScopesToScopeMap($scope, angularScopeMap, newTransportScopeMap);
    return newTransportScopeMap;
}

function updateTransportScopeMap($scope) {
    exec(null, function() {
        console.log("Could not send transport scope map to native side");
    }, "native-ui-plugin", "updateTransportScopeMap", [getTransportScopeMap($scope)]);
}

$ionicPlatform.ready(function() {
    console.log("$ionicPlatform.ready() in plugin called");
});

if (!$rootScope.nativeUIPluginDeferred) {
    throw new Error('nativeUIPlugin is not correctly initialized in app.js');
} else {
    $rootScope.nativeUIPlugin = {};
    updateTransportScopeMap($rootScope);
    $rootScope.$apply(function()
    {
        $rootScope.$compileProvider.directive('nativeId', function() {
            return {
                scope: true,
                link: function(scope, element, attributes){
                    scope.nativeUI = {
                        tagName : element[0].tagName.toLowerCase()
                    };
                    angular.forEach(attributes, function(value, key) {
                        if (key.indexOf('$') != 0) {
                            scope.nativeUI[key] = attributes[key];
                        }
                    });
                    scope.$parse = angular.element(element).injector().get('$parse');
                    updateTransportScopeMap(scope);
                }
            };
        });
        $rootScope.nativeUIPluginDeferred.resolve($rootScope.nativeUIPlugin);
    });
}
