// This code inside of the boardViewModel function is only loaded once the game is fully initialized
(function() {
    function boardViewModel(context) {
        var self = context;
        self.territoryInfo = {};
        self.getMap = function() {
            $(".displayPlayerColor").each(function(index) {
                $(this).append(self.colorList[index]);
            });
            $.ajax('/test/game/' + self.gameID + '/map', {
                method: 'GET',
                    }).done(function(result) {
                        self.territoryInfo = $.parseJSON(result);
                        $("#map td").each(function(index) {
                            $(this).attr("owner", self.territoryInfo.map[index].owner);
                            $(this).attr("territoryNumber", index + 1);
                            $(this).addClass("player" + self.territoryInfo.map[index].owner);
                            if(self.territoryInfo.map[index].owner === self.playerNumber) {
                                $(this).hover(function() {
                                    $(this).addClass("territoryHover");
                                }, function() {
                                    $(this).removeClass("territoryHover");
                                });
                                $(this).click(function() {
                                    self.highlightMap($(this).attr('territoryNumber'));
                                    $(this).toggleClass("territoryClick");
                                });
                            }
                            $(this).append("<p>troops: " + self.territoryInfo.map[index].troops + "</p>");
                        });
                });
        };
        self.getMap();
        self.highlightMap = function(territoryNumber) {
            var index = territoryNumber - 1;
            var map = $("#map td");
            if($(map[index]).hasClass('territoryClick') || $(map[index]).hasClass('territoryAttack') || $(map[index]).hasClass('territoryMoveTroops')) {
                //user wants to attack, move troops, or de-highlight
                self.userMapAction(index, map);
                return;
            }
            self.removeAllPreviousAdjacencies();
            var adjacentTerritories = self.findValidAdjacencies(index);
            for(var k=0; k<adjacentTerritories.length; k++) {
                if($(map[adjacentTerritories[k]]).attr('owner') != self.playerNumber) {
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
            } else {
                //user wants to attack
            }
        };
        self.moveTroops = function(destination, map) {
            console.log('moving troops to destination ' + destination);
        };
    }
    window.Board = boardViewModel;
})(window.ko);

