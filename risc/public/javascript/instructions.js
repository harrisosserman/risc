(function(ko) {
    function instructionsViewModel(globals) {
        var globalFunctions = globals;
        var instructions = this;
        instructions.displayInstructions = ko.observable(false);
        globalFunctions.setDisplayInstructions = function(input) {
            instructions.displayInstructions(true);
        };
        instructions.closeInstructions = function() {
            instructions.displayInstructions(false);
        };

        ko.applyBindings(this, document.getElementById('instructionsKnockout'));
    }
    window.Instructions = instructionsViewModel;

})(window.ko);