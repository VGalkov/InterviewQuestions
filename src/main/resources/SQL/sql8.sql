select    DISTINCT c.id as id,    c.title as ls,    CONCAT('7',substr(cp.value,2,10)) as phone,    
(CEIL(ABS((cb.summa1+cb.summa2-cb.summa3-cb.summa4 - sum(ca.summa))))) as delta 

from contract c 
left join contract_balance cb on (cb.cid=c.id) and (cb.mm=MONTH(NOW())) 
left join contract_account ca on (ca.cid=c.id) and (ca.mm=MONTH(NOW())) and (ca.yy=YEAR(NOW())) and (ca.sid NOT IN (select sid from rscm_service_16 )) 
left join contract_parameter_type_7 a  on (a.cid=c.id) and (a.pid=59)
left join contract_parameter_type_phone cp on (cp.cid=c.id) and (substr(cp.value,1,4) NOT IN ('7846','8846','7812','8812','8817')) and (cp.pid=13) 
left join contract_group cg on (`c`.`gr`&(1<<cg.id)>0) 

where (c.del=0) and (a.val="+OFF+") and (c.status=0) and  ((select DAY(NOW())>"+DaysToInform+") and
 (select DAY(NOW())<32))    and     (        (`c`.`gr`&(1<<5)>0)     or        (`c`.`gr`&(1<<19)>0)    or        (`c`.`gr`&(1<<20)>0)     )    and   
 ((c.date1 IS NULL) or (c.date1<NOW()))    and    ((c.date2 IS NULL) or (c.date2>NOW()))    and (cb.mm=MONTH(NOW())) and (cb.yy=YEAR(NOW()))    and (ca.mm=MONTH(NOW())) and (ca.yy=YEAR(NOW()))    and (ca.sid NOT IN (select sid from rscm_service_16 ))    and (cp.value IS NOT NULL) and (length(cp.value)>=11)    and (cb.summa1+cb.summa2-cb.summa3-cb.summa4) - (select sum(ca.summa) as abon from contract_account ca where (ca.cid=c.id) and (ca.mm=MONTH(NOW())) and (ca.yy=YEAR(NOW())) and (ca.sid NOT IN (select sid from rscm_service_16 ))) <0 

group by c.id having (sum(ca.summa) > 0) order by c.id DESC`summa2`-`cb`.`summa3`-`cb`.`summa4`)-c.closesumma)>0, 'ok', 'нужен лимит')