package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import controllers.routes;
import java.util.*;
import libraries.JSONLibrary.JSONObject;

public class API extends Controller {
    public static Result createGame() {
        //fill in with game creation logic
        JSONObject result = new JSONObject();
        result.put("gameId", "3938383");
        return ok(result.toString());
    }
}
