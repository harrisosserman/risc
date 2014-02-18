(function(ko, Board, Turn, Player) {
    function initializationViewModel() {
        var initialization = this;
        var globalFunctions = {};
        initialization.playerName = ko.observable();
        initialization.playerNumber = -1;
        initialization.displayMap = ko.observable(false);
        initialization.playerList = ko.observableArray([]);
        initialization.playerList.additionalTroops = ko.observable();
        initialization.gameID = -1;
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
        globalFunctions.createMap = function() {
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
        };
        /*          END GLOBAL FUNCTIONS                    */


        initialization.submitTurnClick = function() {
            new Turn(globalFunctions);
        };
        initialization.openInstructions = function() {
            initialization.displayInstructions(true);
        };
        initialization.closeInstructions = function() {
            initialization.displayInstructions(false);
        };
        globalFunctions.createMap();


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

        new Player(globalFunctions);
        new Lobby(globalFunctions);
    }
    $('.initializationKnockout').each(function() {
        ko.applyBindings(new initializationViewModel(), $(this).get(0));
    });

})(window.ko, window.Board, window.Turn, window.Player);