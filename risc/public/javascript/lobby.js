(function(ko) {
    function lobbyViewModel(globals) {
        var globalFunctions = globals;
        var lobby = this;
        lobby.displayGameLobby = ko.observable(false);
        lobby.myGamesList = ko.observableArray();
        lobby.lobbyGamesList = ko.observableArray();

        globalFunctions.setDisplayGameLobby = function(input) {
            lobby.displayGameLobby(input);
        };

        ko.applyBindings(this, document.getElementById('lobbyKnockout'));
    }
    window.Lobby = lobbyViewModel;

})(window.ko);



        // initialization.loadWaitingPlayers = function(deferredObject) {
        //     $.ajax('/game/' + initialization.gameID, {
        //                 method: 'GET',
        //             }).done(function(result) {
        //                 var players = $.parseJSON(result);
        //                 initialization.playerList.removeAll();
        //                 var allPlayersReady = true;
        //                 var k=0;
        //                 initialization.createPlayerList(players.players);
        //                 for(k=0; k<players.players.length; k++) {
        //                     if(players.players[k].ready === false) allPlayersReady = false;
        //                 }
        //                 if(allPlayersReady === true && k > 1) {
        //                     //Can start the game if everyone is ready and there are at least 2 players
        //                     deferredObject.resolve(true);
        //                 } else {
        //                     deferredObject.resolve(false);
        //                 }
        //             });
        // };





        // initialization.enterGame = function() {
        //     var sendingData = {
        //         name: initialization.playerName()
        //     };
        //     $.ajax('/game', {
        //                 method: 'POST',
        //                 data: JSON.stringify(sendingData),
        //                 contentType: "application/json",
        //             }).done(function(result) {
        //                 initialization.displayGameWaitingRoom(true);
        //                 initialization.displayGameStart(false);
        //                 var resultData = $.parseJSON(result);
        //                 initialization.playerNumber = resultData.playerId;
        //                 initialization.gameID = resultData.gameID;
        //                 initialization.pollGameWaitingRoom();
        //             }).fail(function(result) {
        //                 //called when player tries to join after game has started
        //                 alert('Unfortunately, a game is in progress.  You can follow along!');
        //                 var resultData = $.parseJSON(result.responseText);
        //                 initialization.gameID = resultData.gameID;
        //                 initialization.loadWaitingPlayers($.Deferred());
        //                 initialization.displayModal(false);
        //                 initialization.displayMap(true);
        //                 new Board(globalFunctions);
        //             });
        // };

        //         initialization.startGame = function() {
        //     $.ajax('/game/' + initialization.gameID + '/start', {
        //         method: 'POST',
        //         contentType: "application/json",
        //         data: JSON.stringify({
        //             'name': initialization.playerName(),
        //             'playerNumber': initialization.playerNumber
        //         })
        //     }).done(function() {
        //         initialization.loadWaitingPlayers($.Deferred());
        //     });
        // };