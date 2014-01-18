function viewModel() {
    var self = this;
    self.playerName = ko.observable();
    self.displayGameWaitingRoom = ko.observable(false);
    self.displayGameStart = ko.observable(true);
    self.displayModal = ko.observable(true);
    self.displayMap = ko.observable(false);
    self.enterGame = function() {
        self.displayGameWaitingRoom(true);
        self.displayGameStart(false);
        // var data = {
        //     "name": self.playerName
        // };
        // $.ajax('/game/', {
        //             method: 'POST',
        //             data: data
        //         }).done(function(result) {
        //             self.displayGameWaitingRoom(true);
        //             self.displayGameStart(false);
        //             // get names
        //         });
    };
    self.startGame = function() {
        self.displayModal(false);
        self.displayMap(true);
    };
}
ko.applyBindings(new viewModel());

(function() {
    //function to build map out of table
    var map = $("#map tbody");
    var count = 1;
    for(var k=0; k<5; k++) {
        map.append("<tr>");
        for(var m=0; m<5; m++) {
            map.append("<td>" + count + "</td>");
            count++;
        }
        map.append("</tr>");
    }
})();