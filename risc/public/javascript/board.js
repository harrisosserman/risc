// This code inside of the boardViewModel function is only loaded once the game is fully initialized
(function(ko) {
    function boardViewModel(context) {
        var self = context;
        self.territoryInfo = {};
        self.territoryOwner = [];
        self.territoryDOMElements = [];
        self.troops = [];
        self.attackingTroops = [];
        self.getMap = function() {
            $(".displayPlayerColor").each(function(index) {
                $(this).append(self.colorList[index]);
            });
            $.ajax('/test/game/' + self.gameID + '/map', {
                method: 'GET',
                    }).done(function(result) {
                        self.territoryInfo = $.parseJSON(result);
                        var map = $("#map td");
                        $(map).each(function(index) {
                            self.attackingTroops.push({
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
                            self.territoryOwner.push(self.territoryInfo.map[index].owner);
                            self.troops.push(self.territoryInfo.map[index].troops);
                            self.territoryDOMElements.push($(this));
                            $(this).addClass("player" + self.territoryInfo.map[index].owner);
                            if(self.territoryInfo.map[index].owner === self.playerNumber) {
                                $(this).hover(function() {
                                    $(this).addClass("territoryHover");
                                }, function() {
                                    $(this).removeClass("territoryHover");
                                });
                                $(this).click(function() {
                                    self.highlightMap(index + 1);
                                    $(this).toggleClass("territoryClick");
                                });
                            } else {
                                $(this).click(function() {
                                    //click handler for clicking on enemy territory
                                    self.userMapAction(index, map);
                                });
                            }
                            $(this).append("<p class='troopTotals'>troops: <span>" + self.territoryInfo.map[index].troops + "</span></p>");
                        });
                });
        };
        self.getMap();
        self.highlightMap = function(territoryNumber) {
            var index = territoryNumber - 1;
            var map = self.territoryDOMElements;
            if($(map[index]).hasClass('territoryClick') || $(map[index]).hasClass('territoryAttack') || $(map[index]).hasClass('territoryMoveTroops')) {
                //user wants to attack, move troops, or de-highlight
                self.userMapAction(index, self.territoryDOMElements);
                return;
            }
            self.removeAllPreviousAdjacencies();
            var adjacentTerritories = self.findValidAdjacencies(index);
            for(var k=0; k<adjacentTerritories.length; k++) {
                if(self.territoryOwner[adjacentTerritories[k]] != self.playerNumber) {
                    $(map[adjacentTerritories[k]]).addClass('territoryAttack');
                } else {
                    $(map[adjacentTerritories[k]]).addClass('territoryMoveTroops');
                }
            }
        };
        self.removeAllPreviousAdjacencies = function() {
            $("#map td").each(function(){
                if($(this).hasClass('territoryClick') || $(this).hasClass('territoryAttack') || $(this).hasClass('territoryMoveTroops')) {
                    $(this).removeClass('territoryClick territoryAttack territoryMoveTroops');
                }
            });
        };
        self.findValidAdjacencies = function(index) {
            //NEED TO IMPROVE THIS FUNCTION.  FINDS SOME ADDJACENCIES THAT ARE NOT ACTUALLY ADJACENT
            var adjacentTerritories = [index + 1, index - 1, index - 5, index + 5, index + 6, index - 6, index + 4, index - 4];
            for(var k=0; k<adjacentTerritories.length; k++) {
                if(adjacentTerritories[k] < 0 || adjacentTerritories[k] > 24) {
                    adjacentTerritories.splice(k, 1);   //remove elements that are outside the bounds of the map
                }
            }
            return adjacentTerritories;
        };
        self.userMapAction = function(index, map) {
            if($(map[index]).hasClass('territoryClick')) {
                self.removeAllPreviousAdjacencies(map);
                $(map[index]).addClass('territoryClick');   //need to add territoryClick class again because it will be toggled off in the click function
            } else if($(map[index]).hasClass('territoryMoveTroops')) {
                self.moveTroops(index, map);
            } else if($(map[index]).hasClass('territoryAttack')){
                self.attack(index, map);
            }
        };
        self.moveTroops = function(destination, map) {
            var origin = self.findOrigin(destination, map);
            var originTroops = self.troops[origin];
            var destinationTroops = self.troops[destination];
            if(originTroops > 0) {
                originTroops--;
                destinationTroops++;
                self.updateTroopsOnTerritory(origin, originTroops, map);
                self.updateTroopsOnTerritory(destination, destinationTroops, map);
            }
        };
        self.updateTroopsOnTerritory = function(index, troops, map) {
            self.troops[index] = troops;
            $(map[index]).children('p').children('span').html(troops);
        };
        self.attack = function(destination, map) {
            var origin = self.findOrigin(destination, map);
            var originTroops = self.troops[origin];
            var troopsAttacking = originTroops + 1;
            while(troopsAttacking > originTroops) {
                troopsAttacking = prompt("How many troops would you like to attack with?  You have " + originTroops + " available");
            }
            var attackArrowPosition = self.calculateArrowPosition($(self.territoryDOMElements[origin]).position(), $(self.territoryDOMElements[destination]).position());
            var preprendImageUrl = "https://cdn2.iconfinder.com/data/icons/ios-7-icons/50/";
            var appendImageUrl = "-128.png";
            var arrowDOM, attackTextDOM;
            if(troopsAttacking > 0) {
                arrowDOM = $("<img class='attackComponent' src='" + preprendImageUrl + attackArrowPosition.urlDirection + appendImageUrl + "'></img>").appendTo("body").css("top", attackArrowPosition.top + "px").css("left", attackArrowPosition.left + "px");
                attackTextDOM = $("<h3 class='attackComponent'></h3>").appendTo("body").html(troopsAttacking).css("top", attackArrowPosition.textTop + "px").css("left", attackArrowPosition.textLeft + "px").css("color", "red");
            }
            var troopsPreviouslyAttacking = self.updateAttackingTroops(origin, troopsAttacking, map, attackArrowPosition.urlDirection, arrowDOM, attackTextDOM, destination);
            self.updateTroopsOnTerritory(origin, self.troops[origin] - troopsAttacking + parseInt(troopsPreviouslyAttacking,10), map);
        };
        self.updateAttackingTroops = function(origin, troopsAttacking, map, direction, arrowDOM, textDOM, destination) {
            var result = self.attackingTroops[origin][direction];
            var troopsPreviouslyAttacking = result.troops;
            self.attackingTroops[origin][direction]["destination"] = destination;
            if(result.troops !== 0) {
                $(self.attackingTroops[origin][direction].arrowDOM).remove();
                $(self.attackingTroops[origin][direction].textDOM).remove();
            }
            self.attackingTroops[origin][direction].arrowDOM = arrowDOM;
            self.attackingTroops[origin][direction].textDOM = textDOM;
            self.attackingTroops[origin][direction].troops = troopsAttacking;
            return troopsPreviouslyAttacking;
        };
        self.findOrigin = function(destination, map) {
            //UPDATE FIND ORIGIN SO THAT IT USES ADJACENT TERRITORIES TO FIND ORIGIN
            var originTerritory = -1;
            $(map).each(function(index){
                if($(this).hasClass('territoryClick')) {
                    originTerritory = index;
                    return false;
                }
            });
            return originTerritory;
        };
        self.calculateArrowPosition = function(origin, destination) {
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
        self.destroyAndRebuildMap = function() {
            self.displayMap(true);
            self.territoryInfo = {};
            self.territoryOwner = [];
            self.territoryDOMElements = [];
            self.troops = [];
            self.attackingTroops = [];
            $('.troopTotals').each(function() {
                $(this).remove();
            });
            $(".displayPlayerColor").each(function(index) {
                $(this).empty();
            });
            self.removeAllPreviousAdjacencies($('#map'));
            self.getMap();
        };
    }
    window.Board = boardViewModel;
})(window.ko);

