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
        return ok();
    }
}
