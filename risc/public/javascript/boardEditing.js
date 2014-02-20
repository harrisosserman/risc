function BoardEditing(globals) {
    var globalFunctions = globals;
    var editing = this;
    editing.territory2DArray = [[0, 1, 2, 3, 4], [5, 6, 7, 8, 9], [10, 11, 12, 13, 14], [15, 16, 17, 18, 19], [20, 21, 22, 23, 24]];

    editing.calculateAdditionalTroops = function(troopDelta, index, key, troopsArray, additionalTroopsArray) {
        key.preventDefault();
        var currentTroops = troopsArray[index];
        var currentAdditionalTroops = additionalTroopsArray[globalFunctions.getPlayerNumber()];
        if(currentTroops === 0 && troopDelta === -1 || currentAdditionalTroops === 0 && troopDelta === 1) {
            return;
        }
        currentTroops = currentTroops + troopDelta;
        currentAdditionalTroops = currentAdditionalTroops - troopDelta;
        additionalTroopsArray[globalFunctions.getPlayerNumber()] = currentAdditionalTroops;
        editing.updateTroopsOnTerritory(index, currentTroops, $("#map td"), troopsArray);
        // globalFunctions.updateAdditionalTroops(globalFunctions.getPlayerNumber(), currentAdditionalTroops);
    };
    editing.updateTroopsOnTerritory = function(index, troops, map, troopArray) {
        troopArray[index] = troops;
        $(map[index]).children('p').children('span').html(troops);
    };
    editing.findOrigin = function(destination, territoryDOMElements) {
        var adjacencies = editing.findValidAdjacencies(destination);
        for(var k=0; k<adjacencies.length; k++) {
            if($(territoryDOMElements[adjacencies[k]]).hasClass('territoryClick')) {
                return adjacencies[k];
            }
        }
        return -1;
    };
    editing.moveTroops = function(destination, map, territoryDOMElements, troopArray) {
        var origin = editing.findOrigin(destination, territoryDOMElements);
        var originTroops = troopArray[origin];
        var destinationTroops = troopArray[destination];
        if(originTroops > 0) {
            originTroops--;
            destinationTroops++;
            editing.updateTroopsOnTerritory(origin, originTroops, map, troopArray);
            editing.updateTroopsOnTerritory(destination, destinationTroops, map, troopArray);
        }
    };
    editing.removeAllPreviousAdjacencies = function() {
        $("#map td").each(function(){
            if($(this).hasClass('territoryClick') || $(this).hasClass('territoryAttack') || $(this).hasClass('territoryMoveTroops')) {
                $(this).removeClass('territoryClick territoryAttack territoryMoveTroops');
            }
        });
    };
    editing.findValidAdjacencies = function(index) {
        var xPos = Math.floor(index / 5);
        var yPos = index % 5;
        var adjacentTerritories = [];
        var adjacentTerritoriesX = [xPos + 1, xPos - 1, xPos, xPos, xPos + 1, xPos + 1, xPos -1, xPos - 1];
        var adjacentTerritoriesY = [yPos, yPos, yPos + 1, yPos - 1, yPos + 1, yPos - 1, yPos + 1, yPos - 1];
        for(var k=0; k<adjacentTerritoriesX.length; k++) {
            if(adjacentTerritoriesX[k] < 0 || adjacentTerritoriesX[k] > 4 || adjacentTerritoriesY[k] < 0 || adjacentTerritoriesY[k] > 4) {
                continue;
            }
            adjacentTerritories.push(editing.territory2DArray[adjacentTerritoriesX[k]][adjacentTerritoriesY[k]]);
        }
        return adjacentTerritories;
    };
    editing.updateAttackingTroops = function(origin, troopsAttacking, map, direction, arrowDOM, textDOM, destination, attackingTroops) {
        var result = attackingTroops[origin][direction];
        var troopsPreviouslyAttacking = result.troops;
        attackingTroops[origin][direction]["destination"] = destination;
        if(result.troops !== 0) {
            $(attackingTroops[origin][direction].arrowDOM).remove();
            $(attackingTroops[origin][direction].textDOM).remove();
        }
        attackingTroops[origin][direction].arrowDOM = arrowDOM;
        attackingTroops[origin][direction].textDOM = textDOM;
        attackingTroops[origin][direction].troops = troopsAttacking;
        return troopsPreviouslyAttacking;
    };
    editing.attack = function(destination, map, territoryDOMElements, troops, attackingTroops) {
        var origin = editing.findOrigin(destination, territoryDOMElements);
        var originTroops = troops[origin];
        var troopsAttacking = originTroops + 1;
        while(troopsAttacking > originTroops) {
            troopsAttacking = prompt("How many troops would you like to attack with?  You have " + originTroops + " available");
        }
        var attackArrowPosition = editing.calculateArrowPosition($(territoryDOMElements[origin]).position(), $(territoryDOMElements[destination]).position());
        var preprendImageUrl = "https://cdn2.iconfinder.com/data/icons/ios-7-icons/50/";
        var appendImageUrl = "-128.png";
        var arrowDOM, attackTextDOM;
        if(troopsAttacking > 0) {
            arrowDOM = $("<img class='attackComponent' src='" + preprendImageUrl + attackArrowPosition.urlDirection + appendImageUrl + "'></img>").appendTo("body").css("top", attackArrowPosition.top + "px").css("left", attackArrowPosition.left + "px");
            attackTextDOM = $("<h3 class='attackComponent'></h3>").appendTo("body").html(troopsAttacking).css("top", attackArrowPosition.textTop + "px").css("left", attackArrowPosition.textLeft + "px").css("color", "red");
        }
        var troopsPreviouslyAttacking = editing.updateAttackingTroops(origin, troopsAttacking, map, attackArrowPosition.urlDirection, arrowDOM, attackTextDOM, destination, attackingTroops);
        editing.updateTroopsOnTerritory(origin, troops[origin] - troopsAttacking + parseInt(troopsPreviouslyAttacking,10), map, troops);
    };
    editing.calculateArrowPosition = function(origin, destination) {
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
    return editing;

}