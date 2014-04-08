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
            spy: []
        };
        board.playerInfo = {
            food: -1,
            technology: -1,
            maxTechLevel: -1
        };
        board.attackInfo = [];
        board.inputNumberAttackOrMove = ko.observable();
        board.typesOfTroops = ko.observableArray(['Infantry', 'Automatic Weapons', 'Rocket Launchers', 'Tanks', 'Improved Tanks', 'Fighter Planes', 'Spies']);
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
        board.unitUpgradeCost = [0, 3, 11, 30, 55, 90, 35];
        board.editing = new BoardEditing(globalFunctions);
        board.hasLoadedChat = false;
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
                spy: []
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
            if(board.hasLoadedChat === false) {
                var chatRef = new Firebase('https://torid-fire-6946.firebaseio.com');
                var chat = new FirechatUI(chatRef, document.getElementById("firechat-wrapper"));
                // chat._chat.createRoom("RISC12345", "public", function(roomId) {
                //      chat._chat.getRoom(roomId, function(room) {
                //             console.log(room);
                //         });


                // });

                // createRoom(roomName, roomType, callback(roomId))

                var simpleLogin = new FirebaseSimpleLogin(chatRef, function(err, user) {
                  if (user) {
                    chat.setUser(user.id, 'Anonymous' + user.id.substr(0, 8));
                    setTimeout(function() {
                      chat._chat.enterRoom('-JJzwl7SG5J01akn17WU');
                    }, 500);
                  } else {
                    simpleLogin.login('anonymous');
                  }
                });



                // var firechatRef = new Firebase('https://torid-fire-6946.firebaseio.com');
                // var auth = new FirebaseSimpleLogin(firechatRef, function(error, user) {
                //     if(error) {
                //         console.log("there was an error.  " + error);
                //     } else {
                //         console.log("user is: " + user);
                //     }
                // });
                // auth.login('facebook');
                // var chat = new FirechatUI(auth, document.getElementById("firechat-wrapper"));
                // chat.setUser(globalFunctions.getUsername(), globalFunctions.getUsername());

                board.hasLoadedChat = true;
            }


            $.ajax('/test/game/' + globalFunctions.getGameID() + '/map/' + globalFunctions.getUsername(), {
                method: 'GET',
                    }).done(function(result) {

                        if(globalFunctions.getPlayerNumber() === -1) {
                            $('.submitTurnButton').remove();
                        }
                        board.territoryInfo = $.parseJSON(result);
                        for(var m = 0; m<board.territoryInfo.playerInfo.length; m++) {
                            var playerNum = board.getPlayerNumberByUsername(board.territoryInfo.playerInfo[m].owner) - 1;
                            board.updatePlayerInfoTable(m, board.territoryInfo.playerInfo, false, playerNum);
                            board.additionalInfantry[playerNum] = board.territoryInfo.playerInfo[m].additionalInfantry;
                            if(globalFunctions.getPlayerNumber() - 1 === playerNum) {
                                board.playerInfo.food = board.territoryInfo.playerInfo[m].food;
                                board.playerInfo.technology = board.territoryInfo.playerInfo[m].technology;
                                board.playerInfo.maxTechLevel = board.territoryInfo.playerInfo[m].level;
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
                                            $($("#map td button")[pos]).toggle();
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
            for(var k=0; k<7; k++) {
                var troopTypeInTerritoryInfo = board.editing.convertTextForTroopCommit(k);
                var troopTypeInBoardInfo = board.convertReadableText(board.convertTechLevelToText(k)).text;
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
                // if(board.territoryOwner[adjacentTerritories[k]] === globalFunctions.getUsername()) {
                //     $(map[adjacentTerritories[k]]).addClass('territoryMoveTroops');
                // } else if($(map[adjacentTerritories[k]]).hasClass('territoryMoveSpy')) {
                //     $(map[adjacentTerritories[k]]).addClass('territoryMoveTroops');
                // }


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
                plane: board.boardInfo.plane[index],
                spy: board.boardInfo.spy[index]
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
                        spy: board.attackInfo[index][k].spy
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
            } else if(input === "Fighter Planes"){
                result = {
                    text: 'plane',
                    index: 5
                };
            } else {
                result = {
                    text: 'spy',
                    index: 6
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
        board.submitMove = function() {
            $("#dialog").dialog('close');
            //regex found here: http://stackoverflow.com/questions/1019515/javascript-test-for-an-integer
            var intRegex = /^\d+$/;
            if(!(intRegex.test(board.inputNumberAttackOrMove()))) {
               alert('You must enter a nonnegative integer');
               return;
            }
            var troopType = board.convertReadableText(board.typeOfTroopSelected());
            if(board.displayUpgradeTroopsModal() === true) {
                var troopTypeUpgradeTo = board.convertReadableText(board.typeOfTroopUpgradeSelected());
                board.editing.upgradeTroops(board.territoryClickTerritoryNumber() - 1, board.boardInfo[troopType.text], board.boardInfo[troopTypeUpgradeTo.text], board.playerInfo, board.inputNumberAttackOrMove(), troopType, troopTypeUpgradeTo);
            } else if(board.moveTroops === true) {
                board.editing.moveTroops(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType.text], board.inputNumberAttackOrMove(), troopType.index, board.territoryOwner);
            } else if(board.attackTroops === true) {
                board.editing.attack(board.destinationTerritory, $("#map td"), board.territoryDOMElements, board.boardInfo[troopType.text], board.attackInfo, board.inputNumberAttackOrMove(), troopType, board.territoryOwner);
            }
            board.updateTerritoryClickTable(board.territoryClickTerritoryNumber() - 1);
            board.updatePlayerInfoTable(globalFunctions.getPlayerNumber() - 1, null, true);
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
