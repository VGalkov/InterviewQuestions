package ru.galkov.other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class MadMemoryUtilisator {
	
	private static ArrayList <Disputer> list;
	private static long DISPUT_PERIOD = 1000;
	private Worker worker;

	
	
	MadMemoryUtilisator() {
		worker = new Worker();
		list = new ArrayList<Disputer>();
		list.add(new Disputer(true, 1, worker));
		list.add(new Disputer(true, 2, worker));
		list.add(new Disputer(true, 3, worker));				
		list.add(new Disputer(true, 4, worker));				
		list.add(new Disputer(true, 5, worker));				
	}
	
	
	
	public static void main(String[] args) {
		MadMemoryUtilisator test = new MadMemoryUtilisator();
		
		try {	
				test.startDisput();
				Thread.sleep(DISPUT_PERIOD);
				test.enouth();
				
		}
		catch (Exception e) {	e.printStackTrace();}	
		
	}
	
	private final void enouth() {
 		for (Disputer item : list )  { item.isWork(false);	}
 		for (Disputer item : list )  { 
 			try {	item.join();} 
 			catch (InterruptedException e) {	e.printStackTrace();		}		
 		}	
 		System.out.println("Ну всё..");
	}
	
	private final void startDisput() {
		for (Disputer item : list)  item.start();
	}	
}
		


final class  Disputer extends Thread{
	 
		private boolean flag = true;
		private Worker worker;
		private int num =0;
		TotalCounter tc = TotalCounter.getinstance();
		
		public Disputer(boolean fl,  int num2, Worker  worker3) {
			this.flag = fl;
			this.num = num2;
			this.worker = worker3;
		}
	 
		public void run()	{ 
			while(flag) {
				// System.out.println("Процесс "+num+" ковыряет "+worker.mp.toString()+", сквозной номер ковыряния - " +tc.getIandIncI());
				System.out.println("Процесс "+num+" ковыряет Map, сквозной номер ковыряния - " +tc.getIandIncI());
				worker.helloWorldFilter();
			}
		}
		
		public final int getNum() {
			return num;
		}
		
		public void isWork (boolean b) {
			flag = b;
		}
 }


 final class TotalCounter {

	private static TotalCounter instance;
	private int i=0;
	
	private TotalCounter() {	}
	
	public static synchronized TotalCounter getinstance() {
		if (instance == null) instance = new TotalCounter();
		return instance;
	}
	

	
	public synchronized int getIandIncI() {
		if (instance != null) {	return i++;		}
		// may be an NoTotalCounterInstanceException ?
		else return 0;
	}
}

 

final class Worker {
		private final int NUM = 10000000;
		private final char[] CHARS = { 'H','e','l','o'};
		private final int LENGTH = CHARS.length;  
		private static boolean flag = false;
		Random rnd = new Random();		
		Map<String, String> mp = new HashMap<String, String>();
		
		
		Worker () {
			 
				for (int i = 0; i<NUM; i++) {
					String str =	String.valueOf(CHARS[rnd.nextInt(LENGTH)]) + 
									String.valueOf(CHARS[rnd.nextInt(LENGTH)]) + 
									String.valueOf(CHARS[rnd.nextInt(LENGTH)]) + 
									String.valueOf(CHARS[rnd.nextInt(LENGTH)]) +
									String.valueOf(CHARS[rnd.nextInt(LENGTH)]);
					
					mp.put(str, String.valueOf(str.hashCode())); // обычно наоборот
					if (str.equals("Hello")) {flag=true;}
				}
				if (flag) {System.out.println("Hello есть");}
		 }
		
		final void output () {
			for (Entry<String, String> entry : mp.entrySet()) {	
				if (entry.getKey().equals("Hello"))   { System.out.println(entry.getKey()+": =>"+entry.getValue()); }	
			}
		}
		
		final void helloWorldFilter() {
			helloWorldFilter(mp);
		}
		 
		 public  void helloWorldFilter(Map<String, String> helloWorlds) { 
			if (helloWorlds == null) { return; }
		// ? = .map(line -> line.getKey()).collect(Collectors.joining()); ?
			try { helloWorlds.keySet().stream().filter(word -> word.equals("Hello")).map(helloWorlds::remove);} 		
			catch (RuntimeException e) { 
				System.out.println(String.format("Nuclear revenge just started by [%s]:", e)); }
			}
		 
		 
		 
	 }