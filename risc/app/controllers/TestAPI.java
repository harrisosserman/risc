package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import controllers.routes;
import java.util.*;
import libraries.JSONLibrary.JSONObject;
import libraries.JSONLibrary.JSONArray;
import models.Game;

public class TestAPI extends Controller {
    public static int testCounterInGetTestGame = 0;
    public static int testCounterInGetTestMap = 0;
    public static boolean isHarrisReady = false;

    public static Result getTestUsername(String username) {
        //if username is in database, return successful.  otherwise, return failure
        if(username.compareTo("asdf") == 0) {
            return ok();
        }
        if(username.compareTo("harriso") == 0) {
            JSONObject result = new JSONObject();
            result.put("username", "harriso");
            JSONArray arr = new JSONArray();
            arr.put(new JSONObject().put("game", "gameID1"));
            arr.put(new JSONObject().put("game", "gameID2"));
            result.put("games", arr);
            return ok(result.toString());
        }
        return badRequest();
    }
    public static Result loginTest(String username) {
        if(username.compareTo("asdf") == 0) {
            return badRequest();
        }
        JSONObject result = new JSONObject();
        result.put("username", "harriso");
        JSONArray arr = new JSONArray();
        arr.put(new JSONObject().put("game", "gameID1"));
        arr.put(new JSONObject().put("game", "gameID2"));
        result.put("game", arr);
        return ok(result.toString());
    }
    public static Result createTestPlayer() {
        return ok();
    }
    public static Result createTestGame() {
        JSONObject result = new JSONObject();
        result.put("gameID", "gameID3");
        return ok(result.toString());
    }

    public static Result getTestGames() {
        JSONArray returnList = new JSONArray();
        JSONObject result = new JSONObject();
        result.put("gameID", "gameID4");
        result.put("state", 0);
        JSONArray list = new JSONArray();
        list.put(new JSONObject().put("name", "billybob").put("ready", false));
        list.put(new JSONObject().put("name", "juliant").put("ready", true));
        if(isHarrisReady == true) {
            list.put(new JSONObject().put("name", "harriso").put("ready", true));
        }
        result.put("players", list);
        returnList.put(result);

        result = new JSONObject();
        result.put("gameID", "gameID5");
        result.put("state", 0);
        list = new JSONArray();
        list.put(new JSONObject().put("name", "kkrieger").put("ready", false));
        list.put(new JSONObject().put("name", "rickybobby").put("ready", true));
        if(isHarrisReady == true) {
            list.put(new JSONObject().put("name", "harriso").put("ready", true));
        }
        result.put("players", list);
        returnList.put(result);

        return ok(returnList.toString());
    }

    public static Result getTestGame(String id) {
        if(id.compareTo("gameID1") == 0) {
            JSONObject result = new JSONObject();
            result.put("gameID", "gameID1");
            result.put("state", 1);
            JSONArray list = new JSONArray();
            list.put(new JSONObject().put("name", "harriso").put("ready", isHarrisReady));
            list.put(new JSONObject().put("name", "juliant").put("ready", isHarrisReady));
            result.put("players", list);
            return ok(result.toString());
        }
        if(id.compareTo("gameID2") == 0) {
            JSONObject result = new JSONObject();
            result.put("gameID", "gameID2");
            result.put("state", 0);
            JSONArray list = new JSONArray();
            list.put(new JSONObject().put("name", "harriso").put("ready", isHarrisReady));
            list.put(new JSONObject().put("name", "rickybobby").put("ready", isHarrisReady));
            list.put(new JSONObject().put("name", "magicman").put("ready", isHarrisReady));
            list.put(new JSONObject().put("name", "bladesofglory").put("ready", isHarrisReady));
            result.put("players", list);
            return ok(result.toString());
        }
        if(id.compareTo("gameID4") == 0) {
           JSONObject result = new JSONObject();
            result.put("gameID", "gameID4");
            result.put("state", 0);
            JSONArray list = new JSONArray();
            list.put(new JSONObject().put("name", "billybob").put("ready", isHarrisReady));
            list.put(new JSONObject().put("name", "juliant").put("ready", isHarrisReady));
            if(isHarrisReady == true) {
            list.put(new JSONObject().put("name", "harriso").put("ready", true));
            }
            result.put("players", list);
            return ok(result.toString());
        } else {
            JSONObject result = new JSONObject();
            result.put("gameID", "gameID5");
            result.put("state", 0);
            JSONArray list = new JSONArray();
            list.put(new JSONObject().put("name", "kkrieger").put("ready", isHarrisReady));
            list.put(new JSONObject().put("name", "rickybobby").put("ready", isHarrisReady));
            if(isHarrisReady == true) {
            list.put(new JSONObject().put("name", "harriso").put("ready", isHarrisReady));
            }
            result.put("players", list);
            return ok(result.toString());
        }
    }
    public static Result startTestGame(String id) {
        isHarrisReady = true;
        return ok();
    }

    // public static Result getTestGame(Long id) {
    //     JSONObject result = new JSONObject();
    //     result.put("gameID", "3938383");
    //     JSONArray names = new JSONArray();
    //     if(testCounterInGetTestGame < 10) {
    //         names.put(new JSONObject().put("name", "Kat").put("ready", "false"));
    //     } else {
    //         names.put(new JSONObject().put("name", "Kat").put("ready", "true"));
    //     }
    //     if(testCounterInGetTestGame < 5) {
    //         names.put(new JSONObject().put("name", "Julian").put("ready", "false"));
    //     } else {
    //         names.put(new JSONObject().put("name", "Julian").put("ready", "true"));
    //     }
    //     testCounterInGetTestGame++;
    //     names.put(new JSONObject().put("name", "Harris").put("ready", isHarrisReady));
    //     result.put("players", names);
    //     return ok(result.toString());
    // }

    public static Result getTestMap(String id, String username) {
        JSONObject result = new JSONObject();
        result.put("gameID", "3938383");
        result.put("numPlayers", 3);
        JSONArray map = new JSONArray();
        map.put(new JSONObject().put("position", 7).put("owner", "harriso").put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        map.put(new JSONObject().put("position", 3).put("owner", "harriso").put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        map.put(new JSONObject().put("position", 5).put("owner", "rickybobby").put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        map.put(new JSONObject().put("position", 1).put("owner", "harriso").put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 2).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 2).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 3).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 3).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 2).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 2).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 3).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 3).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 2).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 2).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 3).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 3).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 2).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        // map.put(new JSONObject().put("owner", 1).put("INFANTRY", 5).put("AUTOMATIC", 10).put("ROCKETS", 15).put("TANKS", 20).put("IMPROVEDTANKS", 25).put("PLANES", 30).put("food", 10).put("technology", 30));
        result.put("territories", map);
        JSONArray spies = new JSONArray();
        spies.put(new JSONObject().put("owner", "harriso").put("position", 1).put("percentage", 15).put("previousType", "AUTOMATIC"));
        spies.put(new JSONObject().put("owner", "harriso").put("position", 1).put("percentage", 15).put("previousType", "INFANTRY"));
        result.put("spies", spies);
        JSONArray additionalInfo = new JSONArray();
        additionalInfo.put(new JSONObject().put("owner", "rickybobby").put("level", 2).put("playerNumber", 0));
        additionalInfo.put(new JSONObject().put("owner", "magicman").put("level", 3).put("playerNumber", 1));
        ArrayList<String> alliesList = new ArrayList<String>();
        alliesList.add("rickybobby");
        JSONArray allies = new JSONArray(alliesList);
        additionalInfo.put(new JSONObject().put("owner", "harriso").put("allies", allies).put("level", 7).put("food", 35).put("technology", 100).put("additionalInfantry", 50).put("playerNumber", 2));
        result.put("playerInfo", additionalInfo);
        result.put("notifyNukesAvailable", true);
        result.put("canUseNukes", true);
        return ok(result.toString());
    }

    public static Result getTestMapReady(String id) {
        return getTestMap(id, "");
    }

    public static Result commitTestTurn(String id) {
        return ok();
    }

    public static Result playerWinsMap() {
        JSONObject result = new JSONObject();
        result.put("gameID", "3938383");
        result.put("numPlayers", 3);
        JSONArray map = new JSONArray();
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));

        result.put("map", map);
        //DEAL WITH ADDITIONAL TROOPS LATER
        return ok(result.toString());
    }

    public static Result getTestMapPolling(String id) {
        testCounterInGetTestMap++;
        if(testCounterInGetTestMap > 10) {
            return playerWinsMap();
        }
        if(testCounterInGetTestMap == 10) {
            return getTestMap(id, "");
        }
        return status(400, "map not ready");
    }
}
