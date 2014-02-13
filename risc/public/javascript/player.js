(function(ko) {
    function playerViewModel(globals) {
        var player = this;
        player.username = ko.observable();
        player.password = ko.observable();
        player.passwordCheck = ko.observable();
        player.displayModal = ko.observable(true);
        player.displayGameStart = ko.observable(true);
        player.displayLogin = ko.observable(true);
        player.displaySignup = ko.observable(false);
        player.usernameError = ko.observable(false);
        player.passwordError = ko.observable(false);
        player.loginError = ko.observable(false);

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
                player.displayModal(false);
                player.displayLogin(false);
                player.loginError(false);
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
                    'username': player.username(),
                    'password': CryptoJS.SHA512(player.password().toString())
                }),
                contentType: "application/json"
            }).done(function() {
                player.displayModal(false);
                player.displayLogin(false);
                player.usernameError(false);
                //do a get to the username
            }).fail(function() {
                player.usernameError(true);
            });
        };
    }
    window.Player = playerViewModel;
    ko.applyBindings(playerViewModel, document.getElementById('playerKnockout'));
})(window.ko);



        // initialization.loadWaitingPlayers = function(deferredObject) {
        //     $.ajax('/game/' + initialization.gameID, {
        //                 method: 'GET',
        //             }).done(function(result) {
        //                 var players = $.parseJSON(result);
        //                 initialization.playerList.removeAll();
        //                 var allPlayersReady = true;
        //                 var k=0;
        //                 initialization.createPlayerList(players.players);
        //                 for(k=0; k<players.players.length; k++) {
        //                     if(players.players[k].ready === false) allPlayersReady = false;
        //                 }
        //                 if(allPlayersReady === true && k > 1) {
        //                     //Can start the game if everyone is ready and there are at least 2 players
        //                     deferredObject.resolve(true);
        //                 } else {
        //                     deferredObject.resolve(false);
        //                 }
        //             });
        // };





        // initialization.enterGame = function() {
        //     var sendingData = {
        //         name: initialization.playerName()
        //     };
        //     $.ajax('/game', {
        //                 method: 'POST',
        //                 data: JSON.stringify(sendingData),
        //                 contentType: "application/json",
        //             }).done(function(result) {
        //                 initialization.displayGameWaitingRoom(true);
        //                 initialization.displayGameStart(false);
        //                 var resultData = $.parseJSON(result);
        //                 initialization.playerNumber = resultData.playerId;
        //                 initialization.gameID = resultData.gameID;
        //                 initialization.pollGameWaitingRoom();
        //             }).fail(function(result) {
        //                 //called when player tries to join after game has started
        //                 alert('Unfortunately, a game is in progress.  You can follow along!');
        //                 var resultData = $.parseJSON(result.responseText);
        //                 initialization.gameID = resultData.gameID;
        //                 initialization.loadWaitingPlayers($.Deferred());
        //                 initialization.displayModal(false);
        //                 initialization.displayMap(true);
        //                 new Board(globalFunctions);
        //             });
        // };

        //         initialization.startGame = function() {
        //     $.ajax('/game/' + initialization.gameID + '/start', {
        //         method: 'POST',
        //         contentType: "application/json",
        //         data: JSON.stringify({
        //             'name': initialization.playerName(),
        //             'playerNumber': initialization.playerNumber
        //         })
        //     }).done(function() {
        //         initialization.loadWaitingPlayers($.Deferred());
        //     });
        // };