// https://tech.yandex.ru/maps/jsbox/2.0/geocode


function init() {
    ymaps.geocode('Самара', { results: 1 }).then(function (res) {
        firstGeoObject = res.geoObjects.get(0), myMap = new ymaps.Map("map", {center: firstGeoObject.geometry.getCoordinates(), zoom: 13});
        myMap.container.fitToViewport();   	
	}, function (err) {	alert(err.message);	}
    );
}


function ReSetMap(SearchParams){
//	alert (SearchParams);
	ClearMarksALL();
	 $.ajax({
	     type: "POST",
         url: "JsonMarksNew",
         data: SearchParams,
         success: function(result){   
     		var geo = JSON.parse(result);
 		    myCollection = new ymaps.GeoObjectCollection();
 		    for(i = 0; i < geo.length; i++) {
 		    	if ((parseFloat(geo[i].xxx) != 0) && (parseFloat(geo[i].xxx) != 0)) {
 		    		myCollection.add(marker(parseFloat(geo[i].xxx), parseFloat(geo[i].yyy), geo[i].title, geo[i].IP, geo[i].Comment, geo[i].Type,geo[i].Adr,false,""));
 		    	}
 		    }
 			myMap.geoObjects.add(myCollection);
 			myMap.setBounds(myCollection.getBounds());
 			mapFlag= true; // факт сформированности коллекции.
        	 
         }
	 })
}



function GetMarksAll() {
	ClearMarksALL();
	 $.ajax({
        type: "POST",
        url: "JsonMarksNew",
        data: "JSONType=JSONALLMarks",
        success: function(result){   
    		var geo = JSON.parse(result);
		    myCollection = new ymaps.GeoObjectCollection();
//		    var i = 0;
		    for(i = 0; i < geo.length; i++) {
		    	if ((parseFloat(geo[i].xxx) != 0) && (parseFloat(geo[i].xxx) != 0)) {
		    		myCollection.add(marker(parseFloat(geo[i].xxx), parseFloat(geo[i].yyy), geo[i].title, geo[i].IP, geo[i].Comment, geo[i].Type,geo[i].Adr, false,""));
		    	}
		    }
			myMap.geoObjects.add(myCollection);
			myMap.setBounds(myCollection.getBounds());
			mapFlag= true; // факт сформированности коллекции.
       	 
        }
	 })
}

function ReSetMapByID(ID) {
	ClearMarksALL();
	 $.ajax({
       type: "POST",
       url: "JsonMarksNew",
       data: "JSONType=JSONMarkByID&ID="+ID,
       success: function(result){   
   		var geo = JSON.parse(result);
		    myCollection = new ymaps.GeoObjectCollection();
		    var i = 0;
		    for(i = 0; i < geo.length; i++) {
		    	if ((parseFloat(geo[i].xxx) != 0) && (parseFloat(geo[i].yyy) != 0)) {
		    		myCollection.add(marker(parseFloat(geo[i].xxx), parseFloat(geo[i].yyy), geo[i].title, geo[i].IP, geo[i].Comment, geo[i].Type,geo[i].Adr, true,ID));
		    	}
		    }
			myMap.geoObjects.add(myCollection);
//			myMap.setBounds(myCollection.getBounds()); //позиционирование на точку.
			mapFlag= true; // факт сформированности коллекции.      	 
       }
	 })
}


// https://tech.yandex.ru/maps/doc/jsapi/2.0/ref/reference/GeoObjectCollection-docpage/
function ClearMarksALL() {	if(mapFlag) { mapFlag= false;	myCollection.removeAll(); }	}

function marker(GeoX,GeoY, Title, IP, Comment, Type,Adr, Dragable,ID) {
	    var min = 0; var max = 9; var i = 1; var j = 1;  var mi = 0.000001; var ma = 0.000003;
	    if ((Math.random() * (max - min) + min) > 4 ) { i = -1; } else { i = 1;}    if ((Math.random() * (max - min) + min) > 4 ) { j = -1; } else { j = 1;}
	    var CorX = (Math.random() * (ma - mi) + mi)*GeoX*i+GeoX;
	    var CorY = (Math.random() * (ma - mi) + mi)*GeoY*j+GeoY;
	    design = 'islands#icon';     col = '#0095b6';

	    if (Type == "Агрегация")    {       design = 'islands#dotleIcon';                   col = '#3caa3c'; }
	    if (Type == "Коммутатор")   {       design = 'islands#circleIcon';                  col = '#3caa3c'; }
	    if (Type == "ДУ")           {       design = 'islands#dotIcon';                     col = '#735184'; }
	    if (Type == "ОП")           {       design = 'islands#circleIcon';                  col = '#735184'; }
	    if (Type == "ОУ")           {       design = 'islands#circleIcon';                  col = '#FFA07A'; }
	    if (Type == "Свой вариант") {       design = 'islands#dotIcon';                     col = '#FF0000'; }
	     
	    var Title_1 = "<font color="+col+">"+Title+"</font>";
	    if (Dragable) {
	    	myPlacemark = new ymaps.Placemark([CorX,CorY], {
		        balloonContentHeader: Title_1 +" ("+IP+")",
		        balloonContentBody: Adr+", "+Comment+", "+Type,
		        hintContent: "передвинь меня"
		    }, { draggable: true,	        preset: design,	        iconColor: col	        });	
	    	// https://tech.yandex.ru/maps/doc/jsapi/2.0/dg/concepts/events-docpage/	    	
	    	myPlacemark.events.add('dragend', function (e) {
	    		 $.ajax({
	                 type: "POST",
	                 url: "JsonMarksNew",
	                 data: "JSONType=NewMarkCoordinates&ID="+ID+"&Coordinates="+myPlacemark.geometry.getCoordinates(),
//	                 success: function(result){  }
	    		 });
	    	});
	    }
	    else {
	    	myPlacemark = new ymaps.Placemark([CorX,CorY], {
		        balloonContentHeader: Title_1 +" ("+IP+")",
		        balloonContentBody: Adr+", "+Comment+", "+Type,
		        hintContent: Title_1 +" "+IP
		    },  { preset: design,		        iconColor: col });	
	    }
	    return myPlacemark;
	}