(function(ko, Board, Turn, Player, Instructions) {
    function initializationViewModel() {
        var initialization = this;
        var globalFunctions = {};
        initialization.displaySubmitTurn = ko.observable(false);
        /*          GLOBAL FUNCTIONS                        */
        globalFunctions.setDisplaySubmitTurn = function(input) {
            initialization.displaySubmitTurn(input);
        };
        /*          END GLOBAL FUNCTIONS                    */
        initialization.openInstructions = function() {
            globalFunctions.setDisplayInstructions(true);
        };

        initialization.submitTurnClick = function() {
            globalFunctions.getAdditionalInfantry()[globalFunctions.getPlayerNumber() - 1] = 0;
            console.log(globalFunctions.getAdditionalInfantry());
            globalFunctions.commitTurn(midTurn=false);
        };

        new Player(globalFunctions);
        new Lobby(globalFunctions);
        new Board(globalFunctions);
        new Instructions(globalFunctions);
        new Turn(globalFunctions);
    }
    ko.applyBindings(new initializationViewModel(), document.getElementById('initializationKnockout1'));

})(window.ko, window.Board, window.Turn, window.Player, window.Instructions);