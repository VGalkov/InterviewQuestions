function myLine(arrow) {
	// alert(arrow);
	if (arrow=="init") {
		$.ajax({
			type: "POST",
			url: "SelectorOptions",
			data: "Type=GetRootElement",
			dataType: "html",
			success: function(result){
				var Str = "На схеме будут отображаться только элементы каким-либо образом связанные с этим элементом (по цепочке через другие или напрямую): ";
				Str = Str + result;
				document.getElementById('map').innerHTML = "<img title='"+Str+"' alt='"+Str+"' id ='Outline' class='line' src='LineImg' >";
			}
		});	
	}
	else {	document.getElementById('map').innerHTML = "&nbsp;";	}
}


function ReSetLineByID(ID) {	alert ("Пересобрать схему с новыми данными поиска:"+ID);	}

function ReSetLine(a1,a2){	myLine('init');	}

function ShowConstructor(List) {
	Order = "Constructor"; HideThem(List);	ShowStyle();
	$.ajax({
        type: "POST",
        url: "SelectorOptions",
        data: "Type=ShowConstructor",
        dataType: "html",
        success: function(result){    
        	document.getElementById('EditorPanel').innerHTML =  GetTopLine('EditorPanel')+result;
        	ShowThis('EditorPanel');
        }
    });
}


function SumbitConstructorLink(){
	$('#Constructor').submit(function(e)    {
        e.preventDefault();                     //отменяем стандартное действие при отправке формы
        var m_action=$(this).attr('action');    //получаем адрес скрипта на сервере, куда нужно отправить форму
        var m_data=$(this).serialize();         //получаем данные, введенные пользователем в формате input1=value1&input2$
        $.ajax({
                type: "POST",
                url: m_action,
                data: m_data,
                dataType: "html",
                success: function(result){	document.getElementById('ekran1').innerHTML =  result; }
        });
	});
};

function ShowConstructorLinks(What,Position){
	ID = What.split(',');
	$.ajax({
        type: "POST",
        data: "Type=ShowConstructorLinks&What="+What+"&Where="+Position,
        url: "SelectorOptions",
        dataType: "html",
        success: function(result){  document.getElementById('ekran1').innerHTML =   result;	ShowThis(ekran1);    }
    });
}