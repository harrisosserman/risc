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

            }).fail(function() {

            });
        };
        self.constructComittedTurn();
    }
    window.Turn = turnViewModel;
})();

