// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
// 'starter.controllers' is found in controllers.js
angular.module('starter', ['ionic', 'starter.controllers']).run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if (window.cordova && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
    }
    if (window.StatusBar) {
      // org.apache.cordova.statusbar required
      StatusBar.styleDefault();
    }
  });
})

.config(function($stateProvider, $urlRouterProvider, $compileProvider) {
  $stateProvider

  .state('app', {
    url: "/app",
    abstract: true,
    templateUrl: "templates/menu.html",
    controller: 'AppCtrl',
    resolve: {
        nativeUIPlugin: function($q, $rootScope) {
            var cordovaIsAvailable = (typeof cordova !== 'undefined');
            if ($rootScope.nativeUIPlugin || !cordovaIsAvailable) {
                return cordovaIsAvailable ? $rootScope.nativeUIPlugin : {};
            }
            var deferred = $q.defer();
            $rootScope.nativeUIPluginDeferred = deferred;
            $rootScope.$compileProvider = $compileProvider;
            return deferred.promise;
        }
    }
  })

      .state('app.activity1', {
          url: "/activity1",
          views: {
              'menuContent': {
                  templateUrl: "templates/activity1.html",
                  controller: 'Activity1Ctrl'
              }
          }
      })
      .state('app.activity2', {
          url: "/activity2",
          views: {
              'menuContent': {
                  templateUrl: "templates/activity2.html",
                  controller: 'Activity2Ctrl'
              }
          }
      })
  .state('app.search', {
    url: "/search",
    views: {
      'menuContent': {
        templateUrl: "templates/search.html"
      }
    }
  })

  .state('app.browse', {
    url: "/browse",
    views: {
      'menuContent': {
        templateUrl: "templates/browse.html"
      }
    }
  })
    .state('app.playlists', {
      url: "/playlists",
      views: {
        'menuContent': {
          templateUrl: "templates/playlists.html",
          controller: 'PlaylistsCtrl'
        }
      }
    })

  .state('app.single', {
    url: "/playlists/:playlistId",
    views: {
      'menuContent': {
        templateUrl: "templates/playlist.html",
        controller: 'PlaylistCtrl'
      }
    }
  });
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/app/activity1');
});
