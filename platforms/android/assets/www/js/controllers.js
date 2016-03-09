// this module is used as a requirement for the starter module in app.js
angular.module('starter.controllers', [])

// controller defines functionalities for modal login window
.controller('AppCtrl', function($scope, $ionicModal, $timeout) {
  // Form data for the login modal
  $scope.loginData = {};

  // Create the login modal
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

// controller for displaying a playlist
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

// controller for playlist (empty)
.controller('PlaylistCtrl', function($scope, $stateParams) {
})

    // controller for the template activity1.html
    .controller('Activity1Ctrl', function($scope, $stateParams, $state) {
        // declare input variable from input
        $scope.input = {};

        // update labels after button click
        $scope.updateTextField = function() {
            $scope.output = $scope.input.text;
            $scope.name1 = "Trinity";
        };

        // go to activity2.html after button click
        $scope.gotoActivity2 = function() {
            $state.go('app.activity2');
        };

        // define watcher on input.text expression and log the new value into the console
        $scope.$watch('input.text', function(newValue, oldValue) {
            console.log("input changed: " + newValue);
        });

        // define default value for gender
        $scope.gender = "empty";

        // set dynamic values for the bound variables in labels
        $scope.name1 = "Morpheus";
        $scope.name2 = "Neo";
        $scope.color = "red";

        // fill supermarket list with selected items
        $scope.supermarketList = {};
        $scope.supermarket = function() {
            var list = "";
            Object.keys($scope.supermarketList).forEach(function(key) {
                if ($scope.supermarketList[key] == true) {
                    list = list + key + ", ";
                }
            });
            return list;
        };
        // show supermarket list on a label
        $scope.showList = Object.getOwnPropertyNames($scope.supermarketList).length > 0 ? $scope.supermarket() : "Empty";

        // define watcher collection on the supermarket list
        $scope.$watchCollection('supermarketList', function(newValue, oldValue) {
            $scope.showList = $scope.supermarket();
        });

        // define default values for bound variables
        $scope.backgroundColor = "white";
        $scope.wlanSwitch = "ON";

        /*
        $scope.textArray = ["Hallo", "Hello", "Hola", "Servus", "Ciao"];
        $scope.myText = $scope.textArray[0];
        $scope.counter = 1;
        $scope.switchText = function() {
            if ($scope.counter < $scope.textArray.length) {
                $scope.myText = $scope.textArray[$scope.counter];
                $scope.counter ++;

            } else {
                $scope.counter = 0;
                $scope.myText = $scope.textArray[$scope.counter];
            }
        };*/

    })
    // controller for the template activity2.html
    .controller('Activity2Ctrl', function($scope, $stateParams, $state  ) {
        // go to activity1.html after button click
        $scope.gotoActivity1 = function() {
            $state.go('app.activity1');
        };
        // set header title
        $scope.title = "Activity 2";

        // set counter and count button clicks
        $scope.counter = 0;
        $scope.countClicks = function() {
            $scope.counter = $scope.counter + 1;
        };

        // get number of counts
        $scope.result = $scope.counter;
        // add number of counts to result and display "NO" if result is less than 5
        // and "Yes" if result greater equal 5
        $scope.calc = function() {
            $scope.result = $scope.result + $scope.counter;
            $scope.alert = $scope.result < 5 ? "No" : "Yes";
        };

        $scope.alert = $scope.result < 5 ? "No" : "Yes";
    });
