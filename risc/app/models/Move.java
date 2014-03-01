package models;

import java.util.*;

public class Move extends MoveType {

	private int myStart;
	private int myEnd;

	public Move(int movetype, String trooptype, int start, int end){
		super(movetype, trooptype);
		myStart = start;
		myEnd = end;
	}

	public int getStart(){
		return myStart;
	}

	public int getEnd(){
		return myEnd;
	}
}