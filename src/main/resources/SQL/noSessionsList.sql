select DISTINCT ct.title
from inet_serv_1 is1
left join contract ct on is1.contractid = ct.id
left join contract_module cm on (cm.cid=ct.id) and (cm.mid=1)
where
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201601)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201602)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201603)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201604)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201605)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201606)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201607)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201608)
    and
    is1.deviceid  NOT IN (select distinct deviceid from inet_session_log_1_201609)
    and
    (cm.mid=1)
    and
        (
        (`ct`.`gr`&(1<<4)>0) or
        (`ct`.`gr`&(1<<5)>0) or
        (`ct`.`gr`&(1<<19)>0) or
        (`ct`.`gr`&(1<<20)>0) or
        (`ct`.`gr`&(1<<58)>0) or
        (`ct`.`gr`&(1<<38)>0) or
        (`ct`.`gr`&(1<<62)>0) or
        (`ct`.`gr`&(1<<57)>0)
        )