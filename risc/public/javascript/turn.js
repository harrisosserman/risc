// This code inside of the turnViewModel function is only loaded once a player clicks the 'submit turn' button
(function() {
    function turnViewModel(globals) {
        var globalFunctions = globals;
        var turn = {};

        turn.pollForNextTurn = function(pollingNextTurnDOM) {
            var deferred = $.Deferred();
            var result = turn.loadGameMap(deferred);
            deferred.done(function(data) {
                    $(pollingNextTurnDOM).remove();
                    var gameMap = data.territories;
                    //handling when player loses or when player wins
                    var playerNumber = globalFunctions.getPlayerNumber();
                    var playerNumberFound = false;
                    var otherPlayersFound = false;
                    var owner = gameMap[0].owner;
                    for(var k=0; k<gameMap.length; k++) {
                        if(gameMap[k].owner !== owner) {
                            otherPlayersFound = true;
                        }
                        if(gameMap[k].owner === playerNumber) {
                            playerNumberFound = true;
                        }
                    }
                    if(playerNumberFound === false) {
                        $.ajax('/game/' + globalFunctions.getGameID() + '/exit', {
                            method: 'POST',
                            contentType: "application/json",
                            data: JSON.stringify({
                                'playerNumber': globalFunctions.getPlayerNumber()
                            })
                        });
                        globalFunctions.setPlayerNumber(-1);
                    }
                    if(otherPlayersFound === false) {
                        alert("Player " + owner + " wins!!!");
                        location.reload(true);
                    }
                    globalFunctions.destroyAndRebuildMap();
                }).fail(function() {
                    //all players have not yet finished their turns
                    setTimeout(function() {
                        turn.pollForNextTurn(pollingNextTurnDOM);
                    }, 1000); //wait 1 second before polling again
                });
        };

        turn.loadGameMap = function(deferred) {
            $.ajax('/game/' + gameID + '/mapReady', {
                method: 'GET',
            }).done(function(result) {
                var gameMap = $.parseJSON(result);
                deferred.resolve(gameMap);
            }).fail(function() {
                deferred.reject();
            });
        };

        turn.constructComittedTurn = function() {
            var returnData = {};
            returnData['gameID'] = globalFunctions.getGameID();
            returnData['username'] = globalFunctions.getUsername();
            returnData['timeStamp'] = new Date().getTime();
            returnData['food'] = globalFunctions.getPlayerInfo().food;
            returnData['technology'] = globalFunctions.getPlayerInfo().technology;
            returnData['technology_level'] = globalFunctions.getPlayerInfo().maxTechLevel;
            returnData['moves'] = globalFunctions.getMoveOrder();
            $.ajax('/game/' + gameID, {
                method: 'POST',
                data: JSON.stringify(returnData),
                contentType: "application/json"
            }).done(function() {
                globalFunctions.setDisplayMap(false);
                $('.attackComponent').each(function() {
                    $(this).remove();   //remove all arrows and attack numbers
                });
                var pollingNextTurnDOM = $("<h3>Waiting for other players to finish their turns...</h3>").appendTo("body").addClass("centerAlign");
                turn.pollForNextTurn(pollingNextTurnDOM);
            }).fail(function() {
                //FILL THIS IN FOR WHEN TURN VALIDATION FAILS
            });
        };
        turn.constructComittedTurn();
    }
    window.Turn = turnViewModel;
})();

