package ru.galkov.other;

package ru.galkov;

import ru.bitel.bgbilling.kernel.script.server.dev.GlobalScriptBase;
import ru.bitel.bgbilling.server.util.Setup;
import ru.bitel.common.sql.ConnectionSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

//@galkov утилита загрузки номеров из списка в биллинг. индексы не совместимы с догрузкой. нужно устанавливать вручную по смыслу.
public class NumerationLoader	extends GlobalScriptBase	{


//	private final String REESTR = "/opt/bgbilling/distr/csv/reestr.csv";
	private final String REESTR = "/opt/bgbilling/task_3_material/reestr.csv";
	// iconv -t UTF-8 -f WINDOWS-1251 -o reestr.csv reestr.csv
	private int categoryId  = 4; 
	private int resID = 3000;
	private final String comment = "без 7";
	
	@Override
	public void execute( Setup setup, ConnectionSet connectionSet )		throws Exception	{
	
		try {
				FileReader fr = new FileReader(REESTR);
				BufferedReader reader = new BufferedReader(fr); String line = reader.readLine();// 1 строка - шапка нафиг.
				Connection con = connectionSet.getConnection();
				Statement stmt = null;
    		
                while (line != null) {

	                line = reader.readLine();
//    	            System.out.println(line);
  	              String[] str =  line.split(";");
 			    resID++;                
//				String phoneFrom = "7"+str[0] + str[1];
//	            String phoneTo   = "7"+str[0] + str[2];
				String phoneFrom = str[0] + str[1];
	            String phoneTo   = str[0] + str[2];
	            String dateFrom = "2019-11-27";
                if (str[4].equals("\"ООО \"\"Первый Телеком\"\"\"")) { categoryId = 8;} else { categoryId = 9; } 
                
				 String sql = "insert into inv_phone_resource_5 set 	id ='"+resID+"', phoneFrom='"+phoneFrom+"', phoneTo='"+phoneTo+"', dateFrom='"+dateFrom+"', comment='"+comment+"', 		categoryId='"+		categoryId+"'";                			 
                System.out.println(sql);
				try{
	                stmt = con.createStatement();
            	 	 stmt.executeUpdate(sql);	
            	 }
            	 catch (SQLException e) {   e.printStackTrace();    }
            	 finally {
   	 			  		try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            	 }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
        }
		
	}

}