package ru.galkov.other;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class passGenerator {
    public static void main(String[] args) throws Exception  {
        ByteArrayOutputStream password = getPassword();
        System.out.println(password.toString());
    }

    public static ByteArrayOutputStream getPassword() throws Exception {
        int passLength = 8;
        String pass = "";
        char[] str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890".toCharArray();
        Random rnd = new Random();
        boolean isDigit = false;
        boolean isUpper = false;
        boolean isLower = false;

        do {
        	pass = "";
            for (int i =0; i<passLength; i++) {
                char c = str[(int)(Math.random()*str.length)];
                if (Character.isDigit(c)) isDigit = true;
                if (Character.isUpperCase(c)) isUpper = true;
                if (Character.isLowerCase(c)) isLower = true;
                pass +=c;
            }
        }
        
        while (!(Pattern.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).+", pass)));
        
        

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
        try {
		        out.write(pass.getBytes());
		        byteArrayOutputStream.flush();
		        byteArrayOutputStream.close();
	    } 
	    catch (IOException e) {   e.printStackTrace();}
        return byteArrayOutputStream;
    }
}