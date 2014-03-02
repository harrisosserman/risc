package controllers;

import akka.*;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;
import akka.actor.Props;
import play.libs.Akka;

import com.mongodb.*;

import controllers.routes;

import java.util.*;

import libraries.JSONLibrary.JSONObject;
import libraries.JSONLibrary.JSONArray;
import libraries.MongoConnection;
import libraries.DBHelper;

import models.Game;
import models.Turn;
import models.State;
import models.UserManager;
import models.WaitingRoom;
import models.BattleTest;

import play.mvc.Result;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.BodyParser;

public class API extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result createWaitingRoom(){
        RequestBody body = request().body();
        String playerName = body.asJson().get(DBHelper.NAME_KEY).toString();
        String playerNameWithoutQuotes = removeQuotes(playerName);

        WaitingRoom wr = new WaitingRoom();
        wr.createNewWaitingRoom(playerNameWithoutQuotes);

        UserManager um = new UserManager();
        um.addGameToUser(wr.getGameID(), playerNameWithoutQuotes);

        JSONObject result = new JSONObject();
        result.put(DBHelper.GAME_ID_KEY, wr.getGameID());

        return ok(result.toString());
    }

     public static Result getWaitingRoomInfo(String gameID){
        WaitingRoom wr = WaitingRoom.getWaitingRoom(gameID);
        return ok(wr.toString());
    }

    public static Result getAllJoinableGames(){
        return ok(WaitingRoom.getJoinableWaitingRoomsJson());
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result commitTurn(String gameID) {

        RequestBody body = request().body();
        //BattleTest b = new BattleTest();
        Turn turn = new Turn();
        int turn_number = turn.createTurn(body);
        boolean json = turn.allTurnsCommitted();
        if(json){
            System.out.println("entered json loop");
            State state = new State(gameID);
            int k = state.loadPreviousState();
            return ok("returned true " + k);
        }
        return ok("Turn commited for turn :" + turn_number);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result addPlayer(String gameID){
        RequestBody body = request().body();
        String username = body.asJson().get(DBHelper.NAME_KEY).toString();
        String usernameWithoutQuotes = removeQuotes(username);

        WaitingRoom wr = WaitingRoom.getWaitingRoom(gameID);
        wr.addPlayer(usernameWithoutQuotes);

        UserManager um = new UserManager();
        um.addGameToUser(wr.getGameID(), usernameWithoutQuotes);

        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result markPlayerReadyAndStartGameIfNeeded(String gameID){
        RequestBody body = request().body();
        String username = body.asJson().get(DBHelper.NAME_KEY).toString();
        String usernameWithoutQuotes = removeQuotes(username);

        WaitingRoom wr = WaitingRoom.getWaitingRoom(gameID);
        wr.markPlayerAsReady(usernameWithoutQuotes);

        if (wr.shouldGameBegin()) {
            //Initalize the new game
            new Game(wr.getGameID(), wr.getUsernames());

            wr.markRoomAsNotJoinable();
        }

        return ok();
    }

    public static Result getMap(String gameID){
        Game game = new Game(gameID);
        if (game.areAllPlayersCommitedForMostRecentTurn()) {
            String mapJson = game.getCurrentGameStateJson();
            return ok(mapJson);
        }else{
            return badRequest("not all players have committed yet");
        }
    }

    public static Result endGame(String gameID){
        WaitingRoom wr = WaitingRoom.getWaitingRoom(gameID);
        wr.markRoomsGameAsEnded();
        return ok();
    }

    public static String removeQuotes(String stringWithQuotes){
        return stringWithQuotes.substring(1, stringWithQuotes.length() - 1);
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

        Akka.system().scheduler().scheduleOnce(
            Duration.create(10, TimeUnit.SECONDS),
            new Runnable() {
                public void run() {
                    System.out.println("----------HELLO----------");
                }
            },
            Akka.system().dispatcher()
        ); 


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
