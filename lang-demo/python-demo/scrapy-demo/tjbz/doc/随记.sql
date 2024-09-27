-- 查询节点路径
with recursive path_cte as (
select name, code, parent_code, code as path
from tjbz
where parent_code is null
union all
select t.name, t.code, t.parent_code, p.path || '>' || t.code as path
from tjbz t
join path_cte p on t.parent_code = p.code)
select *
from path_cte;