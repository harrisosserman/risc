package models;

import java.util.*;
import models.TroopType;
import models.Player;

public class Trade extends MoveType{


	private String giver;
	private String reciever;
	private int myAmount;
	private TradeType myType;

	public Trade(int movetype, String give, String recieve, int number, String type){
		super(movetype);
		giver = give;
		reciever = recieve;
		myAmount = number;
	//	myType = fromString(type);

	}

	public String getReceiver(){
		return reciever;
	}

    public String getGiver(){
		return giver;
	}

	public TradeType getTradeType(){
		return myType;
	}

	public int getAmount(){
		return myAmount;
	}

	public boolean equivalentTo(Trade t){
		if(t.getGiver().equals(reciever)){
			if(t.getReceiver().equals(giver)){
			//	if(t.getAmount() == myAmount)){
					if(t.getTradeType().equals(myType)){
						return true;
				//	}
				}
			}
		}
        return false;
	}   
}