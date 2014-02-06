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
    public static Result createTestGame() {
        JSONObject result = new JSONObject();
        result.put("gameID", "3938383");
        JSONArray names = new JSONArray();
        names.put(new JSONObject().put("name", "Kat").put("ready", "false"));
        names.put(new JSONObject().put("name", "Julian").put("ready", "false"));
        names.put(new JSONObject().put("name", "Harris").put("ready", "false"));
        result.put("players", names);
        return ok(result.toString());
    }

    public static Result startTestGame(Long id) {
        isHarrisReady = "true";
        return ok();
    }

    public static Result getTestGame(Long id) {
        JSONObject result = new JSONObject();
        result.put("gameID", "3938383");
        JSONArray names = new JSONArray();
        if(testCounterInGetTestGame < 10) {
            names.put(new JSONObject().put("name", "Kat").put("ready", "false"));
        } else {
            names.put(new JSONObject().put("name", "Kat").put("ready", "true"));
        }
        if(testCounterInGetTestGame < 5) {
            names.put(new JSONObject().put("name", "Julian").put("ready", "false"));
        } else {
            names.put(new JSONObject().put("name", "Julian").put("ready", "true"));
        }
        testCounterInGetTestGame++;
        names.put(new JSONObject().put("name", "Harris").put("ready", isHarrisReady));
        result.put("players", names);
        return ok(result.toString());
    }

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
