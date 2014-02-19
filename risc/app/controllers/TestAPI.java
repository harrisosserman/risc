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
    public static String isHarrisReady = "false";

    public static Result getTestUsername(String username) {
        //if username is in database, return successful.  otherwise, return failure
        if(username.compareTo("asdf") == 0) {
            return ok();
        }
        if(username.compareTo("harriso") == 0) {
            JSONObject result = new JSONObject();
            result.put("username", "harriso");
            JSONArray arr = new JSONArray();
            arr.put(new JSONObject().put("game", "asdfadsfas"));
            arr.put(new JSONObject().put("game", "asdfadsfasasdf"));
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
        result.put("username", "harris").put("password", "28394u298urwhiurwehaiufdsh");
        JSONArray arr = new JSONArray();
        arr.put(new JSONObject().put("game", "asdfjaslfdsa"));
        arr.put(new JSONObject().put("game", "fdsafsadfsadsf"));
        result.put("game", arr);
        return ok(result.toString());
    }
    public static Result createTestPlayer() {
        return ok();
    }
    public static Result createTestGame() {
        JSONObject result = new JSONObject();
        result.put("gameID", "3938383");
        result.put("playerId", 1);
        return ok(result.toString());
    }

    public static Result getTestGames() {
        JSONArray returnList = new JSONArray();
        JSONObject result = new JSONObject();
        result.put("gameID", "asdfadsfas");
        result.put("state", 0);
        JSONArray list = new JSONArray();
        list.put(new JSONObject().put("player", "billybob").put("ready", false));
        list.put(new JSONObject().put("player", "juliant").put("ready", true));
        result.put("players", list);
        returnList.put(result);

        result = new JSONObject();
        result.put("gameID", "asdfasfas");
        result.put("state", 0);
        list = new JSONArray();
        list.put(new JSONObject().put("player", "kkrieger").put("ready", false));
        list.put(new JSONObject().put("player", "rickybobby").put("ready", true));
        result.put("players", list);
        returnList.put(result);

        return ok(returnList.toString());
    }

    public static Result getTestGame(String id) {
        if(id.compareTo("asdfadsfas") == 0) {
            JSONObject result = new JSONObject();
            result.put("gameID", "asdfadsfas");
            result.put("state", 1);
            JSONArray list = new JSONArray();
            list.put(new JSONObject().put("player", "harriso").put("ready", false));
            list.put(new JSONObject().put("player", "juliant").put("ready", true));
            result.put("players", list);
            return ok(result.toString());
        }
        JSONObject result = new JSONObject();
        result.put("gameID", "asdfasfasasdf");
        result.put("state", 0);
        JSONArray list = new JSONArray();
        list.put(new JSONObject().put("player", "harriso").put("ready", false));
        list.put(new JSONObject().put("player", "rickybobby").put("ready", true));
        list.put(new JSONObject().put("player", "magicman").put("ready", false));
        list.put(new JSONObject().put("player", "bladesofglory").put("ready", true));
        result.put("players", list);
        return ok(result.toString());
    }
    public static Result startTestGame(Long id) {
        isHarrisReady = "true";
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

    public static Result getTestMap(Long id) {
        JSONObject result = new JSONObject();
        result.put("gameID", "3938383");
        result.put("numPlayers", 3);
        JSONArray map = new JSONArray();
        map.put(new JSONObject().put("owner", 1).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 3).put("troops", 5));
        map.put(new JSONObject().put("owner", 3).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 1).put("troops", 5));
        map.put(new JSONObject().put("owner", 1).put("troops", 2));
        map.put(new JSONObject().put("owner", 2).put("troops", 2));
        map.put(new JSONObject().put("owner", 3).put("troops", 2));
        map.put(new JSONObject().put("owner", 3).put("troops", 2));
        map.put(new JSONObject().put("owner", 2).put("troops", 2));
        map.put(new JSONObject().put("owner", 1).put("troops", 2));
        map.put(new JSONObject().put("owner", 1).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 3).put("troops", 5));
        map.put(new JSONObject().put("owner", 3).put("troops", 5));
        map.put(new JSONObject().put("owner", 2).put("troops", 5));
        map.put(new JSONObject().put("owner", 1).put("troops", 5));
        map.put(new JSONObject().put("owner", 1).put("troops", 2));
        map.put(new JSONObject().put("owner", 2).put("troops", 2));
        map.put(new JSONObject().put("owner", 3).put("troops", 2));
        map.put(new JSONObject().put("owner", 3).put("troops", 2));
        map.put(new JSONObject().put("owner", 2).put("troops", 2));
        map.put(new JSONObject().put("owner", 1).put("troops", 2));
        map.put(new JSONObject().put("owner", 1).put("troops", 10));
        result.put("territories", map);
        JSONArray additionalTroops = new JSONArray();
        additionalTroops.put(new JSONObject().put("owner", 1).put("troops", 1));
        additionalTroops.put(new JSONObject().put("owner", 2).put("troops", 2));
        additionalTroops.put(new JSONObject().put("owner", 3).put("troops", 3));
        result.put("additionalTroops", additionalTroops);
        return ok(result.toString());
    }

    public static Result commitTestTurn(Long id) {
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

    public static Result getTestMapPolling(Long id) {
        testCounterInGetTestMap++;
        if(testCounterInGetTestMap > 10) {
            return playerWinsMap();
        }
        if(testCounterInGetTestMap == 10) {
            return getTestMap(id);
        }
        return status(400, "map not ready");
    }
}
