var exec = require('cordova/exec');

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
    exec(function() {
        console.log("Status update has been sent to native side successfully");
    }, function() {
        console.log("Could not send status change to native side");
    }, "native-ui-plugin", "stateChangeStart", [toState, toParams, fromState, fromParams]);
});

