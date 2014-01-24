(function() {
    function viewModel() {
        var self = this;
        self.playerName = ko.observable();
        self.displayGameWaitingRoom = ko.observable(false);
        self.displayGameStart = ko.observable(true);
        self.displayModal = ko.observable(true);
        self.displayMap = ko.observable(false);
        self.playerList = ko.observableArray([]);
        self.gameId = -1;
        self.enterGame = function() {
            // self.displayGameWaitingRoom(true);
            // self.displayGameStart(false);
            var data = {
                "name": self.playerName
            };
            $.ajax('/test/game', {
                        method: 'POST',
                        data: data
                    }).done(function(result) {
                        self.displayGameWaitingRoom(true);
                        self.displayGameStart(false);
                        var players = $.parseJSON(result);
                        self.gameId = players.gameId;
                        for(var k=0; k<players.players.length; k++) {
                            self.playerList.push(players.players[k].name);
                            console.log(players.players[k].name);
                        }
                        // get names
                    });
            // pollGameWaitingRoom(12345);      //pass in game id
        };
        self.startGame = function() {
            self.displayModal(false);
            self.displayMap(true);
        };
        self.pollGameWaitingRoom = function(gameId) {
            return;
            // while(true) {
            //     $.ajax('/game/' + gameId, {
            //         method: 'GET'
            //     }).done(function() {
            //         break;
            //     }).fail(function(result) {
            //         //add each element from result into observable array
            //     });
            //     $.delay(5000);  //wait 5 seconds before polling again
            // }
        };
    }
    ko.applyBindings(new viewModel());
})();

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