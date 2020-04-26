package ru.galkov.other;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Serial {
	
		public static void main(String[] args) {
			
			Somth s1 = new Somth();
			s1.c = 103;
			
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);			
				objectOutputStream.writeObject(s1);
				objectOutputStream.close();
				
				ObjectInputStream objectInputStream2 = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
				Somth s3 = (Somth) objectInputStream2.readObject();
				
				System.out.println(s3.c);
				
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
		}

	}



		class Somth implements Serializable {

			private static final long serialVersionUID = 1231241L;
				public int c = 10;		
		}