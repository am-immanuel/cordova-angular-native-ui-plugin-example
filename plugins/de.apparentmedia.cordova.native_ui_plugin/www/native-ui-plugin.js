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
    var scopeId = args[0];
    var $scope = angularScopeMap[scopeId];
    $scope.$apply(function() {
        if (action == 'evaluateScopeExpression') {
            var expression = args[1];
            $scope.$parse(expression)($scope);
        } else if (action == '$watch') {
            var expression = args[1];
            var callback = args[2];

            // args[3] is contains scope binding variables and full text divided by a separator '#'
            if (typeof args[3] !== "undefined") {
                $scope.$watch(expression, function(newValue, oldValue) {
                    var multipleExpressions = args[3].split("#");
                    var fullText = multipleExpressions[multipleExpressions.length-1];

                    // replace variables with their current values
                    for (var i=0; i<multipleExpressions.length-1; i++) {
                        if (multipleExpressions[i] != expression) {
                            fullText = fullText.replace("{{" + multipleExpressions[i] + "}}", $scope.$parse(multipleExpressions[i])($scope));
                        } else {
                            fullText = fullText.replace("{{" + expression + "}}", newValue);
                        }
                    }
                    //fullText = fullText.replace(/{{/g, "").replace(/}}/g, "");

                    // send Java the complete text with the new value from the watched expression
                    exec(null, null, "native-ui-plugin", "invokeCallback", [callback, fullText, oldValue]);
                    //exec(null, null, "native-ui-plugin", "invokeCallback", [callback, newValue, oldValue, expression, rawText]);
                });
            } else {
                $scope.$watch(expression, function (newValue, oldValue) {
                    exec(null, null, "native-ui-plugin", "invokeCallback", [callback, newValue, oldValue]);
                });
            }
        }
    });
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

function addScopeAndChildScopesToScopeMap($scope, angularScopeMap, transportScopeMap) {
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
                scope: false,
                link: function(scope, element, attributes){
                    if (!scope.nativeUI) {
                        scope.nativeUI = {};
                    }
                    var nativeId = attributes['nativeId'];
                    var nativeUIForElement = scope.nativeUI[nativeId];
                    if (!nativeUIForElement) {
                        scope.nativeUI[nativeId] = nativeUIForElement = {};
                    }
                    nativeUIForElement.tagName = element[0].tagName.toLowerCase();
                    //nativeUIForElement.innerBindingHTML = element[0].innerHTML;

                    if (element[0].innerText) {
                        nativeUIForElement.innerBindingHTML = element[0].innerText;
                    } else if (attributes["ngBindTemplate"]) {
                        nativeUIForElement.innerBindingHTML = element[0].attributes.getNamedItem("ng-bind-template").value;
                    }

                    // compile innerHTML text if directive innerBinding exists
                    /*scope.$compileProvider.directive("innerBinding", function($compile) {
                        return function (scope2, element, attrs) {
                            scope2.$watch(
                                function (scope) {
                                    return scope.$eval(attrs.compile);
                                },
                                function (value) {
                                    // set value with innerHTML because directive innerBinding has no value
                                    value = element[0].innerHTML;
                                    element.html(value);

                                    // compile content
                                    var content = $compile(element.contents())(scope2);

                                    // get current native ID and the corresponding native UI elements
                                    var nativeId = attrs['nativeId'];
                                    var nativeUIForElement = scope2.nativeUI[nativeId];
                                    if (!nativeUIForElement) {
                                        scope2.nativeUI[nativeId] = nativeUIForElement = {};
                                    }

                                    // set innerBinding with compiled content
                                    nativeUIForElement.innerBinding = content[0].textContent;

                                    // only update scope if no {{*}} exist anymore
                                    if (nativeUIForElement.innerBinding.indexOf("{{") < 0) {
                                        updateTransportScopeMap(scope2);
                                    }
                                }
                            );
                        }
                    });*/

                    angular.forEach(attributes, function(value, key) {
                        if (key.indexOf('$') != 0) {
                            nativeUIForElement[key] = attributes[key];
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
