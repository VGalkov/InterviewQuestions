function LoginCheck() {
	$('#MakeLogin').submit(function(e)    {
		e.preventDefault();                 
        var m_method=$(this).attr('method');
        var m_action=$(this).attr('action');
        var m_data=$(this).serialize();     
        $.ajax({
                type: m_method,
                url: m_action,
                data: m_data,
                success: function(result){ if (result == "true") { HideThem(['LoginForm','LoginFormBak']); } }
        });
    }); 
}


function ShowTree() {
// ID, (ip)
	var sel = document.getElementById("GraphList"); 
	var val = sel.options[sel.selectedIndex].value;
	ID = val.split(',');
	$.ajax({
	       type: "POST",
	       url: "SelectorOptions",
	       data: "Type=ShowTree&ID="+ID[0],
	       dataType: "html",
	       success: function(result){
				document.getElementById('EditorPanel').innerHTML = GetTopLine('EditorPanel')+result;
				ShowThis('EditorPanel');
			}
	});
	SearchParams = "JSONType=JSONShowTreeList&ID="+ID[0],
	ReSetMap(SearchParams);
	
}

function SubmitParamSearch() {
    $('#Form1').submit(function(e)    {
        e.preventDefault();                 
        var m_method=$(this).attr('method');
        var m_action=$(this).attr('action');
        var m_data=$(this).serialize();     
        $.ajax({
                type: m_method,
                url: m_action,
                data: m_data,
                success: function(result){
    				document.getElementById('EditorPanel').innerHTML = GetTopLine('EditorPanel')+result;
    				ShowThis('EditorPanel');
					if (Order=="Map")  { ReSetMap(m_data); }
					if (Order=="Line") { ReSetLine(m_method, m_data); }
    			}
        });
    });
}


function SubmitSellsSearch() {
//  2
    $('#Form2').submit(function(e)    {
            e.preventDefault();                     //отменяем стандартное действие при отправке формы
            var m_method=$(this).attr('method');    //берем из формы метод передачи данных
            var m_action=$(this).attr('action');    //получаем адрес скрипта на сервере, куда нужно отправить форму
            var m_data=$(this).serialize();         //получаем данные, введенные пользователем в формате input1=value1&input2$
            $.ajax({
                    type: m_method,
                    url: m_action,
                    data: m_data,
                    // где показывать.
                    success: function(result){      
                    	document.getElementById('EditorPanel').innerHTML = GetTopLine('EditorPanel')+result;
                    	ShowThis('EditorPanel'); 
            			if (Order=="Map")  { ReSetMap(m_data); }
            			if (Order=="Line") { ReSetLine(m_method, m_data); }
                    }
            });
    });
    
}

function  SumbitAdrSearch() {
	$('#Form3').submit(function(e)    {
        e.preventDefault();                     //отменяем стандартное действие при отправке формы
        var m_method=$(this).attr('method');    //берем из формы метод передачи данных
        var m_action=$(this).attr('action');    //получаем адрес скрипта на сервере, куда нужно отправить форму
        var m_data=$(this).serialize();         //получаем данные, введенные пользователем в формате input1=value1&input2$
        $.ajax({
                type: m_method,
                url: m_action,
                data: m_data,
                success: function(result){	
                		document.getElementById('EditorPanel').innerHTML = GetTopLine('EditorPanel')+result;
                		ShowThis('EditorPanel'); 
						if (Order=="Map")  { ReSetMap(m_data); }
						if (Order=="Line") { ReSetLine(m_method, m_data); }
                }
        });
	});	
};

function RememberNewUser() {

	$('#UserAddFormExec').submit(function(e)    {
        e.preventDefault();                   
        var m_method=$(this).attr('method');  
        var m_action=$(this).attr('action');   
        var m_data=$(this).serialize();        
        $.ajax({
                type: m_method,
                url: m_action,
                data: m_data,
                success: function(result){	GetHTML(['Userlist']); }
        });
	});
}

function SumbitUpdateHardwareLink(){ 
	$('#UpdateHardWare').submit(function(e)    {
        e.preventDefault();                   
        var m_method=$(this).attr('method');  
        var m_action=$(this).attr('action');   
        var m_data=$(this).serialize();        
        $.ajax({
                type: m_method,
                url: m_action,
                data: m_data,
                success: function(result){	ShowStyle(); DrawInterface();GetHTML(['ShowEditorList']);   }
        });
	});
}


function SumbitAddHardwareLink(){
	$('#RememberHardWare').submit(function(e)    {
        e.preventDefault();                     
        var m_method=$(this).attr('method');   
        var m_action=$(this).attr('action');    
        var m_data=$(this).serialize();        
        $.ajax({
                type: m_method,
                url: m_action,
                data: m_data,
                success: function(result){	
                	if (Order=="AddHardWareMap")	{ Order = "Map"; ShowStyle(); DrawInterface(); GetMarksAll(); } 
                	if (Order=="AddHardWareLine")	{ Order = "Line"; ShowStyle(); DrawInterface(); }
                }
        });
	});
}


function ReDrawHouses(StrtID) {
	$.ajax({
        type: "POST",
        url: "SelectorOptions",
        data: "Type=ReDrawHouses&StrtID="+StrtID,
        dataType: "html",
        success: function(result){ document.getElementById('Form3dom').innerHTML = result;   }
    });
}

function MakePing(IP) {
	$.ajax({
        type: "POST",
        url: "SelectorOptions",
        data: "Type=MakePing&StrtID="+IP,
        dataType: "html",
        success: function(result){    
        				document.getElementById('PingPanel').innerHTML = GetTopLine('PingPanel')+result;
        				ShowThis('PingPanel');
        }
    });	
}

function TechState(ID1) {
	$.ajax({
        type: "POST",
        url: "SelectorOptions",
        data: "Type=ShowInfo&ID="+ID1,
        dataType: "html",
        success: function(result){    
        				document.getElementById('PingPanel').innerHTML = GetTopLine('PingPanel')+"<div>"+result+"</div>";
        				ShowThis('PingPanel');
        }
    });	
}

function EditThisHardWare(ID)
{	
//	alert(ID);
	$.ajax({
        type: "POST",
        url: "SelectorOptions",
        data: "Type=EditThisHardWare&ID="+ID,
        dataType: "html",
        success: function(result){
        				document.getElementById('EditorPanel').innerHTML = GetTopLine('EditorPanel')+result;
        				ShowThis('EditorPanel');
        }
    });
}

function GetContent() {
	if (Order=="Map")  			{ GetMarksAll();  }	
	if (Order=="Line")			{ ReSetLine(); }
}
function ShowIT(ID) {
//	alert(Order);	
	if (Order=="Map")  				{ ReSetMapByID(ID);  }
	if (Order=="EditHardWareMap")	{ ReSetMapByID(ID);  }	
	if (Order=="Line")				{ ReSetLineByID(ID); }
}

function DeleteThisHardWare(ID)
{	
	$.ajax({
        type: "POST",
        url: "SelectorOptions",
        data: "Type=DeleteThisHardWare&ID="+ID,
        dataType: "html",
        success: function(result){   
        	HideThem(['MBS10','MBS11','MBS13','MBS14','MBS23','MBS18','MBS17']);
        	GetHTML(['ShowEditorList']);       
        }
    });
}


function SetHimToAdmin(User) {
	$.ajax({
		type: "POST",
		url: "SelectorOptions",
		data: 'Type=SetHimToAdmin&User='+User,
		dataType: "html",
		success: function(result){ GetHTML(['Userlist'])	}
	});
}


function GoalOperator(ip,id) {
	var sel = document.getElementById("Goals"+id); 
	var val = sel.options[sel.selectedIndex].value;
	switch (val) {
		case 'Пинг':					MakePing(ip);							break;
		case 'Редактор':				EditThisHardWare(id);	ShowIT(id);		break;
		case 'Удалить': 				DeleteThisHardWare(id);					break;
		case 'Техническое состояние': 	TechState(id);							break;
		case 'Конструктор':		ShowConstructor(['MBS10','MBS11','MBS12','MBS14','MBS13','MBS18','MBS17']);	break; 
		default: alert("Не реализовано!");
	}
}



function SetHimToUser(User) {
	$.ajax({
		type: "POST",
		url: "SelectorOptions",
		data: 'Type=SetHimToUser&User='+User,
		dataType: "html",
		success: function(result){ GetHTML(['Userlist'])	}
	});	
}
function DeleteHim(User) {
	$.ajax({
		type: "POST",
		url: "SelectorOptions",
		data: 'Type=DeleteHim&User='+User,
		dataType: "html",
		success: function(result){ GetHTML(['Userlist'])	}
	});
}

function GetHTML(List){
	for (var i = 0; i < List.length; i++) {
		switch(List[i]) {
			case 'SearchForm5':
				$.ajax({
					type: "POST",
					url: "SelectorOptions",
					data: 'Type=' + List[i],
					dataType: "html",
					success: function(result){ 
						document.getElementById('SearchForm2').innerHTML = result;
						ShowThis('SearchForm2');
				}
				});
				break;
			case 'SearchForm2':
				$.ajax({
					type: "POST",
					url: "SelectorOptions",
					data: 'Type=' + List[i],
					dataType: "html",
					success: function(result){    
						document.getElementById('SearchForm2').innerHTML = result;
						ShowThis('SearchForm2');
					}
				});
				break;
			case 'UserAddForm':
				$.ajax({
					type: "POST",
					url: "SelectorOptions",
					data: 'Type=' + List[i],
					dataType: "html",
					success: function(result){	document.getElementById('map').innerHTML = result;	}
				});
				break;
			case 'Userlist':
				$.ajax({
					type: "POST",
					url: "SelectorOptions",
					data: 'Type=' + List[i],
					dataType: "html",
					success: function(result){    
						document.getElementById('EditorPanel').innerHTML = GetTopLine('EditorPanel')+result;
						ShowThis('EditorPanel');
					}
				});
				break;
			case 'SearchForm1':
				$.ajax({
					type: "POST",
					url: "SelectorOptions",
					data: 'Type=' + List[i],
					dataType: "html",
					success: function(result){    
						document.getElementById('SearchForm1').innerHTML = result;
						ShowThis('SearchForm1');
					}
				});
				break;
			case 'SearchForm3':
				$.ajax({
					type: "POST",
					url: "SelectorOptions",
					data: 'Type=' + List[i],
					dataType: "html",
					success: function(result){    
						document.getElementById('SearchForm3').innerHTML = result;
						ShowThis('SearchForm3');
					}
				});
				break;
			case 'ShowEditorList':
				
				if (Order == "Map") {	Order = "EditHardWareMap";}
				if (Order == "Line") {	Order = "EditHardWareLine";}
				ShowStyle();
				$.ajax({
			        type: "POST",
			        url: "SelectorOptions",
					data: 'Type=' + List[i],
			        dataType: "html",
			        success: function(result){    
			        				document.getElementById('EditorPanel').innerHTML = GetTopLine('EditorPanel')+result;
			        				ShowThis('EditorPanel');
			        }
			    });
				break;
		
			case 'AddPanel':
				Order ='AddHardWareMap'; ShowStyle(); HideThem(['MBS10','MBS11','MBS12','MBS14','MBS23']);
				$.ajax({
					type: "POST",
					url: "SelectorOptions",
					data: 'Type=' + List[i],
					dataType: "html",
					success: function(result){    document.getElementById('map').innerHTML = result;      }
				});	
				break;
			default: alert("Error!");		
		}
	}
};




function GetTopLine(Target) {
	return "<button id='MBS123474234' class='btn-pink' onclick='HideThis(`"+Target+"`)'>x</button>";
}


function ShowStyle() {	
 if (Order == 'Map')  			{ 	ymaps.ready(init);	 	myLine("destroy"); }
 if (Order == 'Line')   		{	myLine("init"); 		if(typeof myMap != "undefined") { myMap.destroy(); } }
 if (Order == 'Users')  		{	myLine("destroy"); 		if(typeof myMap != "undefined") { myMap.destroy(); } }
 
 if (Order == 'AddHardWareMap')  	{	myLine("destroy"); 		if(typeof myMap != "undefined") { myMap.destroy(); } }

 
 if (Order == 'AddHardWareLine')  	{	myLine("init"); 		if(typeof myMap != "undefined") { myMap.destroy(); } }
 if (Order == 'Construtor') 	{ 	myLine("init");			if(typeof myMap != "undefined") { myMap.destroy(); } } 
}


function DrawInterface() {
	switch(Order) {
	case "Map":	
		ShowThem(['MBS99','MBS10','MBS11','MBS12','MBS13','MBS14','BlueButtonsPannel']);
		HideThem(['MBS23','MBS18','MBS17']);
		break;
		
	case "Line":		
		ShowThem(['MBS99','MBS23','MBS10','MBS11','MBS12','MBS13','MBS14','BlueButtonsPannel']);
		HideThem(['MBS18','MBS17']);
		break;
		
	case "Users":
		ShowThem(['MBS18','MBS17','BlueButtonsPannel','MBS99']);
		HideThem(['MBS11','MBS12','MBS13','MBS14','MBS23','MBS10']);
		break;

	default:
		HideThem(['MBS18','MBS17','MBS99','MBS10','MBS11','MBS12','MBS13','MBS14','MBS23','BlueButtonsPannel']);	
		break;
	}

}

function HideThis(qwert) {	document.getElementById(qwert).setAttribute('style','visibility:hidden');		}
function ShowThis(qwert)  {	document.getElementById(qwert).setAttribute('style','visibility:visible');	}


function HideThem(List) {	for (var i = 0; i < List.length; i++) { HideThis(List[i]);}		}
function ShowThem(List) {	for (var i = 0; i < List.length; i++) { ShowThis(List[i]);}		}

function ClearFilter() {
// вообще-то это ну
	FilterConfig.Street =	ALL;
	FilterConfig.Cell 	=	ALL;
	FilterConfig.House 	=	ALL;
	FilterConfig.Pod 	=	ALL;
	FilterConfig.IP 	=	ALL;
	FilterConfig.Type 	=	ALL;
}

function ShowFilterConfig(){
	var html = "<table width='100%' height='100%'><tr><td>Состояние фильтра "+ Order+ "</td><td>"+GetTopLine('FilterCfgPanel') + "</td></tr>" +
			" <tr><td>Улицы: </td><td>" + FilterConfig.Street + "</td></tr>" +
			" <tr><td>Участки: </td><td>"+ FilterConfig.Cell + "</td></tr>" +
			" <tr><td>Дома: </td><td>"+ FilterConfig.House + "</td></tr>" +
			" <tr><td>IP: </td><td>"+ FilterConfig.IP + "</td></tr>" +
			" <tr><td>Подъезды: </td><td>"+ FilterConfig.Pod+"</td></tr>";
			" <tr><td>Тип: </td><td>"+ FilterConfig.Type+"</td></tr></table>";
	document.getElementById('FilterCfgPanel').innerHTML = html;
	ShowThis('FilterCfgPanel');
}

function Login() {	ShowThem(['LoginForm','LoginFormBak']);}


// что ЭТО?!?!?!
function getElementComputedStyle(elem, prop)
{
  if (typeof elem!="object") elem = document.getElementById(elem);
  
  // external stylesheet for Mozilla, Opera 7+ and Safari 1.3+
  if (document.defaultView && document.defaultView.getComputedStyle)
  {
    if (prop.match(/[A-Z]/)) prop = prop.replace(/([A-Z])/g, "-$1").toLowerCase();
    return document.defaultView.getComputedStyle(elem, "").getPropertyValue(prop);
  }
  
  // external stylesheet for Explorer and Opera 9
  if (elem.currentStyle)
  {
    var i;
    while ((i=prop.indexOf("-"))!=-1) prop = prop.substr(0, i) + prop.substr(i+1,1).toUpperCase() + prop.substr(i+2);
    return elem.currentStyle[prop];
  }
  
  return "";
}