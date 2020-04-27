package ru.galkov.other;



import ru.bitel.bgbilling.kernel.script.server.dev.GlobalScriptBase;
import ru.bitel.bgbilling.server.util.Setup;
import ru.bitel.common.sql.ConnectionSet;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

/*
@galkov s0506777@yandex.ru

вся структура в одном файле... логика/структура процедурная, а не ооп, чтобы ничего дополнительно писанного не вызывало.
*/

public class ContractsCreator	extends GlobalScriptBase {

	private final String PRODUCT_FILE_PATH = "/opt/bgbilling/task_2_material/data/abonents.csv"; /* путь, где лежит файл исходных данных
		важно - в первой строке этого файла, должен быть заголовок или пропуск. - при анализе данных она игнорируется в любом случае!
		структура данных файла - поля через разделитель - ;  - поля должны быть без кавычек. 
		
	Номер договора					0	
	Название Компании				1	
	Название продукта (услуги)		2	
	Номер							3	
	Количество						4	
	Цена							5	
	Объем (минут)					6	
	Дата доставки					7	
	Состояние договора				8 			*/
	
	private String[][] products;	// не трогать!
	private Connection con;			// не трогать!
	private final Logger logger = Logger.getLogger( contractsCreator.class );									// не трогать!
	private final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // формат дат в исходном файле
	private final File file = new File(PRODUCT_FILE_PATH);	// не трогать!
	private final LocalDate DEFAULT_START_DATE = LocalDate.of(2010,1,1); // самая дальняя дата. устанавливается в случае отсутствия любых.
	private final char STAMP = ':'; // индификатор добавления скриптом услуги в модуль. после миграции можно подчистить в названиях.
	private final String MARK = "(Добавлено gav конвертером)"; // индификатор добавления. после миграции можно подчистить в коментариях.

	
	private final static int idForStatus = 0; //0- Active не надо править.
	private static final int MODE = 0; // 0 - кредит,	1 - дебет. допуск ухода в минус с сохранением услу активными(0).
	private static final int FC = 1; // 0 - физ. лицо, 1 - юр. лицо
	
	private static final short RUN_TYPE = 0; /* формат запуска скрипта 
		0 - очистка + добавление, полный цикл.
		
		1 - только удаление.(сервисы не удаляются)
		2 - только добавление. можно запускать, только если ранее скрипт не запускался или прогонялся этап очистки + добавлены сервисы(п.4).
		3 - удаление + добавление сервисов в настройку модулей.
		4 - только добавление сервисов(перезапись).
		5 - добавление сервисов + добавление договоров (первый запуск или после очистки).
		
		6 - удаление + добавление договоров - без пересоздания сервисов.

	это важно перед запуском!!!   
			нельзя просто вслепую накатить данные на работающий биллинг это перезатрёт имеющиеся данные!!!
			
			перед добавлением нужно создать добавляемые сущности с уникальными ID и не перезаписать имеющиеся уже + 
			использовать эти id при создании договоров.
			поэтому сначала создаём сущности, а потом, когда будем делать договора будем искать id в созданных по значению поля.
			
			
	скрипт можно запускать многократно. он полностью удаляет все ранее добавленные им данные. 			
	
	перед запуском нужно делать резервные копии таблиц, к котороым обращается метод  > clearEnviroment < включая закоментированные.
	service, 
	contract, 
	contract_module, 
	npay_service_object_					+nPayModuleID, 
	voice_account_							+VoiceModuleID
	inv_phone_resource_subscription_		+VoiceModuleID
	voice_account_base_						+VoiceModuleID
	inv_phone_resource_						+VoiceModuleID
	contract_parameter_type_1
	
	в случае авариных запусков - просто распаковываем резервные копии на ходу на рабочий сервер с полной перезаписью таблиц из файлов дампов списком, с 
	СОБЛЮЕНИЕМ ТОЙ ЖЕ ПОСЛЕДОВАТЕЛЬНОСТИ РАСПАКОВКИ так как присутствуют взаимоувязанные ключи. 
	подразумевается, что в модуле Voice ресурсы хранятся в формате 7xxxyyyyyyy  к этому виду приводятся данные из products для сопоставления и добавления	 
	
	это списки абонплат и их id, на которые будет начислять модуль Voice и nPay 
	это таблица услуг, которые будут сопоставлены с услугами в договоре. если сопоставление не пройдёт хотя бы по 1 услуге у любого абонента 
	скрипт выкинет ошибку.	нужно будет читать лог у кого и исправлять исходные данные (опечатки, пробелы и т.п.)
	требуется 100% совпадение по строке.
	
	*/
	private static final String SHTUKA = "10000";
	private static final String SECUNDA = "20000";
	// voice {Наименование услуги, индекс в боевом биллинге, тип(штуки, секунды)}
	private static final String[][] VOICE_PAYS = {
																													{"Местные вызовы","26",SECUNDA},
																														
																													{"800","25",SECUNDA},
																														
																													{"Безлимитная местная связь","24",SECUNDA},
																														
																													{"Тарификация исходящих вызовов","23",SECUNDA},
																														
																													{"Междугородний вызов","22",SECUNDA},
																														
																													{"Международный вызов","21",SECUNDA},
																														
																													{"Внутирзоновая связь","20",SECUNDA},
																														
																													{"Справочные и экстренные службы","19",SHTUKA},
																														
																													{"Запись и хранение разговоров. Превышение.","18",SECUNDA},
																														
																													{"Запись и хранение разговоров","17",SECUNDA},
																														
																													{"Тарификация исходящих","16",SECUNDA},
																														
																													{"Тарификация входящих","15",SECUNDA},
																														
																													{"Безлимит МСК","14",SECUNDA},
																														
																													{"Переадресация и пропуск трафика", "13",SECUNDA}, 
	};
	// npay
	private static final String[][] NPAY_PAYS = {
				
					{"Пропуск голосовой информации на сети операторов зоновой, МГ и МН связи", "7", SECUNDA},
						
					{"Абонентская плата за детализацию трафика", "29", SECUNDA},
						
					{"Телефонный номер в коде 499", "", SHTUKA}, 
						
//					{"Телефонный номер в коде 499", "30", SHTUKA}, 
						
					{"Абонентская плата за телефонный номер с линией", "2", SHTUKA}, //2,?						


					{"Телефонный номер в коде 495", "27", SHTUKA},

					{"Абонентская плата за мониторинг голосовых вызовов", "10", SHTUKA},
						
					{"Абонентская плата за аренду оборудования", "9", SHTUKA},
						
					{"Предоплаченный объём вызовов", "8", SECUNDA},
						
						
					{"Пропуск АОН", "28", SHTUKA},
						
					{"Абонентская плата за телефонный номер 8800", "6", SHTUKA},
						
					{"Абонентская плата за МММТ", "5", SHTUKA},
						
					{"Абонентская плата за поток E1", "4", SHTUKA},
						
					{"Абонентская плата за линию на АТС", "3", SHTUKA},						
						
					{"Абонентская плата за дополнительную телефонную линию", "1", SHTUKA}
	};	
	
	
	private static final int paramCompanyNameID = 1;				/* выявить или создать ВРУЧНУЮ. это ID параметра будет другим на реальном сервере.
	взять значение можно в Справочники -> Другие -> Договоры -параметры - В этом поле лежит название организации.	*/
	
	private static final int paramContractNumberID = 2;	/* выявить или создать ВРУЧНУЮ. это ID параметра будет другим на реальном сервере
	взять значение можно в Справочники -> Другие -> Договоры -параметры - В этом поле лежит наименование договора */
	
	private static final int paramContractDebug = 3;	/* надо создать вручную и примсвоить значение. это важный параметр, по которому можно идентифицировать 
	добавленные договора и оперировать ими в отличии от имевшихся до. после завершения миграции этот параметр можно будет удалить */

	private static final int NPAY_MODULE_ID = 2;	/* КОД модуля nPay(абонплаты). выявить ВРУЧНУЮ, посмотреть можно в - модули -> редактор модулей и услуг -> код,
	это ID параметра будет другим на реальном сервере.  */
	
	private static final int VOICE_MODULE_ID = 5;	/*КОД модуля Voice. выявить ВРУЧНУЮ, посмотреть можно в - модули -> редактор модулей и услуг -> код,
	это ID параметра будет другим на реальном сервере	*/


	private final static int VOICE_DEVICE_ID = 1; /* это ID устройства из voice_device_(VoiceModuleID) оно заводится вручную в - 
	модуле voice -> устройства, вкладка устройства. */

	private final static int VOICE_ACCOUNT_TYPE_ID=1; /* это значение надо сгенерировать руками создав этот аккаунт в - 
	модуль Voice -> Типы аккаунтов -> клиентские аккаунты и ID первого столбца прописать сюда */
	
	private static final int CATEGORY_ID = 8; 	/* это ID категории(завести вручную, её ID записать сюда), в которую добавятся все выявленные номера, 
	не поддающиеся общему анализу или отсутствуют в ресурсах  но которые нужно повесить на абонента. 
	в этой категории не должно быть никаких других номеров, зханосимых вручную или другими скриптами!!! 
	этот скрипт занесёт все ненайденные в ресурсах номера и привяжет их.
 */
	
	
	
	

// ***************************** ВЫШЕ НАХОДЯТСЯ ПЕРЕМЕННЫЕ, КОТОРЫЕ НУЖНО И МОЖНО МЕНЯТЬ ПО ОБСТОЯТЕЛЬСТВАМ *********************************
// 	но коментарии нихе тоже имеет смысл для этого прочесть. править ниже этой линии ничего нельзя.
// ===========================================================================================================================================
	
	

	@Override
	public void execute( Setup setup, ConnectionSet connectionSet )		throws Exception	{
		// ENTRY POINT
		logger.debug("read data/ + проверка на null. двумерный массив данных - строки - заготовка договора столбец - данные договора.");
		setProducts();
		setCon(connectionSet.getConnection()); 
		
		switch (RUN_TYPE) {
			case 1: clearEnviroment(); break;
			case 2: addContracts(); break;
			case 3: clearEnviroment();	prepareEnviroment(); break;
			case 4: prepareEnviroment(); break;
			case 5: prepareEnviroment(); addContracts(); break;
			case 6: clearEnviroment(); addContracts(); break;
			
			default:{
				clearEnviroment();
				prepareEnviroment();
				addContracts(); 		
			}
			
		}		
	}



//=============================================================
// I
	private void prepareEnviroment() {
		clearServices();
		addNPayServices();
		addVoiceServices();
	}


	private void addContracts() {
		addContractRecords();
		addContractParametres();
		addContractModules();
		addContractServices();
		addContractPhonePoints(); 		
	}



	 private void clearEnviroment() {
	 /*	 ALTER TABLE users AUTO_INCREMENT =  SELECT MAX(id) FROM ...	 */
	 
		logger.debug("Удаление всех данных прежних попыток.");
		// удаление сервисов
		// это делается в clearServices так как не во всех алгоритмах запуска нужно трогать названия услуг.
//		dbUpdate("delete from service where (lm = '2010-01-01 00:00:00') and (isusing=1) and (parentId=0) and (comment='"+MARK+"')");		
		// удаление договоров
		dbUpdate("delete from contract where id in (select cid from contract_parameter_type_1 where (pid='"+paramContractDebug+"') and (val LIKE \"%"+MARK+"%\"))");		
		// удаление всех модулей с созданых договоров.
		dbUpdate("delete from contract_module where cid in (select cid from contract_parameter_type_1 where (pid='"+paramContractDebug+"') and (val LIKE \"%"+MARK+"%\"))");
		//удаление привязок модуля абонплат
		dbUpdate("delete from npay_service_object_"+NPAY_MODULE_ID+" where comment='"+MARK+"'");
		// удаление аккаунтов телефонии voice
		dbUpdate("delete from `voice_account_"+VOICE_MODULE_ID+"` where comment='"+MARK+"'");
 		// удаление привязок к ресурсам
		dbUpdate("delete from `inv_phone_resource_subscription_"+VOICE_MODULE_ID+"` where subscriberTitle IN (select c.title from contract_parameter_type_1 cp left join contract c on cp.cid=c.id where (cp.pid='"+paramContractDebug+"') and (cp.val LIKE \"%"+MARK+"%\"))");
		// удаление привязок номеров на договора
		dbUpdate("delete from `voice_account_base_"+VOICE_MODULE_ID+"` where contractId IN (select cid from contract_parameter_type_1 where (pid='"+paramContractDebug+"') and (val LIKE \"%"+MARK+"%\"))");
		//  удаление нестандартных ресурсов номерации.
		dbUpdate("delete from `inv_phone_resource_"+VOICE_MODULE_ID+"` where (`categoryId`	='"+CATEGORY_ID+"') and (`comment` ='"+MARK+"')");
		
		// этот должен быть последним.
		dbUpdate("delete from contract_parameter_type_1 where (val LIKE \"%"+MARK+"%\") or (pid IN ("+paramContractDebug+","+paramContractNumberID+","+paramCompanyNameID+"))");
	 }
	 
	  private void clearServices() {
		// удаление всех записей о активированных сервисах.
		dbUpdate("delete from service where (isusing=1) and (parentId=0) and (comment='"+MARK+"')");		
	  }
// ====================================================================




//=====================================================================
// II

	private void addContractModules() {
		logger.debug("тут добавляем на договор 2 модуля - модуль voice b модуль абонплат. для того, чтобы это отработало верно на боевом сервере нужно установить в скрипте ID модулей!!!");
 		Statement st = null;	
		try { 
			st = con.createStatement();
			String sql = "select id from contract c left join contract_parameter_type_1 cpt1 on cpt1.cid=c.id where pid='"+paramContractDebug+"'";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			while (res.next()) {		
				logger.debug("навешиваем модуль Voice");
				dbUpdate("insert into contract_module set `cid`='"+res.getInt("id")+"', mid='"+VOICE_MODULE_ID+"'");
				logger.debug("навешиваем модуль nPay");
				dbUpdate("insert into contract_module set `cid`='"+res.getInt("id")+"', mid='"+NPAY_MODULE_ID+"'");
			} 
		}
		catch (SQLException e)  { e.printStackTrace(); }	
    	finally { if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); } 	 } 
	
	}

 	private  void addContractRecords() {
		/* 
	Номер договора				0	
	Название Компании			1	title
	Название продукта (услуги)	2	
	Номер						3	-
	Количество					4	-
	Цена						5	-
	Объем (минут)				6	-
	[7]Дата доставки				7	date1
	[8]Состояние договора			8  = 6
	[9]subscriberID
*/

	 logger.debug("список неповторяющихся номеров договоров");
	  List<String> rawServisesList = new ArrayList<>();
	  for(int i=1; i< products.length; i++) {	 	rawServisesList.add(products[i][0].trim());	 }
	  Set<String> unicContractNames = new LinkedHashSet<>(rawServisesList); 
	  
	 for (String contractName : unicContractNames) {	 	 
		dbUpdate("insert into contract set  `status_date` = now(), `status`="+idForStatus+", del=0, `comment`='"+ getContractTitle(contractName)+MARK+"' ,`fc`="+FC+", `mode`="+MODE+", `date1`='"+getOldestDate(contractName)+"',	closesumma=0,`title`='"+contractName+"',`pswd` = '"+getPassword()+"'");
		}
	}




// =========================================

	private String getContractTitle(String contractName) {
		for (int i=1; i< products.length; i++) {
			if (contractName.equals(products[i][0].trim())) { return clearString(products[i][1]);		}
		}
		return "";
	}
	
	
	private String clearString(String str1) {
		str1 = str1.trim();
		if (str1.charAt(0)=='\"') str1 = str1.substring(1, str1.length()-1);
		if (str1.charAt(str1.length()-1)=='\"') str1 = str1.substring(0, str1.length()-1);
		return str1.trim().replaceAll("\"\"", "\"").replaceAll("\'", "");	
	}
	

	private LocalDate getOldestDate(String contractName) {
	  LocalDate res = LocalDate.now();
	  for (int i=1; i<products.length; i++) {
	  	try {
			  	LocalDate date =  getDate(products[i][7]);
	  			if (contractName.equals(products[i][0].trim())&& (res.compareTo(date)>0)) { 	  				res=date;		  		}
	  	}
	  	catch (DateTimeParseException e) {  }	
	  }
	  
	 logger.debug("если дата из услуг договора не установлена, берётся дата с потолка, за пределами любого возможного договора.");
	 return res.compareTo(LocalDate.now())==0 ? DEFAULT_START_DATE : res;
	}


	private int getPassword() { return new Random().nextInt((999999999 - 100000000) + 1) + 999999999;		}
	
	// ===============================

 	private  void addContractParametres() {
 		addTechParam();
 		addContractNumberParam();
 		addContractCompanyParam();
	}

 	private  void addContractServices() {

 		Statement st = null;	
		try { 
			st = con.createStatement();
			String sql = "select id,title from contract c left join contract_parameter_type_1 cpt1 on cpt1.cid=c.id where pid='"+paramContractDebug+"'";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			while (res.next()) { appendNPaysONContract(res.getInt("id"), getContractServices(res.getString("title")));		} 
		}
		catch (SQLException e)  { e.printStackTrace(); }	
    	finally { if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); } 	 } 				
	}
	
	
	private List<String[]> getContractServices(String contractTitle1) {
		List<String[]> contractNPays = new ArrayList<String[]>();
		logger.info("из products вытаскиваем записи о них для данного абонета.");
		for (int i=1; i<products.length; i++) {		if (contractTitle1.equals(products[i][0])) { contractNPays.add(products[i]); }			}
		return contractNPays;
	}
	
	private void appendNPaysONContract(int contractID1, List<String[]>  contractNPays2) {
		for (String[] record : contractNPays2) {
		// возможно, что emid = 0 или nPayModuleID 
			 dbUpdate("insert into npay_service_object_"+NPAY_MODULE_ID+" set `cid`='"+contractID1+"', `col`='"+record[4]+"', sid='"+getServiceIDByName(record[2])+"',date1='"+getDate(record[7])+"',emid='', comment='"+MARK+"'");
		}
	}
	 	
	private int getServiceIDByName(String serviceName) {
 		Statement st = null;	
		try { 
			st = con.createStatement();
			String sql = "select id, count(*) as count from service where `title` = '"+serviceName+"'";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			if (	(res.next())&&(res.getInt("count")>0)) { return res.getInt("id"); }
			else { throw new IndexOutOfBoundsException("название услуги" + serviceName+"не нашлось в списке услуг модуля!");		 }
		}
		catch (SQLException e)  { e.printStackTrace(); }	
    	finally { if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); } 	 } 		
		return 0;
	}
	 	
	private LocalDate getDate(String dt1)  	 {
	  	try {  	return LocalDate.parse(dt1.trim(), F);  	}
	  	catch (DateTimeParseException e) { logger.debug(dt1 + "не резолвится в LocalDate"); }	
		return LocalDate.now();
	}
	
 private  void addContractPhonePoints() {

 		Statement st = null;	 	
		try { 
			st = con.createStatement();
			String sql = "select id,title from contract c left join contract_parameter_type_1 cpt1 on cpt1.cid=c.id where pid='"+paramContractDebug+"'";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			while (res.next()) {
				List<String[]> contractNumbersList = getContractPhoneList(res.getString("title"));
				if (contractNumbersList.size()>0) {
					//привязка номера на договор
					appendPhonePointOnContract(res.getInt("id"), contractNumbersList);		
				 	//привязка ресурса 	на модуль.
					appendPhoneUseOnModule(res.getInt("id"), contractNumbersList);		
				}
				else { logger.debug("у абонента "+res.getString("title")+" ("+res.getInt("id")+") нет номеров"); }
			} 
		}
		catch (SQLException e)  { e.printStackTrace(); }	
    	finally { if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }	 } 		
	}
  
  
  private List<String[]> getContractPhoneList(String contractTitle1) {
	List<String[]> contractPhonePointsList = new ArrayList<String[]>();
	logger.debug("список номеров телефонов абонента.");
	for (int i=1; i<products.length; i++) {		if (contractTitle1.equals(products[i][0])&&(products[i][3].length()>0)) { contractPhonePointsList.add(products[i]); }				}
  	return contractPhonePointsList;
  }

  private void appendPhonePointOnContract(int contractID1, List<String[]>  contractPhonePoints) {
  	for (String[] phoneRecord : contractPhonePoints) {
  		  String phone = StandartPhoneNo(phoneRecord[3],true);
   		  LocalDate Date = getDate(phoneRecord[7]);
		  int workID = getTableVoiceID();
		 dbUpdate("insert into `voice_account_base_"+VOICE_MODULE_ID+"` set `id`='"+workID+"', `title`='"+phone+"',`deviceId`='"+VOICE_DEVICE_ID+"', `typeId`='"+VOICE_ACCOUNT_TYPE_ID+"', `contractId`='"+contractID1+"' , `dateFrom`='"+Date+"'");		 
		 dbUpdate("insert into `voice_account_"+VOICE_MODULE_ID+"` set`id`='"+workID+"',`number`='"+phone+"', `comment`='"+MARK+"', status=0, deviceState=1, sessionCountLimit=1");
		 setSubscriberId(workID, phoneRecord[3]);
		 workID++;
  	}
  }
  
  private void setSubscriberId(int workID1, String phone3){
  	// это мы добавляем в исходную структуру данные о id интерфейса телефона 
  	for (int i=0; i<products.length; i++){		if (phone3.equals(products[i][3])) { products[i][9] = String.valueOf(workID1); }  	}
  }
  
  private int getTableVoiceID() {
	Statement st = null;	
	try { 
			st = con.createStatement();
			String sql = "select max(id) as mID  from `voice_account_base_"+VOICE_MODULE_ID+"`";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			if (res.next()) 	return res.getInt("mID") + 3;
		}
		catch (SQLException e)  { e.printStackTrace(); }	
    	finally { if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); } 	 } 
    throw new IndexOutOfBoundsException("не смог найти индекс для `voice_account_base_"+VOICE_MODULE_ID+"`");	
  }
  
  private void appendPhoneUseOnModule(int contractID1, List<String[]>  contractPhonePoints) {
  
  	for (String[] phoneRecord : contractPhonePoints) {  	
  		String phone = StandartPhoneNo(phoneRecord[3], false);
  		LocalDate date = getDate(phoneRecord[7]);
		String subscriberTitle = clearString(phoneRecord[0]); // +";"+phone;
		int subscriberId = Integer.valueOf(phoneRecord[9]);		
		int phoneResId = getPhoneResourseNum(phone);

// здесь могут оказаться 11и значные номера, которых нет в ресурсах .
   		if (phoneResId<0) {	
			logger.debug ("Ресурса нет. Добавляем и получаем его номер. " + phone + " !!!");
		 	addPhoneToResources(phone);
			phoneResId = getPhoneResourseNum(phone);  
   		} 
   		  
   	    if (phoneResId>0) {
   		  	// подписываем только если ресурс найден.
   		  	subscribeResource(phone, subscriberId, phoneResId, subscriberTitle, date); 
			}
//		else { throw new IndexOutOfBoundsException("всё равно не могу создать привязку к номеру! Чёрт!");		 }
  	}	
  }

  		  
   		    
  private void  subscribeResource(String phone1, int subscriberId2, int phoneResId3, String subscriberTitle4, LocalDate date5){
  		dbUpdate("insert into `inv_phone_resource_subscription_"+VOICE_MODULE_ID+"` set `dateFrom`='"+date5+"',`subscriberTitle`='"+subscriberTitle4+"', `subscriberId`='"+subscriberId2+"', `phone`='"+phone1+"',`phoneResId`='"+phoneResId3+"'");
  }
  
  
    private void addPhoneToResources(String phoneNo1) {
  	dbUpdate("insert into `inv_phone_resource_"+VOICE_MODULE_ID+"` set `dateFrom`='"+DEFAULT_START_DATE+"', `categoryId`	='"+CATEGORY_ID+"', `comment` ='"+MARK+"', phoneFrom='"+phoneNo1+"', 	`phoneTo`='"+phoneNo1+"'");
  }
  
  
  
  private int getPhoneResourseNum(String phone1)  {
  	Statement st = null;	
	try { 
			st = con.createStatement();
			String sql1= "select id, count(*) as count from	`inv_phone_resource_"+VOICE_MODULE_ID+"` where ((`phoneFrom`<="+phone1+") and (`phoneTo`>="+phone1+")) or (`phoneFrom` LIKE \""+phone1+"\" ) or (`phoneTo` LIKE \""+phone1+"\" )";
			logger.info(sql1);
			ResultSet resourceIDs = st.executeQuery(sql1);
			if ((resourceIDs.next()) && (resourceIDs.getInt("count")>0)) {  return resourceIDs.getInt("id");			}

	}
	catch (SQLException e)  { e.printStackTrace(); }	
    finally { 	if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }	     } 
  
  	return -1;
  }
  
 		
  private String StandartPhoneNo(String phone1, boolean flag2) {
  	
  	if (phone1.length()==10) { phone1 =  "7" + phone1; }
  	else if ((phone1.length()==11)&&(phone1.substring(0,1).equals("8"))) {  phone1 =  "7" + phone1.substring(1,11); }
  	else if ((phone1.length()==11)&&(phone1.substring(0,3).equals("8800"))) { phone1 =  "7" + phone1.substring(1,11); } 	
  	else { 
  		if ((flag2)&&(phone1.length()>0)) {
	  			logger.debug(phone1+" - проблемный номер, но он будет добавлен в блок ресурсов и испольован как есть");
  				addPhoneToResources(phone1);
  		}
  	}  	
  	return phone1;
  }
  
 














//================================================
	private void addContractNumberParam() {
 		Statement st = null;	 	
		try { 
			st = con.createStatement();
			String sql = "select id,title from contract c left join contract_parameter_type_1 cpt1 on cpt1.cid=c.id where pid='"+paramContractDebug+"'";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			while (res.next()) { dbUpdate("insert into contract_parameter_type_1 set `pid`="+paramContractNumberID+",`val`='"+res.getString("title")+"',`cid`='"+res.getInt("id")+"'");			} 
		}
		catch (SQLException e)  { e.printStackTrace(); }	
    	finally { if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }	 } 
	}

	private void addContractCompanyParam() {
 		Statement st = null;	 	
		try { 
			st = con.createStatement();
			String sql = "select id,comment from contract c left join contract_parameter_type_1 cpt1 on cpt1.cid=c.id where pid='"+paramContractDebug+"'";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			while (res.next()) { dbUpdate("insert into contract_parameter_type_1 set `pid`="+paramCompanyNameID+",`val`='"+res.getString("comment")+"',`cid`='"+res.getInt("id")+"'");} 
		}
		catch (SQLException e)  { e.printStackTrace(); }	
    	finally { 	if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }    	 } 
	}

	private void addTechParam() {
 		Statement st = null;	 	
		try { 
			st = con.createStatement();
			String sql = "select id from contract where comment LIKE \"%"+MARK+"%\"";
			ResultSet res = st.executeQuery(sql);
			logger.debug(sql);
			while (res.next()) {		
				dbUpdate("insert into contract_parameter_type_1 set `pid`="+paramContractDebug+",`val`='"+MARK+"',`cid`='"+res.getInt("id")+"'");
			} 
			dbUpdate("update contract set comment = replace(comment, '"+MARK+"', '')");
		}
		catch (SQLException e)	{ e.printStackTrace(); }	
    	finally 				{ if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }    	 }  
	}

// ========================================
	private  void addVoiceServices() {
		logger.info ("это список абонплат, на которые будет начислять модуль Voice.");
		
		if (VOICE_PAYS.length < 1)		{ logger.debug("списка нет.");	 }
		else 							{  logger.debug("список есть.");	 addVoiceServicesThan(); }
		
	}

	private void addVoiceServicesThan() {
	 for (String[] service : VOICE_PAYS) {	 	 
		 	dbUpdate("REPLACE into service set `id` = '"+service[1]+"', title='"+service[0]+"', mid='"+VOICE_MODULE_ID+"', lm = '2010-01-01 00:00:00', unit='"+service[2]+"', isusing=1, parentId=0,comment='"+MARK+"'");		
	 	}
	}

	private void addNPayServices() {
	 for (String[] service : NPAY_PAYS) {	 	 
		 	dbUpdate("REPLACE into service set `id` = '"+service[1]+"', title='"+service[0]+"', mid='"+NPAY_MODULE_ID+"', lm = '2010-01-01 00:00:00', unit='"+service[2]+"', isusing=1, parentId=0,comment='"+MARK+"'");		
	 	}
	/*
	 logger.info("колонка [2] в список уникальных сервисов с проверкой на повтор и защиту от перезаписи индекса.");
	  List<String> rawServisesList = new ArrayList<>();
	  for(int i=0; i< products.length; i++) {	 	rawServisesList.add(products[i][2].trim());	 }
	  Set<String> unicServiceListForNPay = new LinkedHashSet<>(rawServisesList); 
	  
     logger.debug("список в базу прописать. ");
	 logger.debug("после отработки скрипта нужно прописать ПРАВИЛЬНУЮ единицу измерения продуктов вручную в редакторе модулей!");
	 for (String service : unicServiceListForNPay) {	 	 
	 	dbUpdate("insert into service set title='"+service+"', mid='"+NPAY_MODULE_ID+"', lm = '2010-01-01 00:00:00', unit=10000, isusing=1, parentId=0,comment='"+MARK+"'");
	 }	 
		*/
	}



// =======================================================================================================
 private void setCon(Connection con1) {con = con1; 		logger.info("создаём коннект на базу, ошибка - вылет"); }

 
 private void dbUpdate(String sql ) {
 	logger.info(sql);	
 	Statement st = null;
   	try{
			st = con.createStatement();
		 	st.executeUpdate(sql);
	}
    catch (SQLException e)  { e.printStackTrace(); }	
    finally { 	if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }	 }   
 }
 

 private void setProducts() throws IOException {
  ArrayList<String> linesList = getArrayList(); // строки файл а массив с полями в ячейках. 
  products = new String[linesList.size()][];
  for (int i=0; i<linesList.size(); i++) {  	
	linesList.set(i,linesList.get(i) + ";-1"); // это поле для id интерфейса - [9] - subscriberId
  	products[i] = linesList.get(i).split(";");  // по хорошему этот список надо прогнать через "очищение" - удаление проблелов, кавычек и прочего. но в тз уверенно не было ничего такого, что создало бы из-за этого проблемы, так что очистка только по месту, а не всего файла-массива начально..
  }
 }


 private ArrayList<String>  getArrayList() throws IOException {
	ArrayList<String> linesList = new ArrayList<String>();
	BufferedReader reader = new BufferedReader(new FileReader(PRODUCT_FILE_PATH));
	String line;    	    
    if (file.exists() && file.isFile()) {				while ((line = reader.readLine()) != null) {  				 linesList.add(line);				} 			  }
	else { throw new FileNotFoundException("Нет файла продуктов! Такой файл должен быть в "+PRODUCT_FILE_PATH);				}
  	return linesList;
 }


}