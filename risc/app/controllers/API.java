package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import controllers.routes;

public class API extends Controller {
    public static Result createGame() {
        //fill in with game creation logic
        return ok("GAME CREATED");
        // System.out.println("GAME CREATED");
    }
}
