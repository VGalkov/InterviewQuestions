/* последовательность вычисления переменных - сверху вниз!!!*/
select
    'Все источники' as PayTypeStr,
    c.title     as TITLE,
    c.comment   as FIO,
    c.id        as cid,
    NOW()       as now,
    cg.title    as GRP,
    concat($(month),' - ',$(year))      as dttt,
    concat(cb.mm,' - ',cb.yy)           as dtr,

    @PAY := if ((select sum(cp.summa) from `contract_payment` cp where (`cp`.`cid` = `c`.`id`) and (YEAR(cp.dt) = $(year)) and (MONTH(cp.dt) = $(month)) and (cp.pt = 22)) is null, 0,(select sum(cp.summa) from `contract_payment` cp where (`cp`.`cid` = `c`.`id`) and (YEAR(cp.dt) = $(year)) and (MONTH(cp.dt) = $(month)) and (cp.pt = 22)))
    PAY,

    @CHA := if ((select sum(ch.summa) from `contract_charge`  ch where (`ch`.`cid` = `c`.`id`) and (YEAR(ch.dt) = $(year)) and (MONTH(ch.dt) = $(month)) and (ch.pt = 4)) is null, 0,(select sum(ch.summa) from `contract_charge`  ch where (`ch`.`cid` = `c`.`id`) and (YEAR(ch.dt) = $(year)) and (MONTH(ch.dt) = $(month)) and (ch.pt = 4)))
    CHA,

    @C := (if ( ((cb.yy*12) + cb.mm)=(($(year)*12) + $(month)),cb.summa1,cb.summa1+(cb.summa2-@PAY)-cb.summa3-cb.summa4))
    VBAL,                                       /*      входящий остаток        */

    @K := if ( (((cb.yy*12) + cb.mm)=(($(year)*12) + $(month))), cb.summa2 - @PAY, 0)
    OV,                                 /* оплата всего  */

    @D := if ((@C<0), @C,0)
    DZNP,                               /*      дебеторская задолженность на начало периода     */

    @E := if ((@C>=0),@C,0)
    ANP,                                /*      авансы на начало периода        */

    @F := if ( (((cb.yy*12) + cb.mm)=(($(year)*12) + $(month))), cb.summa3, 0)
    HP,                                 /*      начисленно, расход. */

    @G := if (((@D+@E)<0),0,if((@F-(@D+@E)<0),@F,(@D+@E)))
    ZA,                                 /*  Авансы зачтённые */

    @H := if (
                (@D+@E)>0,
                0,
                if(((@K+@D)>0),((-1)*@D),@K)
    )
    DZPP,
    
    @I := if (
                ((@D+@E)<0),
                if((@K-@H)>@F,@F,(@K-@H)),
                if((@K+@G)>@F,(@F-@G),@K)
    )
    DZTP,

    @J := (@K - @H - @I)
    AP,

    @M := if(
                ((@E-@F)>=0),
                ((@E-@F)+@J),
                if((@F>0),@J,0)
            )
    AKP,

    @L := if((@M>0), 0,((@D-@F)+(@K+@G)))
    DZKP                                        /* дебеторская задолженность на конец периода */






from contract c

left join `contract_group`      cg on cg.id=$(gr)
left join `contract_balance`    cb on cb.cid=c.id

where
        /*1 фильтр удалённых договоров */
        (c.del=0) and

        /*2 фильтр группы */
        (`c`.`gr`&(1<<$(gr))>0) and

        /*3 фильтр берём только действующие договора на указанный период спорно. */
        /* ((   month(c.date1)+12*year(c.date1)<($(month)+12*$(year))   ) or    (c.date1 is null)) and  ((      month(c.date2)+12*year(c.date2)>($(month)+12*$(year))   ) or    (c.date2 is null)) */

        /*4 указатель на месяц последних расчов баланса */
        ((cb.yy*12) + cb.mm) = (select max((cb2.yy*12) + cb2.mm)
            from contract_balance cb2
            where (cb2.cid=c.id) and ((cb2.yy*12) + cb2.mm) <= (($(year)*12) + $(month)) and ((cb2.summa1+cb2.summa2-cb2.summa3-cb2.summa4) is not null)
        )

        /*5 фильтр на дебеторов. проверить логику так как таким образом что-то не работало. */
        /*((cb.summa1+cb.summa2-cb.summa3-cb.summa4)<0)*/
        /*and (if ( ((cb.yy*12) + cb.mm)=(($(year)*12) + $(month)), (cb.summa1+cb.summa2-cb.summa3-cb.summa4)<0, cb.summa1<0))*/

group by c.id

