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
		System.out.println("trade");

		giver = give;
		System.out.println("trade");
		reciever = recieve;
		System.out.println("trade");
		myAmount = number;
		System.out.println("trade");
		myType = fromString(type);
		System.out.println("trade");

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
		if(t.getGiver().equals(giver)){
			if(t.getReceiver().equals(reciever)){
				if(t.getAmount() == myAmount){
					if(t.getTradeType().equals(myType)){
						return true;
					}
				}
			}
		}
        return false;
	}   

	public TradeType fromString(String s){
		switch(s){
			case "INFANTRY": return TradeType.INFANTRY;
			case "AUTOMATIC" : return TradeType.AUTOMATIC;
			case "ROCKETS" : return TradeType.ROCKETS;
			case "TANKS": return TradeType.TANKS;
			case "IMPROVEDTANKS": return TradeType.IMPROVEDTANKS;
			case "PLANES": return TradeType.PLANES;
			case "SPIES": return TradeType.SPIES;
			case "INTERCEPTOR": return TradeType.INTERCEPTOR;
			case "NUKE": return TradeType.NUKE;
			case "TERRITORY": return TradeType.TERRITORY;
			case "FOOD": return TradeType.FOOD;
			case "TECHNOLOGY": return TradeType.TECHNOLOGY;
			default: return TradeType.INFANTRY;
		}
	}
}