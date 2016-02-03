cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/com.ionic.keyboard/www/keyboard.js",
        "id": "com.ionic.keyboard.keyboard",
        "clobbers": [
            "cordova.plugins.Keyboard"
        ]
    },
    {
        "file": "plugins/org.apache.cordova.device/www/device.js",
        "id": "org.apache.cordova.device.device",
        "clobbers": [
            "device"
        ]
    },
    {
        "file": "plugins/de.apparentmedia.cordova.native_ui_plugin/www/native-ui-plugin.js",
        "id": "de.apparentmedia.cordova.native_ui_plugin.native-ui-plugin",
        "clobbers": [
            "cordova.plugins.native-ui-plugin"
        ]
    },
    {
        "file": "plugins/cordova-plugin-splashscreen/www/splashscreen.js",
        "id": "cordova-plugin-splashscreen.SplashScreen",
        "clobbers": [
            "navigator.splashscreen"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "com.ionic.keyboard": "1.0.3",
    "org.apache.cordova.console": "0.2.12",
    "org.apache.cordova.device": "0.2.13",
    "de.apparentmedia.cordova.native_ui_plugin": "0.0.1",
    "cordova-plugin-splashscreen": "3.1.0"
}
// BOTTOM OF METADATA
});