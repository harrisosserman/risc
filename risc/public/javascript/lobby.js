(function(ko) {
    function lobbyViewModel(globals) {
        var globalFunctions = globals;
        var lobby = this;
        lobby.displayGameLobby = ko.observable(false);
        lobby.displayJoinOrCreateGame = ko.observable(true);
        lobby.displayGameWaitingRoom = ko.observable(false);
        lobby.myGamesList = ko.observableArray();
        lobby.lobbyGamesList = ko.observableArray();
        lobby.playerList = ko.observableArray();
        lobby.gameID = -1;
        lobby.colorList = ['Purple', 'Salmon', 'Yellow', 'Light Blue', 'Dark Blue'];

        globalFunctions.setDisplayGameLobby = function(input) {
            lobby.displayGameLobby(input);
            lobby.displayJoinOrCreateGame(input);
            lobby.loadMyGames();
            lobby.loadAllGames();
        };
        globalFunctions.getGameID = function() {
            return lobby.gameID;
        };
        globalFunctions.getElementOfColorList = function(index) {
            return lobby.colorList[index];
        };
        globalFunctions.getPlayerList = function() {
            return lobby.playerList;
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
        lobby.enterGame = function(data) {
            lobby.gameID = data.gameID;
            if(data.state === 0) {
                //game hasn't started yet
                lobby.displayJoinOrCreateGame(false);
                lobby.displayGameWaitingRoom(true);
                $.ajax('/game/' + globalFunctions.getGameID() + '/addPlayer', {
                    method: 'POST',
                    data: JSON.stringify({
                    'name': globalFunctions.getUsername()
                    }),
                    contentType: "application/json"
                    });
                lobby.pollGameWaitingRoom();
            } else if(data.state === 1) {
                //game is in progress
                lobby.displayJoinOrCreateGame(false);
                lobby.displayGameWaitingRoom(false);
                lobby.displayGameLobby(false);
                lobby.setPlayerNumber();
                globalFunctions.createAndLoadMap();
            } else {
                //game is over

            }
        };
        lobby.setPlayerNumber = function() {
            $.ajax('/game/' + lobby.gameID, {
                        method: 'GET',
                    }).done(function(result) {
                        var players = $.parseJSON(result);
                        for(k=0; k<players.players.length; k++) {
                            if(players.players[k].name === globalFunctions.getUsername()) {
                                globalFunctions.setPlayerNumber(k + 1);
                                return;
                            }
                        }
                    });
        };
        lobby.startGame = function() {
            $.ajax('/game/' + lobby.gameID + '/start', {
                method: 'POST',
                data: JSON.stringify({
                    'name': globalFunctions.getUsername()
                }),
                contentType: "application/json"
            });
        };
        lobby.createNewGame = function() {
            $.ajax('/game', {
                method: 'POST',
                data: JSON.stringify({
                    'name': globalFunctions.getUsername()
                }),
                contentType: "application/json"
            }).done(function(data) {
                data = $.parseJSON(data);
                lobby.gameID = data.gameID;
                lobby.displayJoinOrCreateGame(false);
                lobby.displayGameWaitingRoom(true);
                lobby.pollGameWaitingRoom();
            });
        };
        lobby.loadMyGames = function() {
            $.ajax('/player/' + globalFunctions.getUsername(), {
                method: 'GET'
            }).done(function(result) {
                result = $.parseJSON(result);
                lobby.myGamesList.removeAll();
                for(var k=0; k<result.games.length; k++) {
                    $.ajax('/game/' + result.games[k].game, {
                        method: 'GET'
                    }).done(function(data) {
                        lobby.loadMyGamesInnerFunc(data);
                    });
                }
            });
        };
        lobby.loadMyGamesInnerFunc = function(data) {
            data = $.parseJSON(data);
            console.log(data);
            lobby.loadGamesHelper(lobby.myGamesList, data);
        };
        lobby.loadAllGames = function() {
            $.ajax('/game', {
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
                playerNames = playerNames + game.players[m].name;
                if(m < (game.players.length - 1)) {
                    playerNames = playerNames + ", ";
                }
            }
            var gameState = lobby.getGameState(game.state);
            dataStructure.push({
                players: playerNames,
                stateDescription: gameState,
                state: game.state,
                gameID: gameID
            });
            // globalFunctions.createPlayerList(game);

        };
        lobby.createPlayerList = function(data) {
            for(var k = 0; k<data.length; k++) {
                lobby.playerList.push({
                    'name': data[k].name,
                    'ready': data[k].ready,
                    'color': lobby.colorList[k],
                    'additionalTroops': 0
                });
            }
        };
        lobby.pollGameWaitingRoom = function() {
            var deferred = $.Deferred();
            var result = lobby.loadWaitingPlayers(deferred);
            deferred.done(function(allPlayersReady) {
                if(allPlayersReady === true) {
                    lobby.displayGameLobby(false);
                    // initialization.displayMap(true);
                    globalFunctions.createAndLoadMap();
                } else {
                    setTimeout(lobby.pollGameWaitingRoom, 1000); //wait 1 second before polling again
                }
            });
        };
        lobby.loadWaitingPlayers = function(deferredObject) {
           $.ajax('/game/' + lobby.gameID, {
                        method: 'GET',
                    }).done(function(result) {
                        var players = $.parseJSON(result);
                        lobby.playerList.removeAll();
                        var allPlayersReady = true;
                        var k=0;
                        lobby.createPlayerList(players.players);
                        globalFunctions.setPlayerNumber(-1);
                        for(k=0; k<players.players.length; k++) {
                            if(players.players[k].ready === false) allPlayersReady = false;
                            if(players.players[k].name === globalFunctions.getUsername()) {
                                globalFunctions.setPlayerNumber(k + 1);
                            }
                        }
                        if(allPlayersReady === true && k > 1) {
                            //Can start the game if everyone is ready and there are at least 2 players
                            deferredObject.resolve(true);
                        } else {
                            deferredObject.resolve(false);
                        }
                    });
        };
        ko.applyBindings(this, document.getElementById('lobbyKnockout'));
    }
    window.Lobby = lobbyViewModel;

})(window.ko);