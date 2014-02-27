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
            console.log("clicking submit turn");
            new Turn(globalFunctions);
        };

        new Player(globalFunctions);
        new Lobby(globalFunctions);
        new Board(globalFunctions);
        new Instructions(globalFunctions);
    }
    ko.applyBindings(new initializationViewModel(), document.getElementById('initializationKnockout1'));

})(window.ko, window.Board, window.Turn, window.Player, window.Instructions);