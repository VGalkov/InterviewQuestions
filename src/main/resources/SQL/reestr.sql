select 
	NOW()  as 'Дата формирования',
	cpt1.val  as 'л.с. Ростелекома',
	c.title as 'л.с. МВ-Самара', 
	c.comment as 'ФИО', 
	c.status_date as 'Дата перехода', 
	CASE
    		WHEN  c.status= 12
		        THEN 'Переведён в РТК,Активен'
		WHEN c.status= 13
		       THEN 'Переведён в РТК, Отключен РТК'
		WHEN c.status= 14
		        THEN 'Перейдёт в ближайшую полночь'
	end as 'Статус',
	c.id as 'индекс в базе'
from contract c
inner join contract_parameter_type_1 cpt1 on cpt1.cid=c.id and cpt1.pid=71
where c.status in (12,13,14)
order by cpt1.val 