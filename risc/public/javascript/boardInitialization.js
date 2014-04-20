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
            plane: [],
            spy: [],
            interceptor: []
        };
        board.playerInfo = {
            food: -1,
            technology: -1,
            maxTechLevel: -1
        };
        board.attackInfo = [];
        board.inputNumberAttackOrMove = ko.observable();
        board.typesOfTroops = ko.observableArray(['Infantry', 'Automatic Weapons', 'Rocket Launchers', 'Tanks', 'Improved Tanks', 'Fighter Planes', 'Spies', 'Interceptors']);
        board.displayMap = ko.observable(false);
        board.hasNotUpgradedThisTurn = ko.observable(true);
        board.displayUpgradeTroopsModal = ko.observable(false);
        board.displayTradeModal = ko.observable(false);
        board.typeOfTroopUpgradeSelected = ko.observable();
        board.canUseNukes = false;
        board.playerList = ko.observableArray();
        board.alliesList = ko.observableArray();
        board.constructProposedTrade = [];
        board.tradeGiver = ko.observable();
        board.tradeReceiver = ko.observable();
        board.tradeType = ko.observable();
        board.tradeTypeList = ko.observableArray(['Territory', 'Food', 'Technology', 'Infantry', 'Automatic Weapons', 'Rocket Launchers', 'Tanks', 'Improved Tanks', 'Fighter Planes', 'Spies', 'Interceptors']);
        board.tradeNumber = ko.observable();
        board.typeOfTroopSelected = ko.observable();
        board.territoryClickTerritoryNumber = ko.observable("-");
        board.territoryClickInfo = ko.observableArray();
        board.territoryClickAttackInfo = ko.observableArray();
        board.tradesList = ko.observableArray();
        board.selectedAddAlly = ko.observable();
        board.moveTroops = false;
        board.attackTroops = false;
        board.technologyLevelCost = [0, 20, 50, 80, 120, 150, 180];
        board.unitUpgradeCost = [0, 3, 11, 30, 55, 90, 35, 50];
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
            board.fillArrayWithZero(board.territoryOwner, 25);
            board.territoryDOMElements = [];
            board.fillArrayWithZero(board.territoryDOMElements, 25);
            board.additionalInfantry = [];
            board.attackInfo = [];
            board.editing.removeAllMoves();
            $("#map").empty();
            board.createMap();
            board.getMap();
            board.editing.clearSpyDowngrades();
            board.editing.constructSpyDowngrades();
            board.editing.clearSpiesCannotMove();
            board.editing.constructSpiesCannotMove();
            board.clearBoardInfo();
            board.alliesList.removeAll();
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
        board.clearBoardInfo = function() {
            board.boardInfo = {
                food: [],
                technology: [],
                infantry: [],
                automatic: [],
                rocket: [],
                tank: [],
                improvedTank: [],
                plane: [],
                spy: [],
                interceptor: []
            };
            for(var k=0; k<25; k++) {
                board.updateBoardInfoValues(k, k);
            }
        };
        board.fillArrayWithZero = function(array, elements) {
            for(var k=0;k<elements; k++) {
                array[k]= 0;
            }
        };
        board.createMap = function() {
            //function to build map out of table
            board.displayMap(true);
            globalFunctions.setDisplaySubmitTurn(true);
            var map = $("#map");
            var count = 1;
            for(var k=0; k<5; k++) {
                map.append("<tr>");
                for(var m=0; m<5; m++) {
                    map.append("<td>" + count + "<button class='upgradeTroopsButton'>Upgrade</button><button class='nukeButton'>Nuke</button><button class='interceptorButton'>Interceptor</button></td>");
                    count++;
                }
                map.append("</tr>");
            }
            $(".upgradeTroopsButton").click(function() {
                board.upgradeTroops();
            });
            $(".upgradeTroopsButton").hide();
            $(".nukeButton").click(function() {
                board.nukeTerritory();
            });
            $(".nukeButton").hide();
            $(".interceptorButton").click(function() {
                board.buyInterceptor();
            });
            $(".interceptorButton").hide();
        };
        board.getPlayerNumberByUsername = function(username) {
            for(var k=0; k<board.territoryInfo.playerInfo.length; k++) {
                if(username === board.territoryInfo.playerInfo[k].owner) {
                    return board.territoryInfo.playerInfo[k].playerNumber + 1;
                }
            }
            return -1;
        };
        board.getMap = function() {
            $("#dialog").dialog();
            $("#dialog").dialog('close');
            board.hasNotUpgradedThisTurn(true);
            $.ajax('/test/game/' + globalFunctions.getGameID() + '/map/' + globalFunctions.getUsername(), {
                method: 'GET',
                    }).done(function(result) {
                        if(globalFunctions.getPlayerNumber() === -1) {
                            $('.submitTurnButton').remove();
                        }
                        board.territoryInfo = $.parseJSON(result);
                        if(board.territoryInfo.notifyNukesAvailable === true) {
                            alert("Be careful!  One of the players now has nukes");
                        }
                        if(board.territoryInfo.canUseNukes === true) {
                            board.canUseNukes = true;
                        }
                        for(var m = 0; m<board.territoryInfo.playerInfo.length; m++) {
                            var playerNum = board.getPlayerNumberByUsername(board.territoryInfo.playerInfo[m].owner) - 1;
                            board.updatePlayerInfoTable(m, board.territoryInfo.playerInfo, false, playerNum);
                            board.additionalInfantry[playerNum] = board.territoryInfo.playerInfo[m].additionalInfantry;
                            if(globalFunctions.getPlayerNumber() - 1 === playerNum) {
                                board.playerInfo.food = board.territoryInfo.playerInfo[m].food;
                                board.playerInfo.technology = board.territoryInfo.playerInfo[m].technology;
                                board.playerInfo.maxTechLevel = board.territoryInfo.playerInfo[m].level;
                                board.updateAlliesTable(m, board.territoryInfo.playerInfo);
                            }
                        }
                        var map = $("#map td");
                        for(var index=0; index<board.territoryInfo.territories.length; index++) {
                            var position = board.territoryInfo.territories[index].position;
                            board.territoryOwner[position] = board.territoryInfo.territories[index].owner;
                            board.updateBoardInfoValues(index, position);
                            board.boardInfo.food[position] = board.territoryInfo.territories[index].food;
                            board.boardInfo.technology[position] = board.territoryInfo.territories[index].technology;
                            var playerNumber = board.getPlayerNumberByUsername(board.territoryInfo.territories[index].owner);
                            $(map[position]).addClass("player" + playerNumber);
                            if(playerNumber === globalFunctions.getPlayerNumber()) {
                                (function() {
                                    //created this immediate function because of a closure here
                                    //was previously only using the last position in the loop to bind the DOM elements
                                    var pos = position;
                                    $(map[pos]).hover(function() {
                                        $(map[pos]).addClass("territoryHover");
                                    }, function() {
                                        $(map[pos]).removeClass("territoryHover");
                                    });

                                    $(map[pos]).click(function() {
                                        board.highlightMap(pos + 1);
                                        $(map[pos]).toggleClass("territoryClick");
                                        board.listenForAdditionalInfantry(pos);
                                        if(!($(map[pos]).hasClass("territoryMoveTroops") || $(map[pos]).hasClass("territoryAttack"))) {
                                            board.updateTerritoryClickTable(pos);
                                            $($(".upgradeTroopsButton")[pos]).toggle();
                                            $($(".interceptorButton")[pos]).toggle();
                                        }
                                    });
                                })();
                            } else {
                                (function() {
                                    var pos = position;
                                    $(map[pos]).click(function() {
                                        //click handler for clicking on enemy territory
                                        board.highlightMap(pos + 1);
                                        board.userMapAction(pos, map);
                                        $(map[pos]).toggleClass("territoryClick");
                                        if(!($(map[pos]).hasClass("territoryMoveTroops") || $(map[pos]).hasClass("territoryAttack"))) {
                                            board.updateTerritoryClickTable(pos);
                                        }
                                        if(board.canUseNukes === true) {
                                            $($(".nukeButton")[pos]).toggle();
                                        }
                                    });
                                })();
                            }
                        }
                        $("#map td").each(function(index) {
                            if(!$(this).hasClass("player1") && !$(this).hasClass("player2") && !$(this).hasClass("player3") && !$(this).hasClass("player4") && !$(this).hasClass("player5")) {
                                //adding a click handler and CSS to territories that are not visible to player
                                $(this).addClass('territoryMoveSpy');
                                $(this).click(function() {
                                    board.highlightMap(index + 1);
                                    board.userMapAction(index, map);
                                    $(map[index]).toggleClass("territoryClick");
                                    if(!($(this).hasClass("territoryMoveTroops") || $(this).hasClass("territoryAttack"))) {
                                        board.updateTerritoryClickTable(index);
                                    }
                                    if(board.canUseNukes === true) {
                                        $($(".nukeButton")[index]).toggle();
                                    }
                                });
                            }
                        });
                        board.editing.constructSpyDowngradesAtStart(board.territoryInfo.spies, board.boardInfo.spy);
                        board.territoryDOMElements = $("#map td");
                }).fail(function() {
                    globalFunctions.displayMapNotReadyAndPoll();
                });
        };
        board.updateBoardInfoValues = function(index, position) {
            for(var k=0; k<9; k++) {
                var troopTypeInTerritoryInfo = board.editing.convertTextForTroopCommit(k);
                var troopTypeInBoardInfo = globalFunctions.convertReadableText(board.convertTroopToText(k), false).text;
                if(typeof board.territoryInfo.territories == 'undefined' || typeof board.territoryInfo.territories[index][troopTypeInTerritoryInfo] == 'undefined') {
                    board.boardInfo[troopTypeInBoardInfo][position] = 0;
                } else {
                    board.boardInfo[troopTypeInBoardInfo][position] = board.territoryInfo.territories[index][troopTypeInTerritoryInfo];
                }
            }
        };
        board.updatePlayerInfoTable = function(index, playerInfo, updateTechnology, playerNumber) {
            var playerObject;
            if(typeof playerNumber !== 'undefined' && playerNumber !== -1) {
                playerObject = board.playerList()[playerNumber];
            } else {
                playerObject = board.playerList()[index];
            }
            var newPlayerObject = {};
            if(typeof updateTechnology !== 'undefined' && updateTechnology !== false) {
                newPlayerObject = {
                    name: playerObject.name,
                    color: playerObject.color,
                    additionalInfantry: board.additionalInfantry[index],
                    food: board.playerInfo.food,
                    tech: board.playerInfo.technology,
                    techLevel: board.convertTroopToText(board.playerInfo.maxTechLevel, true)
                };
            }
            else if(typeof playerInfo !== 'undefined') {
                newPlayerObject = {
                    name: playerInfo[index].owner,
                    color: globalFunctions.getElementOfColorList(index),
                    additionalInfantry: playerInfo[index].additionalInfantry,
                    food: playerInfo[index].food,
                    tech: playerInfo[index].technology,
                    techLevel: board.convertTroopToText(playerInfo[index].level, true)
                };
                if(typeof playerInfo[index].technology == 'undefined') {
                    newPlayerObject.food = 0;
                    newPlayerObject.tech = 0;
                    newPlayerObject.additionalInfantry = 0;
                }
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
            if(typeof playerNumber !== 'undefined') {
                board.playerList.splice(playerNumber, 0, newPlayerObject);
            } else {
                board.playerList.splice(index, 0, newPlayerObject);
            }
        };
        board.hasAllyOnTerritory = function(index) {
            for(var k=0; k<board.alliesList().length; k++) {
                if(board.alliesList()[k].name === board.territoryOwner[index] && board.alliesList()[k].isAlliedNextTurn === true) {
                    return true;
                }
            }
            return false;
        };
        board.highlightMap = function(territoryNumber) {
            var index = territoryNumber - 1;
            // var map = board.territoryDOMElements;
            var map = $("#map td");
            if($(map[index]).hasClass('territoryClick') || $(map[index]).hasClass('territoryAttack') || $(map[index]).hasClass('territoryMoveTroops')) {
                //user wants to attack, move troops, or de-highlight
                board.userMapAction(index, map);
                return;
            }
            board.editing.removeAllPreviousAdjacencies();
            var adjacentTerritories = board.editing.findValidAdjacencies(index);
            for(var k=0; k<adjacentTerritories.length; k++) {
                if(board.territoryOwner[adjacentTerritories[k]] != globalFunctions.getUsername() &&  !board.hasAllyOnTerritory(adjacentTerritories[k])) {
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
        board.updateAlliesTable = function(m, playerInfo) {
            if(typeof playerInfo[m].allies !== 'undefined') {
                for(var k=0; k<playerInfo[m].allies.length; k++) {
                    board.alliesList.push({
                        'name': playerInfo[m].allies[k],
                        'isAlliedNextTurn': true
                    });
                }
            }

        };
        board.clearTradeInputs = function() {
            board.tradeGiver("");
            board.tradeReceiver("");
            board.tradeType("");
            board.tradeNumber("");
        };
        board.addToTrade = function() {
            board.constructProposedTrade.push(
            {
                giver: board.tradeGiver().name,
                receiver: board.tradeReceiver().name,
                number: board.tradeNumber(),
                type: board.tradeType()
            });
            board.clearTradeInputs();
        };
        board.cancelTrade = function(index) {
            board.tradesList.splice(index, 1);
            board.editing.removeTradeFromMovesList(index);
        };
        board.createTrade = function() {
            board.displayTradeModal(true);
            board.constructProposedTrade = [];
            $("#dialog").dialog();
        };
        board.buyInterceptor = function() {
            board.editing.buyInterceptor(board.territoryClickTerritoryNumber() - 1, board.playerInfo, board.boardInfo.interceptor);
            board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1, null, true);
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
                plane: board.boardInfo.plane[index],
                spy: board.boardInfo.spy[index],
                interceptor: board.boardInfo.interceptor[index]
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
                        plane: board.attackInfo[index][k].plane,
                        spy: board.attackInfo[index][k].spy,
                        interceptor: board.attackInfo[index][k].interceptor
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
        board.addAlly = function() {
            var newAlly = {
                'name': board.selectedAddAlly().name,
                'isAlliedNextTurn': true
            };
            if(board.selectedAddAlly().name === globalFunctions.getUsername()) {
                alert("You can't ally with yourself");
                return;
            }
            for(var k=0; k<board.alliesList().length; k++) {
                if(board.alliesList()[k].name === board.selectedAddAlly().name) {
                    if(board.alliesList()[k].isAlliedNextTurn === true) {
                        alert("You already are allies with that player");
                        return;
                    } else {
                        board.alliesList.remove(board.alliesList()[k]);
                        break;
                    }
                }
            }
            board.alliesList.push(newAlly);
            board.editing.editAlliance(globalFunctions.getUsername(), newAlly.name, true);
        };
        board.removeAlly = function(ally) {
            board.alliesList.remove(ally);
            ally.isAlliedNextTurn = false;
            board.alliesList.push(ally);
            board.editing.editAlliance(globalFunctions.getUsername(), ally.name, false);
        };
        globalFunctions.convertReadableText = function(input) {
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
            } else if(input === "Fighter Planes"){
                result = {
                    text: 'plane',
                    index: 5
                };
            } else if(input === "Spies"){
                result = {
                    text: 'spy',
                    index: 6
                };
            } else if(input === "Nukes") {
                result = {
                    text: 'nuke',
                    index: 7
                };
            }
            else if(input === "Interceptors"){
                result = {
                    text: 'interceptor',
                    index: 8
                };
            } else if(input === "Technology"){
                result = {
                    text: 'tech',
                    index: 9
                };
            }
            else if(input === "Territory"){
                result = {
                    text: 'territory',
                    index: 10
                };
            }
            else {
                result = {
                    text: 'food',
                    index: 11
                };
            }
            return result;
        };
        board.convertTroopToText = function(input, techLevel) {
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
            } else if((techLevel === true && input === 6) || (techLevel === false && input === 7)){
                return 'Nukes';
            } else if(input === 8) {
                return 'Interceptors';
            } else {
                return 'Spies';
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
        board.nukeTerritory = function() {
            board.editing.nukeTerritory(board.territoryClickTerritoryNumber() - 1, board.playerInfo);
            board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1, null, true);
        };

        board.submitMove = function() {
            $("#dialog").dialog('close');
            if(board.displayTradeModal() === true) {
                board.addToTrade();
                board.displayTradeModal(false);
                board.tradesList.push({
                    offer: board.constructProposedTrade
                });
                board.editing.addTrade(board.constructProposedTrade);
            } else {
                //regex found here: http://stackoverflow.com/questions/1019515/javascript-test-for-an-integer
                var intRegex = /^\d+$/;
                if(!(intRegex.test(board.inputNumberAttackOrMove()))) {
                   alert('You must enter a nonnegative integer');
                   return;
                }
                var troopType = globalFunctions.convertReadableText(board.typeOfTroopSelected());
                if(board.displayUpgradeTroopsModal() === true) {
                    var troopTypeUpgradeTo = globalFunctions.convertReadableText(board.typeOfTroopUpgradeSelected());
                    board.editing.upgradeTroops(board.territoryClickTerritoryNumber() - 1, board.boardInfo[troopType.text], board.boardInfo[troopTypeUpgradeTo.text], board.playerInfo, board.inputNumberAttackOrMove(), troopType, troopTypeUpgradeTo);
                } else if(board.moveTroops === true) {
                    board.editing.moveTroops(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType.text], board.inputNumberAttackOrMove(), troopType.index, board.territoryOwner);
                } else if(board.attackTroops === true) {
                    board.editing.attack(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType.text], board.attackInfo, board.inputNumberAttackOrMove(), troopType, board.territoryOwner);
                }
                board.updateTerritoryClickTable(board.territoryClickTerritoryNumber() - 1);
                board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1, null, true);
            }
        };
        board.playerWatching = function() {
            //reloads the map every 10 seconds for any players watching game
            if(globalFunctions.getPlayerNumber() !== -1) {
                return;
            }
            $.ajax('/test/game/' + globalFunctions.getGameID() + '/map/' + globalFunctions.getUsername(), {
                method: 'GET',
            }).done(function() {
                globalFunctions.destroyAndRebuildMap();
            });
            setTimeout(board.playerWatching, 10000);
        };
        board.clearBoardInfo();
        ko.applyBindings(this, document.getElementById('boardKnockout'));
    }
    window.Board = boardInitializationViewModel;
})(window.ko, window.BoardEditing);
