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
        board.displayUpgradeTroopsModal = ko.observable(false);
        board.typeOfTroopUpgradeSelected = ko.observable();
        board.playerList = ko.observableArray();
        board.typeOfTroopSelected = ko.observable();
        board.territoryClickTerritoryNumber = ko.observable("-");
        board.territoryClickInfo = ko.observableArray();
        board.territoryClickAttackInfo = ko.observableArray();
        board.moveTroops = false;
        board.attackTroops = false;
        board.technologyLevelCost = [0, 20, 50, 80, 120, 150];
        board.unitUpgradeCost = [0, 3, 11, 30, 55, 90];

        board.editing = new BoardEditing(globalFunctions);
        /*          GLOBAL FUNCTIONS                        */
        globalFunctions.createAndLoadMap = function() {
            board.createMap();
            board.getMap();
            board.playerWatching();
        };
        globalFunctions.setDisplayMap = function(input) {
            board.displayMap(input);
        };
        globalFunctions.getTechnologyLevelCost = function() {
            return board.technologyLevelCost;
        };
        globalFunctions.getUnitUpgradeCost = function() {
            return board.unitUpgradeCost;
        };
        globalFunctions.getAdditionalInfantry = function() {
            return board.additionalInfantry;
        };
        globalFunctions.destroyAndRebuildMap = function() {
            globalFunctions.setDisplayMap(true);
            board.territoryInfo = {};
            board.territoryOwner = [];
            board.territoryDOMElements = [];
            board.additionalInfantry = [];
            board.attackInfo = [];
            board.editing.removeAllMoves();
            $("#map").empty();
            board.createMap();
            board.getMap();
        };
        globalFunctions.getPlayerInfo = function() {
            return board.playerInfo;
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
                    map.append("<td>" + count + "<button class='upgradeTroopsButton'>Upgrade</button></td>");
                    count++;
                }
                map.append("</tr>");
            }
            $("#map td button").click(function() {
                board.upgradeTroops();
            });
            $("#map td button").hide();
        };
        board.getPlayerNumberByUsername = function(username) {
            for(var k=0; k<board.playerList().length; k++) {
                if(username === board.playerList()[k].name) {
                    return k + 1;
                }
            }
            return 0;
        };
        board.getMap = function() {
            $("#dialog").dialog();
            $("#dialog").dialog('close');
            board.hasNotUpgradedThisTurn(true);

            $.ajax('/game/' + globalFunctions.getGameID() + '/map', {
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
                                board.playerInfo.technology = board.territoryInfo.playerInfo[m].technology;
                                board.playerInfo.maxTechLevel = board.territoryInfo.playerInfo[m].level;
                            }
                        }
                        var map = $("#map td");
                        $(map).each(function(index) {
                            var position = board.territoryInfo.territories[index].position;
                            board.territoryOwner.splice(position, 0, board.territoryInfo.territories[index].owner);
                            board.updateBoardInfoValues(position);
                            board.boardInfo.food[position] = board.territoryInfo.territories[index].food;
                            board.boardInfo.technology[position] = board.territoryInfo.territories[index].technology;

                            board.territoryDOMElements.splice(position, 0, $(this));
                            var playerNumber = board.getPlayerNumberByUsername(board.territoryInfo.territories[index].owner);
                            $(this).addClass("player" + playerNumber);
                            if(playerNumber === globalFunctions.getPlayerNumber()) {
                                $(this).hover(function() {
                                    $(this).addClass("territoryHover");
                                }, function() {
                                    $(this).removeClass("territoryHover");
                                });
                                $(this).click(function() {
                                    board.highlightMap(position + 1);
                                    $(this).toggleClass("territoryClick");
                                    board.listenForAdditionalInfantry(position);
                                    if(!($(this).hasClass("territoryMoveTroops") || $(this).hasClass("territoryAttack"))) {
                                        board.updateTerritoryClickTable(position);
                                        $($("#map td button")[position]).toggle();
                                    }
                                });
                            } else {
                                $(this).click(function() {
                                    //click handler for clicking on enemy territory
                                    board.userMapAction(position, map);
                                    if(!($(this).hasClass("territoryMoveTroops") || $(this).hasClass("territoryAttack"))) {
                                        board.updateTerritoryClickTable(position);
                                    }
                                });
                            }
                        });
                });
        };
        board.updateBoardInfoValues = function(index) {
            for(var k=0; k<6; k++) {
                var troopTypeInTerritoryInfo = board.editing.convertTextForTroopCommit(k);
                var troopTypeInBoardInfo = board.convertReadableText(board.convertTechLevelToText(k)).text;
                if(typeof board.territoryInfo.territories[index][troopTypeInTerritoryInfo] == 'undefined') {
                    board.boardInfo[troopTypeInBoardInfo][index] = 0;
                } else {
                    board.boardInfo[troopTypeInBoardInfo][index] = board.territoryInfo.territories[index][troopTypeInTerritoryInfo];
                }
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
                if(board.territoryOwner[adjacentTerritories[k]] != globalFunctions.getUsername()) {
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
                            board.editing.addMove(3, index, -1, 0, -1);
                        } else if(input.keyCode === 40) {
                            //down arrow
                            board.editing.calculateAdditionalTroops(-1, index, input, board.boardInfo.infantry, board.additionalInfantry);
                            board.updateTerritoryClickTable(index);
                            board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1);
                            board.editing.removeAdditionalTroop(3, index, 0);
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
                board.displayUpgradeTroopsModal(false);
                board.destinationTerritory = index;
            } else if($(map[index]).hasClass('territoryAttack')){
                $("#dialog").dialog();
                board.attackTroops = true;
                board.moveTroops = false;
                board.displayUpgradeTroopsModal(false);
                board.destinationTerritory = index;
            }
        };
        board.convertReadableText = function(input) {
            var result = {};
            if(input === "Infantry") {
                result = {
                    text: 'infantry',
                    index: 0
                };
            } else if(input === "Automatic Weapons") {
                result = {
                    text: 'automatic',
                    index: 1
                };
            } else if(input === "Rocket Launchers") {
                result = {
                    text: 'rocket',
                    index: 2
                };
            } else if(input === "Tanks") {
                result = {
                    text: 'tank',
                    index: 3
                };
            } else if(input === "Improved Tanks") {
                result = {
                    text: 'improvedTank',
                    index: 4
                };
            } else {
                result = {
                    text: 'plane',
                    index: 5
                };
            }
            return result;
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
        board.upgradeTroops = function() {
            board.displayUpgradeTroopsModal(true);
            $("#dialog").dialog();
        };
        board.upgradeTechLevel = function() {
            if(board.editing.upgradeTechLevel(board.playerInfo) === true) {
                board.hasNotUpgradedThisTurn(false);
                board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1, null, true);
            }
        };
        board.submitMove = function() {
            $("#dialog").dialog('close');
            //regex found here: http://stackoverflow.com/questions/1019515/javascript-test-for-an-integer
            var intRegex = /^\d+$/;
            if(!(intRegex.test(board.inputNumberAttackOrMove()))) {
               alert('You must enter a nonnegative integer');
            }
            var troopType = board.convertReadableText(board.typeOfTroopSelected());
            if(board.displayUpgradeTroopsModal() === true) {
                var troopTypeUpgradeTo = board.convertReadableText(board.typeOfTroopUpgradeSelected());
                board.editing.upgradeTroops(board.territoryClickTerritoryNumber() - 1, board.boardInfo[troopType.text], board.boardInfo[troopTypeUpgradeTo.text], board.playerInfo, board.inputNumberAttackOrMove(), troopType, troopTypeUpgradeTo);
            } else if(board.moveTroops === true) {
                board.editing.moveTroops(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType.text], board.inputNumberAttackOrMove(), troopType.index);
            } else if(board.attackTroops === true) {
                board.editing.attack(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType.text], board.attackInfo, board.inputNumberAttackOrMove(), troopType);
            }
            board.updateTerritoryClickTable(board.territoryClickTerritoryNumber() - 1);
            board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1, null, true);
        };
        board.playerWatching = function() {
            //reloads the map every 10 seconds for any players watching game
            if(globalFunctions.getPlayerNumber() !== -1) {
                return;
            }
            $.ajax('/test/game/' + globalFunctions.getGameID() + '/map', {
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
