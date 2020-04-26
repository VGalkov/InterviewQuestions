SELECT
    pl.dt ,
    if ((u.title is NULL), "Сервер", if ((u.title LIKE "new%"), "Call-центр", u.title)) as user,
    CAST( pl.data AS CHAR),
    "",
    pl.id as id_
FROM  bgcrm.process_log  pl
left join bgcrm.user u on u.id= pl.user_id
WHERE (YEAR(pl.dt) = 2017) and    (MONTH(pl.dt) = 11) 
UNION
SELECT
    ps.dt,
    if ((u.title is NULL), "Сервер", if ((u.title LIKE "new%"), "Call-центр", u.title)) as user,
    CAST(ps.status_id AS CHAR) ,
    ps.comment,
    ps.process_id as id_
FROM  bgcrm.process_status ps
left join bgcrm.user u on u.id= ps.user_id
WHERE (YEAR(ps.dt) = 2017) and    (MONTH(ps.dt) = 11)



/* UNION SELECT
    plog.dt,
    if ((u.title is NULL), "Сервер", if ((u.title LIKE "new%"), "Call-центр", u.title)) as user,
    CAST(plog.text AS CHAR),
    "",
    ""
FROM  bgcrm.param_log plog
left join bgcrm.user u on u.id= plog.user_id
WHERE (YEAR(plog.dt) = 2017) and    (MONTH(plog.dt) = 11) */

order by id_