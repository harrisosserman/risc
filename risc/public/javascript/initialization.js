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
        initialization.displayInstructions = ko.observable(false);
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
        globalFunctions.setPlayerNumber = function(newPlayerNumber) {
            initialization.playerNumber = newPlayerNumber;
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
            var sendingData = {
                name: initialization.playerName()
            };
            $.ajax('/game', {
                        method: 'POST',
                        data: JSON.stringify(sendingData),
                        contentType: "application/json",
                    }).done(function(result) {
                        initialization.displayGameWaitingRoom(true);
                        initialization.displayGameStart(false);
                        var resultData = $.parseJSON(result);
                        initialization.playerNumber = resultData.playerId;
                        initialization.gameID = resultData.gameID;
                        initialization.pollGameWaitingRoom();
                    }).fail(function(result) {
                        //called when player tries to join after game has started
                        alert('Unfortunately, a game is in progress.  You can follow along!');
                        var resultData = $.parseJSON(result.responseText);
                        initialization.gameID = resultData.gameID;
                        initialization.loadWaitingPlayers($.Deferred());
                        initialization.displayModal(false);
                        initialization.displayMap(true);
                        new Board(globalFunctions);
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
            $.ajax('/game/' + initialization.gameID + '/start', {
                method: 'POST',
                contentType: "application/json",
                data: JSON.stringify({
                    'name': initialization.playerName(),
                    'playerNumber': initialization.playerNumber
                })
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
            $.ajax('/game/' + initialization.gameID, {
                        method: 'GET',
                    }).done(function(result) {
                        var players = $.parseJSON(result);
                        initialization.playerList.removeAll();
                        var allPlayersReady = true;
                        var k=0;
                        initialization.createPlayerList(players.players);
                        for(k=0; k<players.players.length; k++) {
                            if(players.players[k].ready === false) allPlayersReady = false;
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
        initialization.openInstructions = function() {
            initialization.displayInstructions(true);
        };
        initialization.closeInstructions = function() {
            initialization.displayInstructions(false);
        };


        //code to listen for user trying to exit or refresh page
        //found here: http://stackoverflow.com/questions/14645011/window-onbeforeunload-and-window-onunload-is-not-working-in-firefox-safari-o
        var myEvent = window.attachEvent || window.addEventListener;
        var chkevent = window.attachEvent ? 'onbeforeunload' : 'beforeunload'; /// make IE7, IE8 compitable
        myEvent(chkevent, function(e) { // For >=IE7, Chrome, Firefox
            var confirmationMessage = 'Are you sure you want to leave the page?  You game will be lost';  // a space
            (e || window.event).returnValue = confirmationMessage;
            return confirmationMessage;
        });
        $(window).unload(function() {
            $.ajax('/game/' + initialization.gameID + '/exit', {
                method: 'POST',
                contentType: "application/json",
                data: JSON.stringify({
                    'playerNumber': initialization.playerNumber
                })
            });
        });
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