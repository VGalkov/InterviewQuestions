select
    c.title,
    c.id,
    (`cb`.`summa1`+`cb`.`summa2`-`cb`.`summa3`-`cb`.`summa4`),
    c.closesumma,
    if (((`cb`.`summa1`+`cb`.`summa2`-`cb`.`summa3`-`cb`.`summa4`)-c.closesumma)>0, 'ok', 'нужен лимит')

from contract c
inner join contract_status cs on cs.cid=c.id and cs.status=14
inner join contract_balance cb  on  (cb.cid=c.id)  and  (cb.yy=year(cs.date1)) and (cb.mm = month(cs.date1))
where
        (
         (`c`.`gr`&(1<<15)>0) or
         (`c`.`gr`&(1<<54)>0) or
         (`c`.`gr`&(1<<53)>0) or
         (`c`.`gr`&(1<<55)>0)
        )
and c.status = 12 and ((`cb`.`summa1`+`cb`.`summa2`-`cb`.`summa3`-`cb`.`summa4`)<0)
order by if (((`cb`.`summa1`+`cb`.`summa2`-`cb`.`summa3`-`cb`.`summa4`)-c.closesumma)>0, 'ok', 'нужен лимит')