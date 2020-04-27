package ru.galkov.other;

package ru.galkov;

import ru.bitel.bgbilling.kernel.script.server.dev.GlobalScriptBase;
import ru.bitel.bgbilling.server.util.Setup;
import ru.bitel.common.sql.ConnectionSet;
import ru.bitel.bgbilling.modules.voice.accounting.mediation.*;
import ru.bitel.bgbilling.modules.voice.api.common.bean.*;
import ru.bitel.bgbilling.modules.voice.runtime.VoiceSessionRuntime;
import ru.bitel.common.ParameterMap;
import org.apache.log4j.Logger;
import java.util.regex.*;
import ru.bitel.common.*;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.util.*;
import java.io.*;

//@galkov
// парсер ИГНОРИРУЕТ транзитные вызовы поскольку пока нет правила присвоения их на абонента.  работает только 9999888
// в логе присутствуют не понятные номера. 999958 игнорируются. так как нет транзит ID

public class rawCDRParcer	extends GlobalScriptBase {

    private VoiceDevice device;
    private final Logger logger = Logger.getLogger( rawCDRParcer.class );
    private final String rawLogPath = "/opt/bgbilling/CDR/input/"; 
    private final String path = "/opt/bgbilling/CDR"; 
    private final String processLogPath = "/opt/bgbilling/CDR/tmp/"; 
    private final GregorianCalendar calendar = new GregorianCalendar();
    private File mainLog, rawLog;
// переписать на более общее условие - всё из каталога или по расширению и т.п.    и в один файл класть.
    private final String FILE_PREFIX = "Master.csv";    
    private int lineNo = 0;
    
    
	@Override
	public void execute( Setup setup, ConnectionSet connectionSet )		throws Exception 	{
	
   		// перепарсиваем из rawLog => mainLog reader =>writer      ==================================================
		BufferedReader reader = getReader();
	    BufferedWriter writer = getWriter();   
	    String line ="";

		int i=0;
		
	    while ((line != null) && (i<2000)) {
	    	try{ 
		        line = reader.readLine(); lineNo++;
//		        i++; //для дебага - меньше строк
				 String[] params  = getParams(line);
				 // ошибки парсинга исходного CDR. параметр 9 содержит дату вызова или error
   	    	   	 if (params[9].equals("error")) {	System.out.println("В файле не выявленная сигнатура!\n"+line);	}
        	   	 else {
       	   		 	writer.write(params[9] + "\t" + params[12] + "\t" + prepareNumber(params[1],params[0]) + "\t" + prepareNumber(params[1],params[0]) + "\t" + prepareNumber(params[2],params[0]) + "\t" + prepareNumber(params[2],params[0]) + "\t" + prepareTrunk(params[6]) + "\t" + prepareTrunk(params[5]) + "\t" + "0" + "\t" + params[13] + "\t" + "0" + "\n"); 
       	   		  }
        	   }
        	   catch (NullPointerException e) { logger.info("Файл " + rawLogPath + "/" + FILE_PREFIX + " конвертирован. "); }
	    }
	    // ==========================================================================================================
   		writer.close();
		reader.close();
	    
		// удаляем исходник.
        (new File(processLogPath, "raw")).delete();
  		// перекладываем полученный лог в нужную месячную папку.	
        mountCDRLog(mainLog);
	}
	
	
	
	
// ================================================ utilites ====================================================	

    private BufferedReader getReader() throws FileNotFoundException { 
    	// источник. перемещаем в др. каталог и переименовываем.
		(new File( rawLogPath,  FILE_PREFIX)).renameTo(new File(processLogPath, "raw"));    
	    return  new BufferedReader(new FileReader(processLogPath+ "raw"));
    };
    
    private BufferedWriter getWriter() throws IOException   { 

		// создаём файл вида DD_HH - день, час.
		String path1 = (calendar.get(Calendar.DAY_OF_MONTH)<10) ?  ("0" + Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))) : (Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
		String path2 = (calendar.get(Calendar.HOUR_OF_DAY)<10)  ? ("0" + Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))) : (Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
			
   		mainLog = new File( processLogPath, path1 + "_" + path2);
	    try		{ mainLog.createNewFile(); }  catch	(IOException e) { e.printStackTrace(); }
	    return new BufferedWriter(new FileWriter(mainLog));
    };


	private void mountCDRLog(File mainLog) {
			
		// +1 - сменить на LocalDateTime надоела нумерация с 0.
		String path1 = ((calendar.get(Calendar.MONTH)+1)<10) ? ("0" + Integer.toString(calendar.get(Calendar.MONTH)+1)) :(Integer.toString(calendar.get(Calendar.MONTH)+1));
 		String monthDirStr = path + "/" + calendar.get(Calendar.YEAR) + "/" + path1;
// 		System.out.println(monthDirStr);

        File monthDir  = new File( monthDirStr );
        if ( monthDir == null || !monthDir.exists() )
        {
        	boolean res = new File(monthDirStr).mkdirs();
        	if (!res)  { System.out.println("Путь к логам отсутствует и создать его структуру("+monthDirStr+") не удалось!"); }
      	}
      	else {	mainLog.renameTo(new File(monthDirStr, mainLog.getName())); }
	}
	

	private String[] getParams(String line1) {
	    	    	 // удаление не нужной инфы, мешающей разбиению на столбцы. возможно таких сигнатур
    	    	     // нужно будет больше, ибо они фиксят название каких-то линий тупо по сигнатурам, а это не надёжно.. но выбора нет(они нарушают стурктуру данных): 
    	    	     // "\{несколько цифр},200," =>",
    	    	     // "\{несколько цифр},300," =>",
    	       	     // "\{несколько цифр},200,r" =>",
    	       	     // "\{несколько цифр},20" => ",
    	       	     //"\{три цифры}," => ", 
    	    	     // сигнатуры выявлены не все.
    	    	     String line_ = line1.replaceAll("\\/\\d{1,},[2-3]{1}[0]{1,2}\",", "\",");
							line_ = line_.replaceAll("\\/\\d{1,},200,r\",", "\","); 
							line_ = line_.replaceAll("\\/\\d{3},\",", "\","); 
    	    	     
    	    	     line_ = line_.replaceAll("\"",""); // убрать все кавычки.
        	   		 String[] params =  line_.split(",");        	   		         	   		 
        	   		 params[9] = reparceDate(params[9]);        	   		 
        return params;	
	}

	
	
	private String reparceDate(String date){ // yyyy-mm-dd hh:mm:ss => dd-mm-yy hh:mm:ss

		String[] params =  date.split(" ");
		String res = params[0];
		String[] d =  res.split("-");
		
		// здесь мы непременно напарываемся на ошибку парсинга лога, если она есть, можно тут отправлять error для логирования пропущенных сигнатур
		 try { res = d[2] + "." + d[1] + "." + d[0] + " " + params[1]; }		catch ( ArrayIndexOutOfBoundsException e ) { res = "error"; }
// для теста
//		try { res = d[2] + "." + "12" + "." + d[0] + " " + params[1]; }		catch ( ArrayIndexOutOfBoundsException e ) { res = "error"; }
		
		return res;
		
	}

// это спорный момент, но так название транка делается более общим. их становится меньше (сгруппированы) и можно использовать в условиях.
// возможно нужно отрезать первые "SIP/"
	private String prepareTrunk (String trunk) {		return trunk.replaceAll("-[0-9,a-f]{1,}", "");	}


   private String prepareNumber( String number, String number2 )    {   
   		// это условия замены номера транзита из номера транзита в номер абонента. 
   		// сделано как я понял.тут можно сделать через elseif статическую переделку. или использовать общее правило преобразования.
		// индефикатор транзита - 4 первых "9" и 7 цифр в номере
		//					else if (number.equals("xxxxхххх")) { number = number2; } ....
		
		// удаляем всякий мусор из номера...
   		number = number.replaceAll("[+,-,a-z,A-Z]{1,}", "");	

   			
		if (number.length()==7) { 
			if ((number.substring(0,4)).equals("9999")) { 
				if ((number2.length() <1)) { 
					logger.info(number + " - " + number.substring(0,4) + " - " + number2 + "trunk ID в CDR не содержит номера замены. Строка исходного лога - " + lineNo); 
				} 
				else { number = number2;	}
			} 		
		}
   		return prepareNumber(number);
   }

   private String prepareNumber( String number )    {
   		// нормализатор всех номеров к одному формату.

	try {
        
   		// удаляем 810 как индикатор международного вызова.
        if ((number.substring(0,3)).equals("810"))
        {  	try { return number.substring(3); } 	catch (StringIndexOutOfBoundsException e) { return number; }       }        	

   		//заменяем  в номерах 8800 => 7800 для едингого вида.        	
        else if ((number.substring(0,4)).equals("8800"))
        {  	try { return "7" + number.substring(1); } 	catch (StringIndexOutOfBoundsException e) { return number; }       }

        // заменяем первую 8ку на 7ку номере.
        else if ( (number.substring(0,1)).equals("8") && (number.length()==11) ) 
        {  	try { return "7" + number.substring(1); } 	catch (StringIndexOutOfBoundsException e) { return number; }       }
        	
   		// дописываем 7ку к короткому номеру.
        else if ( number.length() == 10 )        { return "7" + number; }
	}
	catch (StringIndexOutOfBoundsException e) {		logger.info("Странный номер. Не может быть приведён к 11 значному формату. Записан в CDR как есть - " + number + ". Строка исходного лога - " + lineNo);	}
        	
        
        return number;
    }
	
}