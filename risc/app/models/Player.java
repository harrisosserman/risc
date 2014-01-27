package models;

import java.util.*;

public class Player {

	private String myName;

	public Player(String name){
		this.myName = name;
		System.out.println("created player with name:" + myName);
	}
}