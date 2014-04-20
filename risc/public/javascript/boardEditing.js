function BoardEditing(globals) {
    var globalFunctions = globals;
    var editing = this;
    editing.moveOrder = [];
    editing.territory2DArray = [[0, 1, 2, 3, 4], [5, 6, 7, 8, 9], [10, 11, 12, 13, 14], [15, 16, 17, 18, 19], [20, 21, 22, 23, 24]];
    editing.spyDowngrades = [];
    editing.spiesCannotMove = [];
    // GLOBAL FUNCTIONS
    globalFunctions.getMoveOrder = function() {
        return editing.moveOrder;
    };
    // END GLOBAL FUNCTIONS
    editing.constructSpyDowngrades = function() {
        for(var k=0; k<25; k++) {
            editing.spyDowngrades[k] = [];
        }
    };
    editing.clearSpyDowngrades = function() {
        editing.spyDowngrades = [];
    };
    editing.constructSpiesCannotMove = function() {
        for(var k = 0; k<25; k++) {
            editing.spiesCannotMove[k] = 0;
        }
    };
    editing.clearSpiesCannotMove = function() {
        editing.spiesCannotMove = [];
    };
    editing.removeAllMoves = function() {
        editing.moveOrder = [];
    };
    editing.moveSpyDowngradeObjects = function(origin, destination) {
        var downgradesList = editing.spyDowngrades[origin];
        for(var k=0; k<downgradesList.length; k++) {
            editing.spyDowngrades[destination].push(downgradesList[k]);
        }
    };
    editing.downgradeSpies = function(index, troopType) {
        var downgradesList = editing.spyDowngrades[index];
        for(var k=0; k<downgradesList.length; k++) {
            if(downgradesList[k].troopType === troopType) {
                if(downgradesList[k].numTroops > 0) {
                    downgradesList[k].numTroops--;
                    return 1;
                }
            }
        }
        return -1;
    };
    editing.convertTextForTroopCommit = function(input) {
        if(input === 0) {
            return 'INFANTRY';
        } else if(input === 1) {
            return 'AUTOMATIC';
        } else if(input === 2) {
            return 'ROCKETS';
        } else if(input === 3) {
            return 'TANKS';
        } else if(input === 4) {
            return 'IMPROVEDTANKS';
        } else if(input === 5) {
            return 'PLANES';
        } else if(input === 6) {
            return 'SPIES';
        } else if(input === 7) {
            return 'NUKES';
        } else if(input === 8) {
            return "INTERCEPTOR";
        } else if(input === 9) {
            return 'TECHNOLOGY';
        } else if(input === 10) {
            return 'TERRITORY';
        }
        return 'FOOD';
    };
    editing.convertTextFromCapitalsToIndex = function(input) {
        if(input === 'INFANTRY') {
            return 0;
        } else if(input === 'AUTOMATIC') {
            return 1;
        } else if(input === 'ROCKETS') {
            return 2;
        } else if(input === 'TANKS') {
            return 3;
        } else if(input === 'IMPROVEDTANKS') {
            return 4;
        } else if(input === 'PLANES') {
            return 5;
        }
        return 6;
    };
    editing.addMove = function(moveType, start, end, troopType, upgradeType, formAlliance) {
        troopType = editing.convertTextForTroopCommit(troopType);
        upgradeType = editing.convertTextForTroopCommit(upgradeType);
        var result = {};
        if(moveType === 0) {
            result = {
                moveType: 0,
                position: start,
                troopType: troopType,
                upgradeType: upgradeType
            };
            editing.moveOrder.push(result);
        } else if(moveType === 1 || moveType === 2) {
            result = {
                moveType: moveType,
                troopType: troopType,
                start: start,
                end: end
            };
            editing.moveOrder.push(result);
        } else if(moveType === 3){
            result = {
                moveType: moveType,
                position: start,
                troopType: troopType
            };
            editing.moveOrder.push(result);
        } else if(moveType === 4) {
            result = {
                moveType: moveType,
                offer: start
            };
            editing.moveOrder.push(result);
        } else if(moveType === 5) {
            result = {
                moveType: 5,
                formAlliance: formAlliance,
                owner: start,
                ally: end
            };
            editing.moveOrder.push(result);
        }
        globalFunctions.commitTurn(midTurn=true);
    };
    editing.removeAdditionalTroop = function(moveType, position, troopType) {
        troopType = editing.convertTextForTroopCommit(troopType);
        for(var k=0; k<editing.moveOrder.length; k++) {
            var move = editing.moveOrder[k];
            if(move.moveType === moveType && move.position === position && move.troopType === troopType) {
                editing.moveOrder.splice(k, 1);
                break;
            }
        }
        globalFunctions.commitTurn(midTurn=true);
    };
    editing.removeTradeFromMovesList = function(index) {
        var foundTrades=0;
        for(var k=0; k<editing.moveOrder.length; k++) {
            if(editing.moveOrder[k].moveType === 4) {
                if(foundTrades === index) {
                    editing.moveOrder.splice(k, 1);
                    break;
                } else {
                    foundTrades++;
                }
            }
        }
        globalFunctions.commitTurn(midTurn=true);
    };
    editing.removeAttack = function(moveType, start, end, troopType, numberOfMoves) {
        troopType = editing.convertTextForTroopCommit(troopType);
        var indicesToRemove = [];

        for(var k=editing.moveOrder.length - 1; k>-1; k--) {
            if(numberOfMoves <= 0) {
                return;
            }
            var move = editing.moveOrder[k];
            if(move.moveType === moveType && move.start === start && move.end === end && move.troopType === troopType) {
                // editing.moveOrder.splice(k, 1);
                indicesToRemove.push(k);
                numberOfMoves--;
            }
        }

        for(var m=0; m<indicesToRemove.length; m++) {
            editing.moveOrder.splice(indicesToRemove[m], 1);
        }
        globalFunctions.commitTurn(midTurn=true);

    };
    editing.calculateAdditionalTroops = function(troopDelta, index, key, infantry, additionalInfantry) {
        key.preventDefault();
        var currentInfantry = infantry[index];
        var currentAdditionalTroops = additionalInfantry[globalFunctions.getPlayerNumber() - 1];
        if(currentInfantry === 0 && troopDelta === -1 || currentAdditionalTroops === 0 && troopDelta === 1) {
            return;
        }
        currentInfantry = currentInfantry + troopDelta;
        currentAdditionalTroops = currentAdditionalTroops - troopDelta;
        additionalInfantry[globalFunctions.getPlayerNumber() - 1] = currentAdditionalTroops;
        infantry[index] = currentInfantry;
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
    editing.editAlliance = function(owner, ally, addAlly) {
        editing.addMove(5, owner, ally, "", "", addAlly);
    };
    editing.addTrade = function(trade) {
        for(var k=0; k<trade.length; k++) {
            var tradeType = trade[k].type;
            tradeType = editing.convertTextForTroopCommit(globalFunctions.convertReadableText(tradeType).index);
            trade[k].type = tradeType;
        }
        editing.addMove(4, trade);
    };
    editing.moveTroops = function(destination, map, territoryDOMElements, troopArray, numberOfTroopsMoved, troopType, territoryOwner) {
        var origin = editing.findOrigin(destination, territoryDOMElements);
        var originTroops = troopArray[origin];
        if(typeof troopArray[destination] === 'undefined') {
            troopArray[destination] = 0;
        }
        if(territoryOwner[origin] !== globalFunctions.getUsername() && troopType != 6) {
            alert("You can only move spies from an enemy territory");
            return;
        }
        var destinationTroops = troopArray[destination];

        if(originTroops - numberOfTroopsMoved >= 0 || numberOfTroopsMoved < 0) {
            if(troopType === 6) {
                if(territoryOwner[destination] != globalFunctions.getUsername()) {
                    if(troopArray[origin] - editing.spiesCannotMove[origin] < numberOfTroopsMoved) {
                        alert("You have already moved some of the requested spies this turn");
                        return;
                    } else {
                        editing.spiesCannotMove[destination] = editing.spiesCannotMove[destination] + numberOfTroopsMoved;
                    }
                } else {
                    var resultingUnmovableSpies = editing.spiesCannotMove[origin] - numberOfTroopsMoved;
                    if(resultingUnmovableSpies < 0) {
                        editing.spiesCannotMove[origin] = 0;
                        editing.spiesCannotMove[destination] = editing.spiesCannotMove[destination] + numberOfTroopsMoved + resultingUnmovableSpies;
                    } else {
                        editing.spiesCannotMove[origin] = editing.spiesCannotMove[origin] - numberOfTroopsMoved;
                        editing.spiesCannotMove[destination] = editing.spiesCannotMove[destination] + numberOfTroopsMoved;
                    }
                }
                editing.moveSpyDowngradeObjects(origin, destination);
            }
            originTroops = originTroops - numberOfTroopsMoved;
            destinationTroops = parseInt(destinationTroops, 10) + parseInt(numberOfTroopsMoved, 10);
            troopArray[origin] = originTroops;
            troopArray[destination] = destinationTroops;
            for(var k=0; k<numberOfTroopsMoved; k++) {
                editing.addMove(1, origin, destination, troopType, -1);
            }
        } else {
            alert("You only have " + originTroops + " and are trying to move " + numberOfTroopsMoved);
        }

    };
    editing.nukeTerritory = function(index, playerInfo) {
        console.log(playerInfo);
        if(playerInfo.technology < 50) {
            alert("You need at least 50 technology to nuke a territory");
            return;
        }
        playerInfo.technology = playerInfo.technology - 50;
        editing.addMove(2, -1, index, 7);
        $($("#map td")[index]).addClass('territoryNuke');
        $($(".nukeButton")[index]).prop('disabled', true);
    };
    editing.upgradeTechLevel = function(playerInfo) {
        if(playerInfo.maxTechLevel === 6) {
            alert("You are already on the maximum technology level");
            return false;
        } else if(playerInfo.technology >= globalFunctions.getTechnologyLevelCost()[playerInfo.maxTechLevel + 1]) {
            //able to issue upgrade request
            playerInfo.maxTechLevel++;
            playerInfo.technology = playerInfo.technology - globalFunctions.getTechnologyLevelCost()[playerInfo.maxTechLevel];
            return true;
        } else {
            alert("Unable to issue upgrade request.  You don't have enough technology");
            return false;
        }
    };
    editing.upgradeTroops = function(origin, convertFromTroops, convertToTroops, playerInfo, numberOfTroopsConverting, troopTypeConvertFrom, troopTypeConvertTo) {
        var cost = (globalFunctions.getUnitUpgradeCost()[troopTypeConvertTo.index] - globalFunctions.getUnitUpgradeCost()[troopTypeConvertFrom.index]) * numberOfTroopsConverting;
        if(convertFromTroops[origin] < numberOfTroopsConverting) {
            alert("You don't have enough " + troopTypeConvertFrom.text + " to convert the troops");
            return;
        }
        if(playerInfo.maxTechLevel < troopTypeConvertTo.index && troopTypeConvertTo.index != 6 && troopTypeConvertFrom.index != 6) {
            alert("Your technology level is lower than the selected troop upgrade level");
            return;
        } else if(troopTypeConvertFrom.index === 6) {
            cost = 5 * numberOfTroopsConverting;
            if(cost > playerInfo.technology) {
                alert("You need " + cost + " technology, but you only have " + playerInfo.technology);
                return;
            }
            for(var k=0; k<numberOfTroopsConverting; k++) {
                if(editing.downgradeSpies(origin, troopTypeConvertTo.index) === -1) {
                    numberOfTroopsConverting = k;
                    alert("You don't have enough " + editing.convertTextForTroopCommit(troopTypeConvertTo.index) + " to fill the entire downgrade");
                    break;
                }
            }
        } else if(troopTypeConvertFrom.index > troopTypeConvertTo.index){
            alert("You cannot convert troops from a higher technology level to a lower technology level");
            return;
        } else if(cost > playerInfo.technology) {
            alert("You need " + cost + " technology, but you only have " + playerInfo.technology);
            return;
        }
        playerInfo.technology = playerInfo.technology - cost;
        convertFromTroops[origin] = parseInt(convertFromTroops[origin], 10) - parseInt(numberOfTroopsConverting, 10);
        convertToTroops[origin] = parseInt(convertToTroops[origin], 10) + parseInt(numberOfTroopsConverting, 10);
        for(var k=0; k<numberOfTroopsConverting; k++) {
            editing.addMove(0, origin, -1, troopTypeConvertFrom.index, troopTypeConvertTo.index);
        }
        if(troopTypeConvertTo.index === 6) {
            editing.spyDowngrades[origin].push({
                'troopType': troopTypeConvertFrom.index,
                'numTroops': numberOfTroopsConverting
            });
        }
    };
    editing.constructSpyDowngradesAtStart = function(spies, spiesTerritoryList) {
        if(typeof spies == 'undefined') {
            return;
        }
        for(var k=0; k<spies.length; k++) {
            var spyDowngrade = {
                'troopType': editing.convertTextFromCapitalsToIndex(spies[k].previousType),
                'numTroops': 1
            };
            editing.spyDowngrades[spies[k].position].push(spyDowngrade);
            spiesTerritoryList[spies[k].position] = spiesTerritoryList[spies[k].position] + 1;
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
            var territoryLocation = editing.territory2DArray[adjacentTerritoriesX[k]][adjacentTerritoriesY[k]];
            var territoryDOMElement = $("#map td")[territoryLocation];
            adjacentTerritories.push(editing.territory2DArray[adjacentTerritoriesX[k]][adjacentTerritoriesY[k]]);
        }
        return adjacentTerritories;
    };
    editing.updateAttackingTroops = function(origin, destination, attackingTroops, troopArray, troopType, numberOfTroopsAttacking) {
        numberOfTroopsAttacking = parseInt(numberOfTroopsAttacking, 10);
        var data = {
                destination: destination,
                infantry: 0,
                automatic: 0,
                rocket: 0,
                tank: 0,
                improvedTank: 0,
                plane: 0
            };
        data[troopType.text] = numberOfTroopsAttacking;
        if(typeof attackingTroops[origin] != 'undefined') {
            for(var k=0; k<attackingTroops[origin].length; k++) {
                if(attackingTroops[origin][k].destination === destination) {
                    var troopsPreviouslyAttacking = attackingTroops[origin][k][troopType.text];
                    editing.removeAttack(2, origin, destination, troopType.index, troopsPreviouslyAttacking);
                    troopArray[origin] = troopArray[origin] + troopsPreviouslyAttacking;
                    attackingTroops[origin][k][troopType.text] = numberOfTroopsAttacking;
                    return;
                }
            }
            attackingTroops[origin].push(data);
        } else {
            attackingTroops[origin] = [];
            attackingTroops[origin].push(data);
        }
    };
    editing.attack = function(destination, map, territoryDOMElements, troopArray, attackingTroops, numberOfTroopsAttacking, troopType, territoryOwner) {
        var origin = editing.findOrigin(destination, territoryDOMElements);
        var originTroops = troopArray[origin];
        if(originTroops < numberOfTroopsAttacking || numberOfTroopsAttacking < 0) {
            alert("You don't have enough troops to do that attack.  You have " + originTroops + " and are trying to attack with " + numberOfTroopsAttacking);
            return;
        }
        if(troopType.index === 6) {
            //a spy is trying to move into an enemy territory
            editing.moveTroops(destination, map, territoryDOMElements, troopArray, numberOfTroopsAttacking, troopType.index, territoryOwner);
            return;
        } else {
            if(territoryOwner[origin] !== globalFunctions.getUsername()) {
                alert("You can only move spies from an enemy territory");
                return;
            }
        }
        troopArray[origin] = parseInt(originTroops, 10) - parseInt(numberOfTroopsAttacking, 10);
        editing.updateAttackingTroops(origin, destination, attackingTroops, troopArray, troopType, numberOfTroopsAttacking);
        for(var k=0; k<numberOfTroopsAttacking; k++) {
            editing.addMove(2, origin, destination, troopType.index, -1);
        }
    };
    editing.constructSpiesCannotMove();
    editing.constructSpyDowngrades();
    return editing;

}