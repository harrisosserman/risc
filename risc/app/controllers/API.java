package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import controllers.routes;
import java.util.*;
import libraries.JSONLibrary.JSONObject;
import libraries.JSONLibrary.JSONArray;
import models.Game;
import play.mvc.Http.RequestBody;
import play.mvc.BodyParser;
import libraries.MongoConnection;
import com.mongodb.*;
import java.net.UnknownHostException;

public class API extends Controller {

    private static final String NAME = "name";
    private static final String GAME_ID = "gameID";
    private static final String PLAYER_ID = "playerId";
    private static final String PLAYER_NUMBER = "playerNumber";

    public static MongoConnection initDB() throws UnknownHostException{
        MongoConnection mongoConnection = new MongoConnection();
        return mongoConnection;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result createGame() throws UnknownHostException{
        RequestBody body = request().body();
        String playerName = body.asJson().get(NAME).toString();

        Game game = new Game();
        if (game.getWaitingPlayerCount() >= 5) {
            return badRequest("The maximum number of players are already playing. Please try again later");
        }
        game.addWaitingPlayer(playerName);

        JSONObject result = new JSONObject();
        result.put(GAME_ID, game.getGameID());
        result.put(PLAYER_ID, game.getWaitingPlayerCount());
        return ok(result.toString());
    }

    public static Result getWaitingPlayers(String id) throws UnknownHostException{
        Game game = new Game();
        String json = game.getWaitingPlayersJson(id);
    	return ok(json);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result startGame(String id) throws UnknownHostException{
        RequestBody body = request().body();
        int startingPlayerNumber = Integer.parseInt(body.asJson().get(PLAYER_NUMBER).toString());
        String startingPlayerName = body.asJson().get(NAME).toString();

        Game game = new Game();
        game.start(id, startingPlayerNumber, startingPlayerName);
        return ok();
    }

    public static Result getMap(String id) throws UnknownHostException{
        Game game = new Game();
        String mapJson = game.getMapJson(id);
        return ok(mapJson);
    }

    public static Result commitTurn(String id) {
        return ok("Turn commited for game:" + id);
    }
}
