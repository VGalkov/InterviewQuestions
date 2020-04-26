select
    rl.dt as 'Дата перехода',
    c.title as 'МВ-Самара л.с.',
    cpt.val as 'Ростелеком л.с.',
    `cb`.`summa4` as 'Расход',
    (`cb`.`summa1`+`cb`.`summa2`-`cb`.`summa3`-`cb`.`summa4`) as 'Исходящий остаток',
    if ((`cb`.`summa1`+`cb`.`summa2`-`cb`.`summa3`-`cb`.`summa4`)<0,(`cb`.`summa1`+`cb`.`summa2`-`cb`.`summa3`-`cb`.`summa4`), `cb`.`summa4`) as 'Цифра, отправленная в РТК',
    cg.title as 'группа',
    c.comment as 'ФИО',
    if(cpt5.val=1, "Подписано ДС", "-") as 'Алгоритм'

from contract c 
left join rostelecom_log rl on rl.acc =c.title
left join contract_parameter_type_1  cpt on c.id=cpt.cid and cpt.pid=71
left join contract_parameter_type_5  cpt5 on c.id=cpt5.cid and cpt5.pid=73
left join contract_balance cb  on  (cb.cid=c.id)  and  ((cb.yy*12+cb.mm) =  (select max(cb1.yy*12+cb1.mm) from contract_balance cb1 where (`cb1`.`cid` = `c`.`id`)))
left join       contract_group cg on (c.gr&(1<<cg.id)>0)
where 	 
/*(cg.id NOT IN (8,6,9,10,11,12,13,14,21,22,48,49,51,59,60))*/
c.status in (12,14) /* and c.id = 35446 and c.title = "I_66091" */
group by c.id
order by rl.dt,c.id