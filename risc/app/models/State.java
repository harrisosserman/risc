package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import models.Territory;
import models.Troop;
import models.Attacker;

public class State{
 
	private int	turn; 
	private String myGameID;
	private Territory[] myTerritories;
	private ArrayList<Attacker> attackers;


public State(){

}	


}