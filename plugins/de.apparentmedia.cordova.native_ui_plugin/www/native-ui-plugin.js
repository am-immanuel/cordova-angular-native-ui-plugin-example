var exec = require('cordova/exec');

var angularScopeMap = {};

// register permanent callback
exec(function(data) {
    console.log(data);
    if (data.elementId) {
        var element = document.getElementById(data.elementId);
        if (element) {
            var $parse = angular.element(element).injector().get('$parse');
            var scope = angular.element(element).scope();
            $parse(data.expression)(scope);
        }
    }
}, function() {
    console.log("couldn't register permanent callback");
}, "native-ui-plugin", "registerPermanentCallback", []);

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "native-ui-plugin", "coolMethod", [arg0]);
};

var $rootScope = angular.element(document.body).scope().$root;
$rootScope.$on("$stateChangeStart", function (event, toState, toParams, fromState, fromParams) {
    exec(null, function() {
        console.log("Could not send status change to native side");
    }, "native-ui-plugin", "$stateChangeStart", [toState, toParams, fromState, fromParams, null]);
});

$rootScope.$on("$stateChangeSuccess", function (event, toState, toParams, fromState, fromParams) {
    exec(null, function() {
        console.log("Could not send status change to native side");
    }, "native-ui-plugin", "$stateChangeSuccess", [toState, toParams, fromState, fromParams, getTransportScopeMap()]);
});

function addScopeAndChildScopesToScopeMap($scope, angularScopeMap, transportScopeMap) {
    var transportScope = {};
    angularScopeMap[$scope.$id] = $scope;
    transportScope.id = $scope.$id;
    transportScope.$$childHead = null;
    transportScope.$$nextSibling = null;
    transportScope.$$childTail = null;
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

function getTransportScopeMap() {
    var newAngularScopeMap = {};
    var newTransportScopeMap = {};
    addScopeAndChildScopesToScopeMap($rootScope, newAngularScopeMap, newTransportScopeMap);
    angularScopeMap = newAngularScopeMap;
    return newTransportScopeMap;
}

exec(null, function() {
    console.log("Could not send transport scope map to native side");
}, "native-ui-plugin", "updateTransportScopeMap", [getTransportScopeMap()]);