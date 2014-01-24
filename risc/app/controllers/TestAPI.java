package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import controllers.routes;
import java.util.*;
import libraries.JSONLibrary.JSONObject;
import libraries.JSONLibrary.JSONArray;
import models.Game;

public class TestAPI extends Controller {
    public static Result createTestGame() {
        JSONObject result = new JSONObject();
        result.put("gameId", "3938383");
        JSONArray names = new JSONArray();
        names.put(new JSONObject().put("name", "Kat").put("ready", "false"));
        names.put(new JSONObject().put("name", "Julian").put("ready", "false"));
        names.put(new JSONObject().put("name", "Harris").put("ready", "false"));
        result.put("players", names);
        return ok(result.toString());
    }


}
