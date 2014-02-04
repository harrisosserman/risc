// This code inside of the turnViewModel function is only loaded once a player clicks the 'submit turn' button
(function() {
    function turnViewModel(globals) {
        var globalFunctions = globals;
        var turn = {};
        var territoryOwner = globalFunctions.getTerritoryOwner();
        var troops = globalFunctions.getTroops();
        var attackingTroops = globalFunctions.getAttackingTroops();
        var gameID = globalFunctions.getGameID();
        var playerNumber = globalFunctions.getPlayerNumber();
        var troopDirections = ['up', 'down', 'left', 'right', 'up_left', 'up_right', 'down_left', 'down_right'];

        turn.constructAttackingTroops = function(index) {
            var attack = attackingTroops[index];
            var result = [];
            for(var k=0; k<troopDirections.length; k++) {
                if(attack[troopDirections[k]].troops === 0) {
                    continue;
                }
                else {
                    result.push({
                        "territory": attack[troopDirections[k]].destination,
                        "troops": attack[troopDirections[k]].troops
                    });
                }
            }
            return result;
        };

        turn.pollForNextTurn = function(pollingNextTurnDOM) {
            var deferred = $.Deferred();
            var result = turn.loadGameMap(deferred);
            deferred.done(function() {
                    $(pollingNextTurnDOM).remove();
                    globalFunctions.destroyAndRebuildMap();
                }).fail(function() {
                    //all players have not yet finished their turns
                    setTimeout(function() {
                        turn.pollForNextTurn(pollingNextTurnDOM);
                    }, 1000); //wait 1 second before polling again
                });
        };

        turn.loadGameMap = function(deferred) {
            $.ajax('/test/game/' + gameID + '/polling', {
                method: 'GET',
            }).done(function(result) {
                var gameMap = $.parseJSON(result);
                deferred.resolve();
            }).fail(function() {
                deferred.reject();
            });
        };

        turn.constructComittedTurn = function() {
            var returnData = {};
            returnData['_id'] = gameID;
            returnData['player'] = playerNumber;
            var territories = [];
            for(var k=0; k<territoryOwner.length; k++) {
                var territoryInfo = {};
                var attacking = turn.constructAttackingTroops(k);
                territoryInfo = {
                    "troops": troops[k],
                    "attacking": attacking
                };
                territories[k.toString()] = territoryInfo;
            }
            returnData['territories'] = territories;
            $.ajax('/test/game/' + gameID, {
                method: 'POST',
                data: returnData,
                settings: [
                    {
                        contentType: "application/json"
                    }
                ]
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

