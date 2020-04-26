set @tab1 := concat("log_sessions_",year(now()), month(now()));
set @sql = concat(
    'select * from ',
    @tab1,
    ' where date_format(hour,"%d%.%m.%Y") = date_format(now(),"%d%.%m.%Y")'
);
prepare st from @sql;
execute st;