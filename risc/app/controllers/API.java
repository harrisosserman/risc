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
import models.UserManager;
import models.WaitingRoom;

public class API extends Controller {

    public static MongoConnection initDB() throws UnknownHostException{
        MongoConnection mongoConnection = new MongoConnection();
        return mongoConnection;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result createWaitingRoom(){
        RequestBody body = request().body();
        String playerName = body.asJson().get(DBHelper.NAME_KEY).toString();
        String playerNameWithoutQuotes = removeQuotes(playerName);

        WaitingRoom wr = new WaitingRoom();
        wr.createNewWaitingRoom(playerNameWithoutQuotes);

        Game game = new Game();

        JSONObject result = new JSONObject();
        result.put(DBHelper.GAME_ID_KEY, wr.getGameID());

        return ok(result.toString());
    }

     public static Result getWaitingRoomInfo(String id){
        WaitingRoom wr = WaitingRoom.getWaitingRoom(id);
        return ok(wr.toString());
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

    //-------- Player API Methods ----------
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createPlayer() {
        RequestBody body = request().body();
        String username = body.asJson().get(UserManager.NAME_KEY).toString();
        String usernameWithoutQuotes = removeQuotes(username);
        String password = body.asJson().get(UserManager.PASSWORD_KEY).toString();
        String passwordWithoutQuotes = removeQuotes(password);

        UserManager um = new UserManager();
        boolean wasSuccessful = um.createUser(usernameWithoutQuotes, passwordWithoutQuotes);
        if (wasSuccessful) {
            return ok("Successfully created user: " + usernameWithoutQuotes);
        }else{
            return badRequest("Username already taken");
        }
    }

    public static Result getPublicUserInfo(String username){
        UserManager um = new UserManager();
        String playerJson = um.getPublicPlayerInfoJson(username);
        if (playerJson != null) {
            return ok(playerJson);
        }else{
            return badRequest("User: " + username + " does not exist");
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result logUserIn(String username){
        RequestBody body = request().body();
        String jsonUsername = body.asJson().get(UserManager.NAME_KEY).toString();
        String usernameWithoutQuotes = removeQuotes(jsonUsername);
        String password = body.asJson().get(UserManager.PASSWORD_KEY).toString();
        String passwordWithoutQuotes = removeQuotes(password);

        UserManager um = new UserManager();
        if (um.doesUsernameMatchPassword(usernameWithoutQuotes, passwordWithoutQuotes)) {
            String playerJson = um.getPublicPlayerInfoJson(username);
            return ok(playerJson.toString());
        }else{
            return badRequest("The username/password combination was invalid.");
        }
    }
}
