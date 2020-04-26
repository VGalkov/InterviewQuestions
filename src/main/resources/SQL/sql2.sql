select * from contract c 
inner join contract_balance cb on cb.cid=c.id
left join contract_status cs on c.id=c.id and cs.status=12 and (cs.date1>='2020-04-01') and  (cs.date1<='2020-04-05')
where ((`c`.`gr`&(1<<53)>0) or (`c`.`gr`&(1<<55)>0)) and (cb.yy =2020) and (cb.mm =4) and c.status in (12,14) and cb.summa4=0 and (cb.summa1+cb.summa2-cb.summa3-cb.summa4) >=0
group by c.id