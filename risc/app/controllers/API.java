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
    
    public static Result getWaitingPlayers(String id) throws UnknownHostException{
        Game game = new Game();
        String json = game.getWaitingPlayersJson(id);
    	return ok(json);
    }

    public static Result startGame(String id) {
        return ok("will start game:" + id);
    }

    public static Result getMap(String id) {
        return ok("will return map for game:" + id);
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result commitTurn(String id) throws UnknownHostException {
        RequestBody body = request().body();
        Turn turn = new Turn();

        int json = turn.createTurn(body);
        return ok("Turn commited for game:" + json);
    }

}
