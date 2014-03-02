package models;

import java.util.*;

public class Place extends MoveType {

	private int myPosition;

	public Place(int movetype, String trooptype, int position){
		super(movetype, trooptype);
		myPosition = position;
	}

	public int getPosition(){
		return myPosition;
	}
}