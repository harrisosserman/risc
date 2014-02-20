(function(ko, BoardEditing) {
    function boardInitializationViewModel(globals) {
        var globalFunctions = globals;
        var board = this;
        board.territoryInfo = {};
        board.territoryOwner = [];
        board.territoryDOMElements = [];
        board.troops = [];
        board.attackingTroops = [];
        board.additionalTroops = [];
        board.displayMap = ko.observable(false);
        board.playerList = ko.observableArray();
        board.territoryClickTerritoryNumber = ko.observable("-");
        board.territoryClickInfo = ko.observableArray();
        board.territoryClickAttackInfo = ko.observableArray();

        board.editing = new BoardEditing(globalFunctions);
        /*          GLOBAL FUNCTIONS                        */
        globalFunctions.createAndLoadMap = function() {
            board.createMap();
            board.getMap();
            board.playerWatching();
        };
        globalFunctions.getTerritoryOwner = function() {
            return board.territoryOwner;
        };
        globalFunctions.getTroops = function() {
            return board.troops;
        };
        globalFunctions.getAttackingTroops = function() {
            return board.attackingTroops;
        };
        globalFunctions.setDisplayMap = function(input) {
            board.displayMap(input);
        };
        globalFunctions.setTerritoryClickTerritoryNumber = function(input) {
            board.territoryClickTerritoryNumber(input);
        };
        globalFunctions.destroyAndRebuildMap = function() {
            globalFunctions.setDisplayMap(true);
            board.territoryInfo = {};
            board.territoryOwner = [];
            board.territoryDOMElements = [];
            board.troops = [];
            board.attackingTroops = [];
            $("#map").empty();
            board.createMap();
            var callMapReady = true;
            board.getMap(callMapReady);
        };
        /*          END GLOBAL FUNCTIONS                    */
        board.createMap = function() {
            //function to build map out of table
            board.displayMap(true);
            globalFunctions.setDisplaySubmitTurn(true);
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
        board.getMap = function(mapReady) {
            var appendUrl = "/map";
            if(typeof mapReady !== 'undefined' && mapReady === true) {
                appendUrl = "/mapReady";
            }
            $.ajax('/test/game/' + globalFunctions.getGameID() + appendUrl, {
                method: 'GET',
                    }).done(function(result) {
                        if(globalFunctions.getPlayerNumber() === -1) {
                            $('.submitTurnButton').remove();
                        }
                        board.territoryInfo = $.parseJSON(result);
                        for(var m = 0; m<board.territoryInfo.playerInfo.length; m++) {
                            // globalFunctions.updateAdditionalTroops(board.territoryInfo.additionalTroops[m].owner,
                            //     board.territoryInfo.additionalTroops[m].troops);
                            board.updatePlayerInfoTable(m, board.territoryInfo.playerInfo);
                        }
                        var map = $("#map td");
                        $(map).each(function(index) {
                            board.attackingTroops.push({
                                'up': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                },
                                'down': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                },
                                'left': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                },
                                'right': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                },
                                'up_left': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                },
                                'up_right': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                },
                                'down_left': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                },
                                'down_right': {
                                    'troops': 0,
                                    'arrowDOM': ' ',
                                    'textDOM': ' ',
                                    'destination': -1
                                }
                            });
                            board.territoryOwner.push(board.territoryInfo.territories[index].owner);
                            board.troops.push(board.territoryInfo.territories[index].troops);
                            board.territoryDOMElements.push($(this));
                            $(this).addClass("player" + board.territoryInfo.territories[index].owner);
                            if(board.territoryInfo.territories[index].owner === globalFunctions.getPlayerNumber()) {
                                $(this).hover(function() {
                                    $(this).addClass("territoryHover");
                                }, function() {
                                    $(this).removeClass("territoryHover");
                                });
                                $(this).click(function() {
                                    board.highlightMap(index + 1);
                                    $(this).toggleClass("territoryClick");
                                    board.listenForAdditionalTroops(index);
                                });
                            } else {
                                $(this).click(function() {
                                    //click handler for clicking on enemy territory
                                    board.userMapAction(index, map);
                                });
                            }
                            $(this).append("<p class='troopTotals'>troops: <span>" + board.territoryInfo.territories[index].troops + "</span></p>");
                        });
                });
        };
        globalFunctions.createPlayerList = function(data) {
            for(var k = 0; k<data.length; k++) {
                board.playerList.push({
                    'name': data[k].name,
                    'ready': data[k].ready,
                    'color': globalFunctions.getElementOfColorList(k),
                    'additionalInfantry': 0,
                    'food': 0,
                    'tech': 0,
                    'techLevel': 0
                });
            }
        };
        board.updatePlayerInfoTable = function(index, playerInfo) {
            var playerObject = board.playerList()[index];
            var newPlayerObject = {
                name: playerInfo[index].owner,
                color: globalFunctions.getElementOfColorList(index),
                additionalInfantry: playerInfo[index].additionalInfantry,
                food: playerInfo[index].food,
                tech: playerInfo[index].technology,
                techLevel: playerInfo[index].level
            };
            board.playerList.remove(playerObject);
            board.playerList.splice(index, 0, newPlayerObject);
        };
        board.highlightMap = function(territoryNumber) {
            var index = territoryNumber - 1;
            var map = board.territoryDOMElements;
            if($(map[index]).hasClass('territoryClick') || $(map[index]).hasClass('territoryAttack') || $(map[index]).hasClass('territoryMoveTroops')) {
                //user wants to attack, move troops, or de-highlight
                board.userMapAction(index, board.territoryDOMElements);
                return;
            }
            board.editing.removeAllPreviousAdjacencies();
            var adjacentTerritories = board.editing.findValidAdjacencies(index);
            for(var k=0; k<adjacentTerritories.length; k++) {
                if(board.territoryOwner[adjacentTerritories[k]] != globalFunctions.getPlayerNumber()) {
                    $(map[adjacentTerritories[k]]).addClass('territoryAttack');
                } else {
                    $(map[adjacentTerritories[k]]).addClass('territoryMoveTroops');
                }
            }
        };
        board.listenForAdditionalTroops = function(index) {
            $("body").unbind("keydown");
            if($(board.territoryDOMElements[index]).hasClass('territoryClick')) {
                $("body").keydown(function(input) {
                        if(input.keyCode === 38) {
                            //up arrow
                            board.editing.calculateAdditionalTroops(1, index, input, board.troops, board.additionalTroops);
                        } else if(input.keyCode === 40) {
                            //down arrow
                            board.editing.calculateAdditionalTroops(-1, index, input, board.troops, board.additionalTroops);
                        }
                });
            }
        };
        board.userMapAction = function(index, map) {
            if($(map[index]).hasClass('territoryClick')) {
                board.editing.removeAllPreviousAdjacencies();
                $(map[index]).addClass('territoryClick');   //need to add territoryClick class again because it will be toggled off in the click function
            } else if($(map[index]).hasClass('territoryMoveTroops')) {
                board.editing.moveTroops(index, map, board.territoryDOMElements, board.troops);
            } else if($(map[index]).hasClass('territoryAttack')){
                board.editing.attack(index, map, board.territoryDOMElements, board.troops, board.attackingTroops);
            }
        };
        board.playerWatching = function() {
            //reloads the map every 10 seconds for any players watching game
            if(globalFunctions.getPlayerNumber() !== -1) {
                return;
            }

            $.ajax('/test/game/' + globalFunctions.getGameID() + '/mapReady', {
                method: 'GET',
            }).done(function() {
                globalFunctions.destroyAndRebuildMap();
            });
            setTimeout(board.playerWatching, 10000);
        };

        ko.applyBindings(this, document.getElementById('boardKnockout'));
    }
    window.Board = boardInitializationViewModel;
})(window.ko, window.BoardEditing);

