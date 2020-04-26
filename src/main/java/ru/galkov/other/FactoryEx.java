package ru.galkov.other;

import java.util.ArrayList;

public class FactoryEx {
	public static void main(String[] args) {
		Factory factory = new Factory();
		
		
		
		ArrayList<Swimming> ls = new ArrayList<Swimming>(); 
		ls.add(factory.create("cf"));
		ls.add(factory.create("cf"));
		ls.add(factory.create("rf"));
		ls.add(factory.create("rf"));
		ls.add(factory.create("rf"));
		
		
		for(Swimming item : ls) {
			item.swim();
		}
		
	}

}


// фабрика классов.
	class Factory  {
		public Swimming create(String menuItem) {
			if (menuItem.equals("cf")) return new CryFish();
			else if (menuItem.equals("rf")) return new RealFish();
			else throw new RuntimeException("òèï îáúåêòà íå ïîääåðæèàåòñÿ!");
		}
	}
	

// "объединялка"	
	interface Swimming {

		void swim();
	}
	
	

	
// варианты
	
	class CryFish implements Swimming  {
		@Override
		public void swim () {
			System.out.println(this.getClass().toString() + " is swimming");
		}
	}
	
	
	
	class RealFish implements Swimming  {
		
		@Override
		public void swim () {
			System.out.println(this.getClass().toString() + "is swimming");
		}
	}
	
	

