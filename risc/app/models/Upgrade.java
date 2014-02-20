package models;

import java.util.*;

public class Upgrade extends MoveType {

	private int myPosition;
	private String myUpgradeType;

	public Upgrade(String movetype, String trooptype, String upgrade, int position){
		super(movetype, trooptype);
		myPosition = position;
		myUpgradeType = upgrade;
	}

	public String getUpgradeType(){
		return myUpgradeType;
	}

	public int getPosition(){
		return myPosition;
	}
}