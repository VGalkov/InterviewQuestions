select distinct cp.pt, cpt.title
from contract_payment cp 
left join contract_payment_types cpt on cpt.id=cp.pt
where YEAR(cp.dt) =YEAR(NOW()) and (MONTH(NOW()) <= (MONTH(cp.dt)+3)) and (cp.summa>0) 
order by cp.pt