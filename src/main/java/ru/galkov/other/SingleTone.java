package ru.galkov.other;

import java.util.ArrayList;

public class SingleTone {

	public static void main(String[] args) {

		SingleToneExample st = SingleToneExample.getInstance();		
		st.runThis();

				
		ArrayList<Cl> li = new ArrayList<Cl>();
		li.add(new Cl());
		li.add(new ClNext());
		li.add(new ClOne());
		li.add(new ClSrange());
		
		for (Cl var : li) {
			var.runner();
		}
		
	}
		
}



class SingleToneExample {
	private static SingleToneExample instance; 


	private SingleToneExample() {	}
	
	
	public static synchronized SingleToneExample getInstance() {
		if (instance == null) instance = new SingleToneExample();
		return instance;
	}
	
	public synchronized void runThis() {		
		if (instance != null) {
			System.out.println(instance.toString());	
			System.out.println(instance.hashCode());
			System.out.println("===========");
		}
	}
	
}



// набивка

class Cl {
	
	public void runner() {		System.out.println("Cl");	}		
}

class ClOne extends Cl {
	
	@Override
	public void runner() {		System.out.println("ClOne");	}
	
}

class ClSrange extends Cl {

	@Override
	public void runner() {		super.runner();	}
	
}

class ClNext extends Cl {
	
	@Override
	public void runner() {		System.out.println("ClNext");	}
	
}