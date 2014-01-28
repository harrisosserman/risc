// This code inside of the boardViewModel function is only loaded once the game is fully initialized
(function() {
    function boardViewModel(context) {
        var self = context;
        self.territoryInfo = {};
        self.territoryOwner = [];
        self.territoryDOMElements = [];
        self.troops = [];
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
                            $(this).append("<p >troops: <span>" + self.territoryInfo.map[index].troops + "</span></p>");
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
                self.troops[origin] = originTroops;
                self.troops[destination] = destinationTroops;
                $(map[origin]).children('p').children('span').html(originTroops);
                $(map[destination]).children('p').children('span').html(destinationTroops);
            }
        };
        self.attack = function(destination, map) {
            var origin = self.findOrigin(destination, map);
            var originTroops = self.troops[origin];
            var troopsAttacking = originTroops + 1;
            while(troopsAttacking > originTroops) {
                troopsAttacking = prompt("How many troops would you like to attack with?  You have " + originTroops + " available");
            }
            var attackArrowPosition = self.calculateArrowPosition($(self.territoryDOMElements[origin]).position(), $(self.territoryDOMElements[destination]).position());
            // var mapAppendArrow = $('#map').append("<span class='attackArrow glyphicon " + attackArrowPosition.class + "' style='top: " + attackArrowPosition.top + "px; left: " + attackArrowPosition.left + "px'>HELLO</span>");
            var preprendImageUrl = "https://cdn2.iconfinder.com/data/icons/ios-7-icons/50/";
            var appendImageUrl = "-128.png";
            var mapAppendArrow = $("<img class='attackArrow' src='" + preprendImageUrl + attackArrowPosition.urlDirection + appendImageUrl + "'></img>").appendTo("body").css("top", attackArrowPosition.top + "px").css("left", attackArrowPosition.left + "px");
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
                urlDirection: ' '
            };
            if(origin.top > destination.top && origin.left === destination.left) {
                //ARROW UP
                result.top = result.top - upDownArrowPadding;
                result.left = result.left + (widthOfTile / 4);
                result.urlDirection = 'up';
            }
            if(origin.top < destination.top && origin.left === destination.left) {
                //ARROW DOWN
                result.top = result.top  + heightOfTile - upDownArrowPadding;
                result.left = result.left + (widthOfTile / 4);
                result.urlDirection = 'down';
            }
            if(origin.top > destination.top && origin.left > destination.left) {
                //ARROW UP AND LEFT
                console.log('arrow up and left');
            }
            if(origin.top < destination.top && origin.left < destination.left) {
                //ARROW DOWN AND RIGHT
                console.log('arrow down and right');
            }
            if(origin.top > destination.top && origin.left < destination.left) {
                //ARROW UP AND RIGHT
                console.log('arrow up and right');
            }
            if(origin.top < destination.top && origin.left > destination.left) {
                //ARROW DOWN AND LEFT
                console.log('arrow down and left');
            }
            if(origin.top === destination.top && origin.left > destination.left) {
                //ARROW LEFT
                result.top = result.top  + heightOfTile/12;
                result.left = result.left -  leftRightArrowPadding;
                result.urlDirection = 'left';
            }
            if(origin.top === destination.top && origin.left < destination.left) {
                //ARROW RIGHT
                result.top = result.top  + heightOfTile/12;
                result.left = result.left +  widthOfTile - leftRightArrowPadding;
                result.urlDirection = 'right';
            }
            return result;
        };
    }
    window.Board = boardViewModel;
})(window.ko);

