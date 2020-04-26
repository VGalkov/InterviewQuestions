select
	`c`.`title`			as 'Л/c',
	`cg`.`title`			as `Группа`,
	`status_galkov`.`title`		as `Статус`,
	`c`.`comment`		as 'Наименование',
	`cpt1`.val			as `ИНН`,
	`cpt2`.val			as `КПП`,
	`address_city`.`title`		as `ГОРОД(Адрес предоставления услуги)`,
	`address_street`.`title`		as 'УЛИЦА(Адрес предоставления услуги)',
	`address_house`.`house`  		as 'ДОМ(Адрес предоставления услуги)',
	`address_house`.`frac`		as 'ОФИС',

	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt3`.val, ',', 2 ), ',', -1 )	as `ГОРОД(Юр.адрес)`,
	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt3`.val, ',', 3 ), ',', -1 )	as 'УЛИЦА(Юр.адрес)',
	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt3`.val, ',', 4 ), ',', -1 )  	as 'ДОМ(Юр.адрес)',
	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt3`.val, ',', 5 ), ',', -1 )	as 'ОФИС(Юр.адрес)',
	`cpt3`.val		as `Юридический адрес`,
				
	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt4`.val, ',', 2 ), ',', -1 )	as `ГОРОД(Фактический адрес)`,
	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt4`.val, ',', 3 ), ',', -1 )	as 'УЛИЦА(Фактический адрес)',
	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt4`.val, ',', 4 ), ',', -1 )	as 'ДОМ(Фактический адрес)',
	SUBSTRING_INDEX( SUBSTRING_INDEX( `cpt4`.val, ',', 5 ), ',', -1 )	as 'ОФИС(Фактический адрес)',
	`cpt4`.val		as `Фактический адрес`



 


/*
21.12.2015  добавлены абоненты в статусе "закрыт"
28. авг. 2015 г. в отчёте присутствуют абоненты, которые относятся к "физическим лицам", которые находятся в группах абонентов, относящихся к физлицам
	 в расчёт попадают И физические лица не отнесённые ни к одной группе абонентов!!
15. января 2016 г. добавлены литеры к номерам домов.
*/
    from `contract` c

left join	`status_galkov`	on	`status_galkov`.`status` = `c`.`status`
left join	`contract_parameter_type_2`	on	`c`.`id`                                =`contract_parameter_type_2`.`cid` 
left join	`address_house`	on	`contract_parameter_type_2`.`hid`       = `address_house`.`id` 
left join	`address_street`	on	`address_street`.`id`                   = `address_house`.`streetid` 
left join	`address_area`	on	`address_house`.`areaid`                = `address_area`.`id` 
left join	`address_quarter`	on	`address_house`.`quarterid`             = `address_quarter`.`id` 

left join	`address_city`		on	`address_city`.`id`                     = `address_street`.`cityid`
left join	`address_config`	on 	(`address_config`.`key`='ConnectTechnology') and (`address_config`.`table_id`='address_house') and `address_config`.`record_id` = `address_house`.id
left join       contract_group cg on (c.gr&(1<<cg.id)>0)
/*(`c`.`gr`&(1<<19)>0) */

left join       `contract_tariff` ct   on (`ct`.`cid` = c.id )
left join       `tariff_plan` tp on `tp`.`id` = `ct`.`tpid`
left join	`contract_parameter_type_1` cpt1 on cpt1.cid=c.id and cpt1.pid=32
left join	`contract_parameter_type_1` cpt2 on cpt2.cid=c.id and cpt2.pid=33

left join	`contract_parameter_type_1` cpt3 on cpt3.cid=c.id and cpt3.pid=16 
left join	`contract_parameter_type_1` cpt4 on cpt4.cid=c.id and cpt4.pid=17

where
    (c.fc=1)	and (cg.id NOT IN (8,6,9,10,11,12,13,14,21,22,48,49,51,59,60))
group by `c`.`title`
order by   `c`.`title`

