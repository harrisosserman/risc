package models;

import java.util.*;

public class Allign extends MoveType {

	private boolean forming;
	private String myOwner;
	private String myAlly;

	public Allign(int movetype, boolean form, String owner, String ally){
		super(movetype);
		forming = form;
		myOwner = owner;
		myAlly = ally;
		
	}

	public int getPosition(){
		return myPosition;
	}

	public boolean forming(){
		return forming;
	}

	public String getOwner(){
		return myOwner;
	}

	public String getAlly(){
		return myAlly;
	}
}