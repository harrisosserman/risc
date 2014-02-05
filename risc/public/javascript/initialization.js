(function(ko, Board, Turn) {
    function initializationViewModel() {
        var initialization = this;
        var globalFunctions = {};
        initialization.playerName = ko.observable();
        initialization.playerNumber = -1;
        initialization.displayGameWaitingRoom = ko.observable(false);
        initialization.displayGameStart = ko.observable(true);
        initialization.displayModal = ko.observable(true);
        initialization.displayMap = ko.observable(false);
        initialization.playerList = ko.observableArray([]);
        initialization.playerList.name = ko.observable();
        initialization.playerList.ready = ko.observable();
        initialization.playerList.color = ko.observable();
        initialization.playerList.additionalTroops = ko.observable();
        initialization.gameID = -1;
        initialization.colorList = ['Purple', 'Salmon', 'Yellow', 'Light Blue', 'Dark Blue'];
        /*          GLOBAL FUNCTIONS                        */
        globalFunctions.setDisplayMap = function(input) {
            initialization.displayMap(input);
        };
        globalFunctions.getGameID = function() {
            return initialization.gameID;
        };
        globalFunctions.getPlayerNumber = function() {
            return initialization.playerNumber;
        };
        globalFunctions.updateAdditionalTroops = function(playerNumber, additionalTroops) {
            var playerObject = initialization.playerList()[playerNumber - 1];
            var newPlayerObject = {
                "name": playerObject.name,
                "ready": playerObject.ready,
                "color": playerObject.color,
                "additionalTroops": additionalTroops
            };
            initialization.playerList.remove(playerObject);
            initialization.playerList.splice(playerNumber - 1, 0, newPlayerObject);
        };
        /*          END GLOBAL FUNCTIONS                    */
        initialization.enterGame = function() {
            var data = {
                "name": initialization.playerName()
            };
            $.ajax('/test/game', {
                        method: 'POST',
                        data: data,
                        settings: [
                            {
                                contentType: "application/json"
                            }
                        ]
                    }).done(function(result) {
                        initialization.displayGameWaitingRoom(true);
                        initialization.displayGameStart(false);
                        var players = $.parseJSON(result);
                        initialization.gameID = players.gameID;
                        initialization.createPlayerList(players.players);
                        for(var k=0; k<players.players.length; k++) {
                            if(initialization.playerName() === players.players[k].name) {
                                initialization.playerNumber = k + 1;
                            }
                        }
                        initialization.pollGameWaitingRoom();
                    });
        };
        initialization.createPlayerList = function(data) {
            for(var k = 0; k<data.length; k++) {
                initialization.playerList.push({
                    'name': data[k].name,
                    'ready': data[k].ready,
                    'color': initialization.colorList[k],
                    'additionalTroops': 0
                });
            }
        };
        initialization.startGame = function() {
            $.ajax('/test/game/' + initialization.gameID + '/start', {
                method: 'POST',
                settings: [
                            {
                                contentType: "application/json"
                            }
                        ],
                data: {
                    'name': initialization.playerName(),
                    'playerNumber': initialization.playerNumber
                }
            }).done(function() {
                initialization.loadWaitingPlayers($.Deferred());
            });
        };
        initialization.pollGameWaitingRoom = function() {
                var deferred = $.Deferred();
                var result = initialization.loadWaitingPlayers(deferred);
                deferred.done(function(allPlayersReady) {
                    if(allPlayersReady === true) {
                        initialization.displayModal(false);
                        initialization.displayMap(true);
                        new Board(globalFunctions);
                    } else {
                        setTimeout(initialization.pollGameWaitingRoom, 1000); //wait 1 second before polling again
                    }
                });
        };
        initialization.loadWaitingPlayers = function(deferredObject) {
            $.ajax('/test/game/' + initialization.gameID, {
                        method: 'GET',
                    }).done(function(result) {
                        var players = $.parseJSON(result);
                        initialization.playerList.removeAll();
                        var allPlayersReady = true;
                        var k=0;
                        initialization.createPlayerList(players.players);
                        for(k=0; k<players.players.length; k++) {
                            if(players.players[k].ready === 'false') allPlayersReady = false;
                        }
                        if(allPlayersReady === true && k > 1) {
                            //Can start the game if everyone is ready and there are at least 2 players
                            deferredObject.resolve(true);
                        } else {
                            deferredObject.resolve(false);
                        }
                    });
        };
        initialization.submitTurnClick = function() {
            new Turn(globalFunctions);
        };

    }
    ko.applyBindings(new initializationViewModel());
})(window.ko, window.Board, window.Turn);

(function() {
    //function to build map out of table
    var map = $("#map");
    var count = 1;
    for(var k=0; k<5; k++) {
        map.append("<tr>");
        for(var m=0; m<5; m++) {
            map.append("<td>" + count + "</td>");
            count++;
        }
        map.append("</tr>");
    }
})();