package ru.galkov.other;

package ru.galkov;

import java.util.Date;
import java.util.List;
import ru.bitel.bgbilling.modules.voice.accounting.mediation.Mediator;
import ru.bitel.bgbilling.modules.voice.accounting.mediation.VoiceRecord;
import ru.bitel.bgbilling.modules.voice.accounting.mediation.VoiceRecordProcessor;
import ru.bitel.bgbilling.modules.voice.api.common.bean.VoiceDevice;
import ru.bitel.bgbilling.modules.voice.api.common.bean.VoiceDeviceType;
import ru.bitel.bgbilling.modules.voice.runtime.VoiceSessionRuntime;
import ru.bitel.bgbilling.server.util.Setup;
import ru.bitel.common.ParameterMap;
import org.apache.log4j.Logger;
import java.io.*;
//import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.bitel.common.TimeUtils;
import ru.bitel.common.Utils;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;


//@galkov читаем местные логи телефонии в биллинг

public class AsteriskMediatorCustom	implements Mediator {

	// https://forum.bitel.ru/viewtopic.php?f=58&t=13344
    private final Logger logger = Logger.getLogger( AsteriskMediatorCustom.class );
   	private final Pattern PATTERN_HOUR = Pattern.compile( "(\\d{2})_(\\d{2})" );
    private VoiceDevice device;
    private final String rawLogPath = "input/"; // сюда поступают логи с астериска.

    
	
	@Override
	public void getLogExists( Date month, int[] data )	{
		
	    String path = device.getLogPath();  
	    
//		System.out.println("getLogExists: Ищем логи в - " + path);	    
         File rootDir = new File( path );
        if ( !rootDir.exists() )        {            return;        }
 		
 		String monthDirStr = path + File.separator + new SimpleDateFormat( "yyyy/MM" ).format( month );
        File monthDir  = new File( monthDirStr );
//        System.out.println(monthDirStr);
        
        if ( monthDir == null || !monthDir.exists() )
        {
        	boolean res = new File(monthDirStr).mkdirs();
        	if (!res)  { System.out.println("Путь к логам отсутствует и создать его структуру("+monthDirStr+") не удалось!"); }
            return;
        }
        
        
        for ( File  logFile : monthDir.listFiles() )
        {
            Matcher matcher = PATTERN_HOUR.matcher( logFile.getName() );
            System.out.println("найден лог - "+logFile.getName());
            if ( matcher.find() )
            {
                String dd = matcher.group( 1 );
                String hh = matcher.group( 2 );
                
                //TODO parseByte
                byte day = (byte) Utils.parseInt( dd, -1 );
                byte hour = (byte) Utils.parseInt( hh, -1 );
                
                if ( day > 0 && hour >= 0 )
                {
                    data[day - 1] |=  ( 1 << hour );   
                }
                
            }
        }
		
	}

	@Override
	public void readHourDataLog( VoiceRecordProcessor processor, Date hour )		throws Exception	{
		
        InputStream is = null;
        BufferedReader reader = null;
        String path = device.getLogPath();
		System.out.println("readHourDataLog логи берём из - " + path);        
	    try
	    {
            String fullPath = path + TimeUtils.format( hour, "/yyyy/MM/dd_HH" );
    		
            File file = new File( fullPath );
       		System.out.println("смотрим в - " + fullPath);
    		
			is = new FileInputStream( file );
    		
    		if ( is == null )
    		{
    			System.out.println( "лог для " + hour +  " пустой" );
    			return;
    		}
    
    		reader = new BufferedReader( new InputStreamReader( is ), 128 * 1024 );

		    //final Pattern pattern = Pattern.compile( "\\t" );

			String line;
			while( (line = reader.readLine()) != null )
			{
				String[] params = line.split( "\t" );
				// странная проверка валидности.. 
				if( params.length >= 9 )
				{               
				     processLine( processor, params );
				}
				else
				{
					System.out.println( "Skip line: " + line );
				}
			}
		} // IOException e
		catch (FileNotFoundException e) { e.getMessage(); }
	    
		finally
		{
			if ( reader != null )		{  reader.close();			}
			if ( is != null )			{  is.close();		}
		}
		
	}


	@Override
	public Object init( Setup setup, int moduleId, VoiceDevice device, VoiceDeviceType deviceType, ParameterMap config )		throws Exception	{
        this.device = device;
		return null;
	}

	@Override
	public void errorOperAccountFound( List<VoiceSessionRuntime> recordSessions, VoiceRecord record )	{
	}

	@Override
	public void errorAbonAccountFound( List<VoiceSessionRuntime> recordSessions, VoiceRecord record )	{
	}
	
	
    
	protected void processLine( final VoiceRecordProcessor processor, final String[] params )		throws InterruptedException
	{
		final VoiceRecord record = processor.next();

		record.sessionStart = TimeUtils.parseDate( params[0], TimeUtils.DATE_FORMAT_PATTERN_DDMMYYYY_HHMMSS );
		record.duration = record.connectionDuration = Utils.parseInt( params[1], 0 );
		// переформатировать (данные в логах не в формате) или это будет делать проверялка логов?
		record.callingStationId = params[2];
		record.e164CallingStationId = params[3];
		record.calledStationId = params[4];
		record.e164CalledStationId = params[5];
		
		record.trunkIncoming = params[6];
		record.trunkOutgoing = params[7];
		record.category = Utils.parseInt( params[8], 0 );

		if( params.length > 9 )
		{
			record.connectionDuration = Utils.parseInt( params[9].trim(), 0 );
			try
			{
				record.callCost = new BigDecimal( params[10] );
				record.callOperCost = new BigDecimal( params[11] );
			}
			catch( Exception e )			{				
//				e.printStackTrace();			
//				System.out.println("это вроде не надо");
			}
		}
	}  

}