package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import controllers.routes;
import java.util.*;
import libraries.JSONLibrary.JSONObject;
import models.Game;

public class API extends Controller {
    public static Result createGame() {
        //fill in with game creation logic

        Game game = new Game();
        game.start();
        JSONObject result = new JSONObject();
        result.put("gameId", "3938383");
        return ok(result.toString());
    }

    public static Result getWaitingPlayers(String id) {
    	return ok("will return a JSON of waiting players for game_id:" + id);
    }
}
