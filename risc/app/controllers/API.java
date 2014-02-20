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
import models.Turn;
import models.State;
import libraries.DBHelper;

public class API extends Controller {

    public static MongoConnection initDB() throws UnknownHostException{
        MongoConnection mongoConnection = new MongoConnection();
        return mongoConnection;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result createGame() throws UnknownHostException{
        RequestBody body = request().body();
        String playerName = body.asJson().get(DBHelper.NAME_KEY).toString();
        String playerNameWithoutQuotes = removeQuotes(playerName);

        Game game = new Game();

        boolean canStillJoin = game.canPlayersStillJoin();
        String gameID = game.getGameID();
        int playerID;

        if(canStillJoin){
            game.addWaitingPlayer(playerNameWithoutQuotes);
            playerID = game.getWaitingPlayerCount();
        }else{
            playerID = 0;
        }

        JSONObject result = new JSONObject();
        result.put(DBHelper.GAME_ID_KEY, gameID);
        result.put(DBHelper.PLAYER_ID_KEY, playerID);

        if (canStillJoin) {
            return ok(result.toString());
        }else{
            return badRequest(result.toString());
        }
    }

    public static Result getWaitingPlayers(String id) throws UnknownHostException{
        Game game = new Game();
        String json = game.getWaitingPlayersJson(id);
    	return ok(json);
    }


    @BodyParser.Of(BodyParser.Json.class)
    public static Result commitTurn(String id) throws UnknownHostException {

        RequestBody body = request().body();
        Turn turn = new Turn();
        //String gameID = turn.getGameID(body);
        int turn_number = turn.createTurn(body);
    /*    boolean json = turn.allTurnsCommitted();
        if(json){
            State state = new State(gameID);
            state.assembleState(turn_number);
            return ok("game state made");
        }*/
        return ok("Turn commited for turn :" + turn_number );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result startGame(String id) throws UnknownHostException{
        RequestBody body = request().body();
        int startingPlayerNumber = Integer.parseInt(body.asJson().get(DBHelper.PLAYER_NUMBER_KEY).toString());
        String startingPlayerName = body.asJson().get(DBHelper.NAME_KEY).toString();
        Game game = new Game();
        game.start(id, startingPlayerNumber, startingPlayerName);
        return ok();
    }

    public static Result getMap(String id) throws UnknownHostException{
        Game game = new Game();
        String mapJson = game.getMapJson(id);
        return ok(mapJson);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result exit(String id) throws UnknownHostException{
        RequestBody body = request().body();
        int exitingPlayerNumber = Integer.parseInt(body.asJson().get(DBHelper.PLAYER_NUMBER_KEY).toString());

        Game game = new Game();
        game.removePlayer(exitingPlayerNumber);
        return ok("Exiting for player:" + exitingPlayerNumber);
    }

    public static Result isMapReady(String id) throws UnknownHostException{
        Game game = new Game();
        if (game.areAllPlayersCommitted()) {
            String gameStateJson = game.getCurrentGameStateJson(id);
            return ok(gameStateJson);
        }else{
            return badRequest("all players havent committed yet");
        }
    }

    public static String removeQuotes(String stringWithQuotes){
        return stringWithQuotes.substring(1, stringWithQuotes.length() - 1);
    }
    
    public static Result reset(String id) throws UnknownHostException{
        DBHelper.reset(id);
        return ok("Reset DB for gameID:" + id);
    }
}
