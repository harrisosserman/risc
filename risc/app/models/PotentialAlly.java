package models;

import java.util.*;

public class PotentialAlly {

	private Player proposer;
	private Player accepter;

	public PotentialAlly(Player p, Player k){
		proposer = p;
		accepter = k;
	}

	public Player getProposer(){
		return proposer;
	}

	public Player getAccepter(){
		return accepter;
	}
}