(function(ko) {
    function lobbyViewModel(globals) {
        var globalFunctions = globals;
        var lobby = this;
        lobby.displayGameLobby = ko.observable(false);
        lobby.myGamesList = ko.observableArray();
        lobby.lobbyGamesList = ko.observableArray();

        globalFunctions.setDisplayGameLobby = function(input) {
            lobby.displayGameLobby(input);
            lobby.loadMyGames();
            lobby.loadAllGames();
        };
        lobby.getGameState = function(state) {
            if(state === 0) {
                return "waiting to start";
            }
            if(state === 1) {
                return "game in progress";
            }
            return "game over";
        };
        lobby.enterGame = function() {

        };
        lobby.loadMyGames = function() {
            $.ajax('/test/player/' + globalFunctions.getUsername(), {
                method: 'GET'
            }).done(function(result) {
                result = $.parseJSON(result);
                lobby.myGamesList.removeAll();
                for(var k=0; k<result.games.length; k++) {
                    $.ajax('/test/game/' + result.games[k].game, {
                        method: 'GET'
                    }).done(function(gameResult) {
                        gameResult = $.parseJSON(gameResult);
                        lobby.loadGamesHelper(lobby.myGamesList, gameResult);
                    });
                }
            });
        };
        lobby.loadAllGames = function() {
            $.ajax('/test/game', {
                method: 'GET'
            }).done(function(result) {
                lobby.lobbyGamesList.removeAll();
                var allGames = $.parseJSON(result);
                for(var k=0; k<allGames.length; k++) {
                    lobby.loadGamesHelper(lobby.lobbyGamesList, allGames[k]);
                }
            });
        };
        lobby.loadGamesHelper = function(dataStructure, game) {
                    var gameID = game.gameID;
                    var playerNames = "";
                    for(var m=0; m<game.players.length; m++) {
                        playerNames = playerNames + game.players[m].player;
                        if(m < (game.players.length - 1)) {
                            playerNames = playerNames + ", ";
                        }
                    }
                    var gameState = lobby.getGameState(game.state);
                    dataStructure.push({
                        players: playerNames,
                        state: gameState
                    });

        };

        ko.applyBindings(this, document.getElementById('lobbyKnockout'));
    }
    window.Lobby = lobbyViewModel;

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