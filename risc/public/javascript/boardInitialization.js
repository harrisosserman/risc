(function(ko, BoardEditing) {
    function boardInitializationViewModel(globals) {
        var globalFunctions = globals;
        var board = this;
        board.territoryInfo = {};
        board.destinationTerritory = 0;
        board.territoryOwner = [];
        board.territoryDOMElements = [];
        board.additionalInfantry = [];    //rename
        board.boardInfo = {
            food: [],
            technology: [],
            infantry: [],
            automatic: [],
            rocket: [],
            tank: [],
            improvedTank: [],
            plane: []
        };
        board.playerInfo = {
            food: -1,
            technology: -1,
            maxTechLevel: -1
        };
        board.attackInfo = [];
        board.inputNumberAttackOrMove = ko.observable();
        board.typesOfTroops = ko.observableArray(['Infantry', 'Automatic Weapons', 'Rocket Launchers', 'Tanks', 'Improved Tanks', 'Fighter Planes']);
        board.displayMap = ko.observable(false);
        board.hasNotUpgradedThisTurn = ko.observable(true);
        board.playerList = ko.observableArray();
        board.typeOfTroopSelected = ko.observable();
        board.territoryClickTerritoryNumber = ko.observable("-");
        board.territoryClickInfo = ko.observableArray();
        board.territoryClickAttackInfo = ko.observableArray();
        board.moveTroops = false;
        board.attackTroops = false;
        board.technologyLevelCost = [0, 20, 50, 80, 120, 150];
        board.unitUpgradeCost = [0, 3, 8, 19, 25, 35];

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
            // $("#dialog").dialog('close');
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
                            board.updatePlayerInfoTable(m, board.territoryInfo.playerInfo);
                            board.additionalInfantry[m] = board.territoryInfo.playerInfo[m].additionalInfantry;
                            if(globalFunctions.getPlayerNumber() - 1 === m) {
                                board.playerInfo.food = board.territoryInfo.playerInfo[m].food;
                                board.playerInfo.tech = board.territoryInfo.playerInfo[m].technology;
                                board.playerInfo.maxTechLevel = board.territoryInfo.playerInfo[m].level;
                            }
                        }
                        var map = $("#map td");
                        $(map).each(function(index) {
                            // board.attackingTroops.push({
                            //     'up': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     },
                            //     'down': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     },
                            //     'left': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     },
                            //     'right': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     },
                            //     'up_left': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     },
                            //     'up_right': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     },
                            //     'down_left': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     },
                            //     'down_right': {
                            //         'troops': 0,
                            //         'arrowDOM': ' ',
                            //         'textDOM': ' ',
                            //         'destination': -1
                            //     }
                            // });
                            board.territoryOwner.push(board.territoryInfo.territories[index].owner);
                            board.boardInfo.infantry[index] = board.territoryInfo.territories[index].INFANTRY;
                            board.boardInfo.automatic[index] = board.territoryInfo.territories[index].AUTOMATIC;
                            board.boardInfo.rocket[index] = board.territoryInfo.territories[index].ROCKETS;
                            board.boardInfo.tank[index] = board.territoryInfo.territories[index].TANKS;
                            board.boardInfo.improvedTank[index] = board.territoryInfo.territories[index].IMPROVEDTANKS;
                            board.boardInfo.plane[index] = board.territoryInfo.territories[index].PLANES;
                            board.boardInfo.food[index] = -1;
                            board.boardInfo.technology[index] = -1;

                            // board.troops.push(board.territoryInfo.territories[index].troops);
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
                                    board.listenForAdditionalInfantry(index);
                                    if(!($(this).hasClass("territoryMoveTroops") || $(this).hasClass("territoryAttack"))) {
                                        board.updateTerritoryClickTable(index);
                                    }
                                });
                            } else {
                                $(this).click(function() {
                                    //click handler for clicking on enemy territory
                                    board.userMapAction(index, map);
                                    if(!($(this).hasClass("territoryMoveTroops") || $(this).hasClass("territoryAttack"))) {
                                        board.updateTerritoryClickTable(index);
                                    }
                                });
                            }
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
        board.updatePlayerInfoTable = function(index, playerInfo, updateTechnology) {
            var playerObject = board.playerList()[index];
            var newPlayerObject = {};
            if(typeof updateTechnology !== 'undefined') {
                newPlayerObject = {
                    name: playerObject.name,
                    color: playerObject.color,
                    additionalInfantry: board.additionalInfantry[index],
                    food: board.playerInfo.food,
                    tech: board.playerInfo.technology,
                    techLevel: board.convertTechLevelToText(board.playerInfo.maxTechLevel)
                };
            }
            else if(typeof playerInfo !== 'undefined') {
                newPlayerObject = {
                    name: playerInfo[index].owner,
                    color: globalFunctions.getElementOfColorList(index),
                    additionalInfantry: playerInfo[index].additionalInfantry,
                    food: playerInfo[index].food,
                    tech: playerInfo[index].technology,
                    techLevel: board.convertTechLevelToText(playerInfo[index].level)
                };
            } else {
                newPlayerObject = {
                    name: playerObject.name,
                    color: playerObject.color,
                    additionalInfantry: board.additionalInfantry[index],
                    food: playerObject.food,
                    tech: playerObject.tech,
                    techLevel: playerObject.techLevel
                };
            }
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
        board.listenForAdditionalInfantry = function(index) {
            $("body").unbind("keydown");
            if($(board.territoryDOMElements[index]).hasClass('territoryClick')) {
                $("body").keydown(function(input) {
                        if(input.keyCode === 38) {
                            //up arrow
                            board.editing.calculateAdditionalTroops(1, index, input, board.boardInfo.infantry, board.additionalInfantry);
                            board.updateTerritoryClickTable(index);
                            board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1);
                        } else if(input.keyCode === 40) {
                            //down arrow
                            board.editing.calculateAdditionalTroops(-1, index, input, board.boardInfo.infantry, board.additionalInfantry);
                            board.updateTerritoryClickTable(index);
                            board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1);
                        }
                });
            }
        };
        board.updateTerritoryClickTable = function(index) {
            board.territoryClickTerritoryNumber(index + 1);
            board.territoryClickInfo.removeAll();
            var data = {
                food: board.boardInfo.food[index],
                tech: board.boardInfo.technology[index],
                infantry: board.boardInfo.infantry[index],
                automatic: board.boardInfo.automatic[index],
                rocket: board.boardInfo.rocket[index],
                tank: board.boardInfo.tank[index],
                improvedTank: board.boardInfo.improvedTank[index],
                plane: board.boardInfo.plane[index]
            };
            board.territoryClickInfo.push(data);
            board.territoryClickAttackInfo.removeAll();
            if(typeof board.attackInfo[index] != 'undefined') {
                for(var k=0; k<board.attackInfo[index].length; k++) {
                    data = {
                        destination: board.attackInfo[index][k].destination,
                        infantry: board.attackInfo[index][k].infantry,
                        automatic: board.attackInfo[index][k].automatic,
                        rocket: board.attackInfo[index][k].rocket,
                        tank: board.attackInfo[index][k].tank,
                        improvedTank: board.attackInfo[index][k].improvedTank,
                        plane: board.attackInfo[index][k].plane
                    };
                    board.territoryClickAttackInfo.push(data);
                }
            }
        };
        board.userMapAction = function(index, map) {
            if($(map[index]).hasClass('territoryClick')) {
                board.editing.removeAllPreviousAdjacencies();
                $(map[index]).addClass('territoryClick');   //need to add territoryClick class again because it will be toggled off in the click function
            } else if($(map[index]).hasClass('territoryMoveTroops')) {
                $("#dialog").dialog();
                board.moveTroops = true;
                board.attackTroops = false;
                board.destinationTerritory = index;
            } else if($(map[index]).hasClass('territoryAttack')){
                $("#dialog").dialog();
                board.attackTroops = true;
                board.moveTroops = false;
                board.destinationTerritory = index;
            }
        };
        board.convertReadableText = function(input) {
            if(input === "Infantry") {
                return 'infantry';
            } else if(input === "Automatic Weapons") {
                return 'automatic';
            } else if(input === "Rocket Launchers") {
                return 'rocket';
            } else if(input === "Tanks") {
                return 'tank';
            } else if(input === "Improved Tanks") {
                return 'improvedTank';
            } else {
                return 'plane';
            }
        };
        board.convertTechLevelToText = function(input) {
            if(input === 0) {
                return 'Infantry';
            } else if(input === 1) {
                return 'Automatic Weapons';
            } else if(input === 2) {
                return 'Rocket Launchers';
            } else if(input === 3) {
                return 'Tanks';
            } else if(input === 4) {
                return 'Improved Tanks';
            } else if(input === 5) {
                return 'Fighter Planes';
            }
        };
        board.upgradeTechLevel = function() {
            if(board.playerInfo.maxTechLevel === 5) {
                alert("You are already on the maximum technology level");
            } else if(board.playerInfo.food >= board.technologyLevelCost[board.playerInfo.maxTechLevel + 1]) {
                //able to issue upgrade request
                board.playerInfo.maxTechLevel++;
                board.playerInfo.food = board.playerInfo.food - board.technologyLevelCost[board.playerInfo.maxTechLevel];
                board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1, null, true);
                board.hasNotUpgradedThisTurn(false);
            } else {
                alert("Unable to issue upgrade request.  You need " + board.technologyLevelCost[board.playerInfo.maxTechLevel + 1] + " food and you have " + board.playerInfo.food);
            }
        };
        board.submitMove = function() {
            // console.log(board.typeOfTroopSelected());
            // console.log(board.inputNumberAttackOrMove());
            $("#dialog").dialog('close');
            var troopType = board.convertReadableText(board.typeOfTroopSelected());
            if(board.moveTroops === true) {
                board.editing.moveTroops(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType], board.inputNumberAttackOrMove());
            } else if(board.attackTroops === true) {
                board.editing.attack(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType], board.attackInfo, board.inputNumberAttackOrMove(), troopType);
            }
            board.updateTerritoryClickTable(board.territoryClickTerritoryNumber() - 1);
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

