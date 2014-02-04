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
        initialization.gameID = -1;
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
                        for(var k=0; k<players.players.length; k++) {
                            if(initialization.playerName() === players.players[k].name) {
                                initialization.playerNumber = k + 1;
                            }
                            initialization.playerList.push({
                                'name': players.players[k].name,
                                'ready': players.players[k].ready});
                        }
                        initialization.pollGameWaitingRoom();
                    });
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
                        for(k=0; k<players.players.length; k++) {
                            if(players.players[k].ready === 'false') allPlayersReady = false;
                            initialization.playerList.push({
                                'name': players.players[k].name,
                                'ready': players.players[k].ready});
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