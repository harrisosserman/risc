// $(function(ko) {
//     this.playerName = ko.observable('playerName');
//     this.enterGame = function() {
//         console.log("click happened");
//         console.log(this.playerName);
//     };
//     ko.applyBindings(new viewModel());
//     console.log("loaded index.js");
// }(window.ko));



function viewModel() {
    var self = this;
    self.playerName = ko.observable();
    self.displayGameWaitingRoom = ko.observable(false);
    self.displayGameStart = ko.observable(true);
    self.enterGame = function() {
        self.displayGameWaitingRoom(true);
        self.displayGameStart(false);
    };
}
ko.applyBindings(new viewModel());

