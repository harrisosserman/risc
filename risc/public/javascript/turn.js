// This code inside of the boardViewModel function is only loaded once a player clicks the 'submit turn' button
(function() {
    function turnViewModel(context) {
        var self = context;
        var territoryOwner = self.territoryOwner;
        var troops = self.troops;
        var attackingTroops = self.attackingTroops;
        var gameID = self.gameID;
        var playerNumber = self.playerNumber;
        var troopDirections = ['up', 'down', 'left', 'right', 'up_left', 'up_right', 'down_left', 'down_right'];

        self.constructAttackingTroops = function(index) {
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

        self.pollForNextTurn = function(pollingNextTurnDOM) {
            var deferred = $.Deferred();
            var result = self.loadGameMap(deferred);
            deferred.done(function() {
                    $(pollingNextTurnDOM).remove();
                    // self.destroyAndRebuildMap();
                }).fail(function() {
                    //all players have not yet finished their turns
                    setTimeout(function() {
                        self.pollForNextTurn(pollingNextTurnDOM);
                    }, 1000); //wait 1 second before polling again
                });
        };

        self.loadGameMap = function(deferred) {
            $.ajax('/test/game/' + gameID + '/polling', {
                method: 'GET',
            }).done(function(result) {
                var gameMap = $.parseJSON(result);
                deferred.resolve();
            }).fail(function() {
                deferred.reject();
            });
        };

        self.constructComittedTurn = function() {
            var returnData = {};
            returnData['_id'] = gameID;
            returnData['player'] = playerNumber;
            var territories = [];
            for(var k=0; k<territoryOwner.length; k++) {
                var territoryInfo = {};
                var attacking = self.constructAttackingTroops(k);
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
                self.displayMap(false);
                var pollingNextTurnDOM = $("<h3>Waiting for other players to finish their turns...</h3>").appendTo("body").addClass("centerAlign");
                self.pollForNextTurn(pollingNextTurnDOM);
            }).fail(function() {

            });
        };
        self.constructComittedTurn();
    }
    window.Turn = turnViewModel;
})();

