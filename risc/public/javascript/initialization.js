(function(ko, Board, Turn, Player) {
    function initializationViewModel() {
        var initialization = this;
        var globalFunctions = {};
        initialization.displayInstructions = ko.observable(false);
        initialization.displaySubmitTurn = ko.observable(false);
        /*          GLOBAL FUNCTIONS                        */
        globalFunctions.setDisplaySubmitTurn = function(input) {
            initialization.displaySubmitTurn(input);
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
        new Board(globalFunctions);
    }
    $('.initializationKnockout').each(function() {
        ko.applyBindings(new initializationViewModel(), $(this).get(0));
    });

})(window.ko, window.Board, window.Turn, window.Player);