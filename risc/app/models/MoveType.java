package models;

import java.util.*;

public abstract class MoveType {

	private String myMoveType;
	private String myTroopType;

	public MoveType(String movetype, String trooptype){
		myMoveType = movetype;
		myTroopType = trooptype;
	}

	public String getMoveType(){
		return myMoveType;
	}

	public String getTroopType(){
		return myTroopType;
	}
}