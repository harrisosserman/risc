(function() {
    function viewModel() {
        var self = this;
        self.playerName = ko.observable();
        self.playerNumber = -1;
        self.displayGameWaitingRoom = ko.observable(false);
        self.displayGameStart = ko.observable(true);
        self.displayModal = ko.observable(true);
        self.displayMap = ko.observable(false);
        self.playerList = ko.observableArray([]);
        self.gameID = -1;
        self.enterGame = function() {
            // self.displayGameWaitingRoom(true);
            // self.displayGameStart(false);
            var data = {
                "name": self.playerName()
            };
            $.ajax('/test/game', {
                        method: 'POST',
                        data: data
                    }).done(function(result) {
                        self.displayGameWaitingRoom(true);
                        self.displayGameStart(false);
                        var players = $.parseJSON(result);
                        self.gameID = players.gameID;
                        for(var k=0; k<players.players.length; k++) {
                            if(self.playerName() === players.players[k].name) playerNumber = k;
                            self.playerList.push({
                                'name': players.players[k].name,
                                'ready': players.players[k].ready});
                        }
                        self.pollGameWaitingRoom();
                    });
        };
        self.startGame = function() {
            $.ajax('/test/game/' + self.gameID + '/start', {
                method: 'POST',
                data: {
                    'name': self.playerName(),
                    'playerNumber': self.playerNumber
                }
            }).done(function() {
                self.loadWaitingPlayers($.Deferred());
            });
        };
        self.pollGameWaitingRoom = function() {
                var deferred = $.Deferred();
                var result = self.loadWaitingPlayers(deferred);
                deferred.done(function(allPlayersReady) {
                    if(allPlayersReady === true) {
                        self.displayModal(false);
                        self.displayMap(true);
                    } else {
                        setTimeout(self.pollGameWaitingRoom, 1000); //wait 5 seconds before polling again
                    }
                });
        };
        self.loadWaitingPlayers = function(deferredObject) {
            $.ajax('/test/game/' + self.gameID, {
                        method: 'GET',
                    }).done(function(result) {
                        var players = $.parseJSON(result);
                        self.playerList.removeAll();
                        var allPlayersReady = true;
                        var k=0;
                        for(k=0; k<players.players.length; k++) {
                            if(players.players[k].ready === 'false') allPlayersReady = false;
                            self.playerList.push({
                                'name': players.players[k].name,
                                'ready': players.players[k].ready});
                        }
                        if(allPlayersReady === true && k > 1) {
                            //Can start the game if everyone is ready and there are at least 2 players
                            deferredObject.resolve(true);
                        } else {
                            deferredObject.resolve(false);
                        }
                    });
        };
    }
    ko.applyBindings(new viewModel());
})();

(function() {
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
})();