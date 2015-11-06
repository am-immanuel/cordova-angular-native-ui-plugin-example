angular.module('starter.controllers', [])

.controller('AppCtrl', function($scope, $ionicModal, $timeout) {
  // Form data for the login modal
  $scope.loginData = {};

  // Create the login modal that we will use later
  $ionicModal.fromTemplateUrl('templates/login.html', {
    scope: $scope
  }).then(function(modal) {
    $scope.modal = modal;
  });

  // Triggered in the login modal to close it
  $scope.closeLogin = function() {
    $scope.modal.hide();
  };

  // Open the login modal
  $scope.login = function() {
    $scope.modal.show();
  };

  // Perform the login action when the user submits the login form
  $scope.doLogin = function() {
    console.log('Doing login', $scope.loginData);

    // Simulate a login delay. Remove this and replace with your login
    // code if using a login system
    $timeout(function() {
      $scope.closeLogin();
    }, 1000);
  };
})

.controller('PlaylistsCtrl', function($scope) {
  $scope.playlists = [
    { title: 'Reggae', id: 1 },
    { title: 'Chill', id: 2 },
    { title: 'Dubstep', id: 3 },
    { title: 'Indie', id: 4 },
    { title: 'Rap', id: 5 }
  ];
   for (var i = 6; i < 100000; i++) {
       $scope.playlists.push(
           { title: 'Cowbell ' + i, id: i }
       );
   }
})

.controller('PlaylistCtrl', function($scope, $stateParams) {
})

    .controller('Activity1Ctrl', function($scope, $stateParams, $state) {
        $scope.input = {};
        $scope.updateTextField = function() {
            $scope.output = $scope.input.text;
        }
        $scope.gotoActivity2 = function() {
            $state.go('app.activity2');
        };
        $scope.$watch('input.text', function(newValue, oldValue) {
            console.log("input changed: " + newValue);
        });
    })
    .controller('Activity2Ctrl', function($scope, $stateParams, $state  ) {
        $scope.gotoActivity1 = function() {
            $state.go('app.activity1');
        };
        $scope.title = "Activity 2";

        $scope.counter = 0;
        $scope.countClicks = function() {
            $scope.counter = $scope.counter + 1;
        };

        $scope.result = $scope.counter;
        $scope.calc = function() {
            $scope.result = $scope.result + $scope.counter;
            $scope.alert = $scope.result < 5 ? "No" : "Yes";
        };

        $scope.alert = $scope.result < 5 ? "No" : "Yes";

    });
