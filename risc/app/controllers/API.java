package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import controllers.routes;
import java.util.*;
// import org.codehaus.jackson.JsonNode;
import play.libs.Json;
// import org.codehaus.jackson.node.ObjectNode;
import models.Game;

public class API extends Controller {
    public static Result createGame() {
        //fill in with game creation logic
        // ObjectNode result = Json.newObject();
        // result.put("gameId", "3938383");

    	Game game = new Game();
    	game.start();

        return ok("GAME IS CREATED");
    }
}
