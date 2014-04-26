package models;

import java.util.*;

public abstract class MoveType {

	private int myMoveType;
	private String myTroopType;

	public MoveType(int movetype, String trooptype){
		myMoveType = movetype;
		myTroopType = trooptype;
	}

	public MoveType(int movetype){
		myMoveType = movetype;
	}

	public int getMoveType(){
		return myMoveType;
	}

	public String getTroopType(){
		return myTroopType;
	}
}