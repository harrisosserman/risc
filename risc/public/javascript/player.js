(function(ko) {
    function playerViewModel(globals) {
        var globalFunctions = globals;
        var player = this;
        player.username = ko.observable();
        player.password = ko.observable();
        player.passwordCheck = ko.observable();
        player.displaySignupSigninModal = ko.observable(true);
        player.displayGameStart = ko.observable(true);
        player.displayLogin = ko.observable(true);
        player.displaySignup = ko.observable(false);
        player.usernameError = ko.observable(false);
        player.passwordError = ko.observable(false);
        player.loginError = ko.observable(false);
        player.playerNumber = -1;

        globalFunctions.getUsername = function() {
            return player.username();
        };
        globalFunctions.setPlayerNumber = function(number) {
            player.playerNumber = number;
        };
        globalFunctions.getPlayerNumber = function() {
            return player.playerNumber;
        };
        player.usernameChanged = function() {
            $.ajax('/test/player/' + player.username(), {
                method: 'GET'
            }).fail(function() {
                //there is no username in the db with the username the user tried to use
                player.usernameError(false);
            }).done(function() {
                player.usernameError(true);
            });

        };

        player.login = function() {
            $.ajax('/test/player/' + player.username() + '/login', {
                method: 'POST',
                data: JSON.stringify({
                    'password': CryptoJS.SHA512(player.password().toString())
                }),
                contentType: "application/json"
            }).done(function(result) {
                //do something with result
                player.displaySignupSigninModal(false);
                player.displayLogin(false);
                player.loginError(false);
                globalFunctions.setDisplayGameLobby(true);
            }).fail(function() {
                player.loginError(true);
            });
        };
        player.showSignup = function() {
            player.displaySignup(true);
            player.displayLogin(false);
        };
        player.createUser = function() {
            if(player.password() !== player.passwordCheck()) {
                player.passwordError(true);
                return;
            }
            player.passwordError(false);

            $.ajax('/test/player', {
                method: 'POST',
                data: JSON.stringify({
                    'name': player.username(),
                    'password': CryptoJS.SHA512(player.password().toString())
                }),
                contentType: "application/json"
            }).done(function() {
                player.displaySignupSigninModal(false);
                player.displayLogin(false);
                player.usernameError(false);
                globalFunctions.setDisplayGameLobby(true);
                //do a get to the username
            }).fail(function() {
                player.usernameError(true);
            });
        };

        ko.applyBindings(this, document.getElementById('playerKnockout'));
    }
    window.Player = playerViewModel;
})(window.ko);