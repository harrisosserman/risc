// This code inside of the boardViewModel function is only loaded once the game is fully initialized
(function(ko) {
    function boardViewModel(globals) {
        var globalFunctions = globals;
        var board = {};
        board.territoryInfo = {};
        board.territoryOwner = [];
        board.territoryDOMElements = [];
        board.troops = [];
        board.attackingTroops = [];
        board.territory2DArray = [[0, 1, 2, 3, 4], [5, 6, 7, 8, 9], [10, 11, 12, 13, 14], [15, 16, 17, 18, 19], [20, 21, 22, 23, 24]];
        board.additionalTroops = [];
        /*          GLOBAL FUNCTIONS                        */
        globalFunctions.createMap = function() {
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
        /*          END GLOBAL FUNCTIONS                    */
        globalFunctions.getMap = function(mapReady) {
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
                        if(typeof board.territoryInfo.additionalTroops !== 'undefined') {
                            for(var m = 0; m<board.territoryInfo.additionalTroops.length; m++) {
                                // globalFunctions.updateAdditionalTroops(board.territoryInfo.additionalTroops[m].owner,
                                //     board.territoryInfo.additionalTroops[m].troops);
                                board.additionalTroops[board.territoryInfo.additionalTroops[m].owner] = board.territoryInfo.additionalTroops[m].troops;
                            }
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
        board.highlightMap = function(territoryNumber) {
            var index = territoryNumber - 1;
            var map = board.territoryDOMElements;
            if($(map[index]).hasClass('territoryClick') || $(map[index]).hasClass('territoryAttack') || $(map[index]).hasClass('territoryMoveTroops')) {
                //user wants to attack, move troops, or de-highlight
                board.userMapAction(index, board.territoryDOMElements);
                return;
            }
            board.removeAllPreviousAdjacencies();
            var adjacentTerritories = board.findValidAdjacencies(index);
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
                            board.calculateAdditionalTroops(1, index, input);
                        } else if(input.keyCode === 40) {
                            //down arrow
                            board.calculateAdditionalTroops(-1, index, input);
                        }
                });
            }
        };



        board.calculateAdditionalTroops = function(troopDelta, index, key) {
            key.preventDefault();
            var currentTroops = board.troops[index];
            var currentAdditionalTroops = board.additionalTroops[globalFunctions.getPlayerNumber()];
            if(currentTroops === 0 && troopDelta === -1 || currentAdditionalTroops === 0 && troopDelta === 1) {
                return;
            }
            currentTroops = currentTroops + troopDelta;
            currentAdditionalTroops = currentAdditionalTroops - troopDelta;
            board.additionalTroops[globalFunctions.getPlayerNumber()] = currentAdditionalTroops;
            board.updateTroopsOnTerritory(index, currentTroops, $("#map td"));
            globalFunctions.updateAdditionalTroops(globalFunctions.getPlayerNumber(), currentAdditionalTroops);
        };
        board.removeAllPreviousAdjacencies = function() {
            $("#map td").each(function(){
                if($(this).hasClass('territoryClick') || $(this).hasClass('territoryAttack') || $(this).hasClass('territoryMoveTroops')) {
                    $(this).removeClass('territoryClick territoryAttack territoryMoveTroops');
                }
            });
        };
        board.findValidAdjacencies = function(index) {
            var xPos = Math.floor(index / 5);
            var yPos = index % 5;
            var adjacentTerritories = [];
            var adjacentTerritoriesX = [xPos + 1, xPos - 1, xPos, xPos, xPos + 1, xPos + 1, xPos -1, xPos - 1];
            var adjacentTerritoriesY = [yPos, yPos, yPos + 1, yPos - 1, yPos + 1, yPos - 1, yPos + 1, yPos - 1];
            for(var k=0; k<adjacentTerritoriesX.length; k++) {
                if(adjacentTerritoriesX[k] < 0 || adjacentTerritoriesX[k] > 4 || adjacentTerritoriesY[k] < 0 || adjacentTerritoriesY[k] > 4) {
                    continue;
                }
                adjacentTerritories.push(board.territory2DArray[adjacentTerritoriesX[k]][adjacentTerritoriesY[k]]);
            }
            return adjacentTerritories;
        };
        board.userMapAction = function(index, map) {
            if($(map[index]).hasClass('territoryClick')) {
                board.removeAllPreviousAdjacencies(map);
                $(map[index]).addClass('territoryClick');   //need to add territoryClick class again because it will be toggled off in the click function
            } else if($(map[index]).hasClass('territoryMoveTroops')) {
                board.moveTroops(index, map);
            } else if($(map[index]).hasClass('territoryAttack')){
                board.attack(index, map);
            }
        };
        board.moveTroops = function(destination, map) {
            var origin = board.findOrigin(destination);
            var originTroops = board.troops[origin];
            var destinationTroops = board.troops[destination];
            if(originTroops > 0) {
                originTroops--;
                destinationTroops++;
                board.updateTroopsOnTerritory(origin, originTroops, map);
                board.updateTroopsOnTerritory(destination, destinationTroops, map);
            }
        };
        board.updateTroopsOnTerritory = function(index, troops, map) {
            board.troops[index] = troops;
            $(map[index]).children('p').children('span').html(troops);
        };
        board.attack = function(destination, map) {
            var origin = board.findOrigin(destination);
            var originTroops = board.troops[origin];
            var troopsAttacking = originTroops + 1;
            while(troopsAttacking > originTroops) {
                troopsAttacking = prompt("How many troops would you like to attack with?  You have " + originTroops + " available");
            }
            var attackArrowPosition = board.calculateArrowPosition($(board.territoryDOMElements[origin]).position(), $(board.territoryDOMElements[destination]).position());
            var preprendImageUrl = "https://cdn2.iconfinder.com/data/icons/ios-7-icons/50/";
            var appendImageUrl = "-128.png";
            var arrowDOM, attackTextDOM;
            if(troopsAttacking > 0) {
                arrowDOM = $("<img class='attackComponent' src='" + preprendImageUrl + attackArrowPosition.urlDirection + appendImageUrl + "'></img>").appendTo("body").css("top", attackArrowPosition.top + "px").css("left", attackArrowPosition.left + "px");
                attackTextDOM = $("<h3 class='attackComponent'></h3>").appendTo("body").html(troopsAttacking).css("top", attackArrowPosition.textTop + "px").css("left", attackArrowPosition.textLeft + "px").css("color", "red");
            }
            var troopsPreviouslyAttacking = board.updateAttackingTroops(origin, troopsAttacking, map, attackArrowPosition.urlDirection, arrowDOM, attackTextDOM, destination);
            board.updateTroopsOnTerritory(origin, board.troops[origin] - troopsAttacking + parseInt(troopsPreviouslyAttacking,10), map);
        };
        board.updateAttackingTroops = function(origin, troopsAttacking, map, direction, arrowDOM, textDOM, destination) {
            var result = board.attackingTroops[origin][direction];
            var troopsPreviouslyAttacking = result.troops;
            board.attackingTroops[origin][direction]["destination"] = destination;
            if(result.troops !== 0) {
                $(board.attackingTroops[origin][direction].arrowDOM).remove();
                $(board.attackingTroops[origin][direction].textDOM).remove();
            }
            board.attackingTroops[origin][direction].arrowDOM = arrowDOM;
            board.attackingTroops[origin][direction].textDOM = textDOM;
            board.attackingTroops[origin][direction].troops = troopsAttacking;
            return troopsPreviouslyAttacking;
        };
        board.findOrigin = function(destination) {
            var adjacencies = board.findValidAdjacencies(destination);
            for(var k=0; k<adjacencies.length; k++) {
                if($(board.territoryDOMElements[adjacencies[k]]).hasClass('territoryClick')) {
                    return adjacencies[k];
                }
            }
            return -1;
        };
        board.calculateArrowPosition = function(origin, destination) {
            var upDownArrowPadding = 60;
            var leftRightArrowPadding = 80;
            var heightOfTile = 97;
            var widthOfTile = 188;
            var result = {
                top: origin.top,
                left: origin.left,
                urlDirection: ' ',
                textTop: origin.top,
                textLeft: origin.left
            };
            if(origin.top > destination.top && origin.left === destination.left) {
                //ARROW UP
                result.top = result.top - upDownArrowPadding;
                result.left = result.left + (widthOfTile / 4);
                result.urlDirection = 'up';
                result.textTop = result.top + upDownArrowPadding;
                result.textLeft = result.left + widthOfTile/3;
            }
            if(origin.top < destination.top && origin.left === destination.left) {
                //ARROW DOWN
                result.top = result.top  + heightOfTile - upDownArrowPadding;
                result.left = result.left + (widthOfTile / 4);
                result.urlDirection = 'down';
                result.textTop = result.top + heightOfTile/4;
                result.textLeft = result.left + widthOfTile/4;
            }
            if(origin.top > destination.top && origin.left > destination.left) {
                //ARROW UP AND LEFT
                result.top = result.top - upDownArrowPadding;
                result.left = result.left -  leftRightArrowPadding;
                result.urlDirection = 'up_left';
                result.textTop = result.top + upDownArrowPadding;
                result.textLeft = result.left + leftRightArrowPadding;
            }
            if(origin.top < destination.top && origin.left < destination.left) {
                //ARROW DOWN AND RIGHT
                result.top = result.top  + heightOfTile - upDownArrowPadding;
                result.left = result.left +  widthOfTile - leftRightArrowPadding;
                result.urlDirection = 'down_right';
                result.textTop = result.top + heightOfTile/4;
                result.textLeft = result.left + widthOfTile/3;
            }
            if(origin.top > destination.top && origin.left < destination.left) {
                //ARROW UP AND RIGHT
                result.top = result.top - upDownArrowPadding;
                result.left = result.left +  widthOfTile - leftRightArrowPadding;
                result.urlDirection = 'up_right';
                result.textTop = result.top + upDownArrowPadding;
                result.textLeft = result.left + widthOfTile/3;
            }
            if(origin.top < destination.top && origin.left > destination.left) {
                //ARROW DOWN AND LEFT
                result.top = result.top  + heightOfTile - upDownArrowPadding;
                result.left = result.left -  leftRightArrowPadding;
                result.urlDirection = 'down_left';
                result.textTop = result.top + heightOfTile/4;
                result.textLeft = result.left + leftRightArrowPadding;
            }
            if(origin.top === destination.top && origin.left > destination.left) {
                //ARROW LEFT
                result.top = result.top  + heightOfTile/12;
                result.left = result.left -  leftRightArrowPadding;
                result.textTop = result.top  + heightOfTile/3;
                result.textLeft = result.left + leftRightArrowPadding;
                result.urlDirection = 'left';
            }
            if(origin.top === destination.top && origin.left < destination.left) {
                //ARROW RIGHT
                result.top = result.top  + heightOfTile/12;
                result.left = result.left +  widthOfTile - leftRightArrowPadding;
                result.textTop = result.top  + heightOfTile/3;
                result.textLeft = result.left + widthOfTile/3;
                result.urlDirection = 'right';
            }
            return result;
        };
        globalFunctions.destroyAndRebuildMap = function() {
            globalFunctions.setDisplayMap(true);
            board.territoryInfo = {};
            board.territoryOwner = [];
            board.territoryDOMElements = [];
            board.troops = [];
            board.attackingTroops = [];
            $("#map").empty();
            globalFunctions.createMap();
            var callMapReady = true;
            globalFunctions.getMap(callMapReady);
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
        globalFunctions.getMap();
        board.playerWatching();

        // $('.boardKnockout').each(function() {
        //     ko.applyBindings(new boardViewModel(), $(this).get(0));
        // });
    }
    window.Board = boardViewModel;
})(window.ko);

