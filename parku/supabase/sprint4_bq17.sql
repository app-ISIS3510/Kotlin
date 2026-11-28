-- Sprint 4 · BQ17 (Tipo 5, mezcla 2 y 4): en los ultimos 7 dias, que parqueaderos tuvieron
-- la sesion completada mas larga y el mayor costo promedio por sesion.
-- No necesita eventos nuevos: usa parking_sessions y el precio por minuto de cada parqueadero.
-- Los minutos se calculan igual que en la app (minutos enteros, minimo 1).
-- Ejecutar una vez en el SQL Editor de Supabase. No toca nada existente.

create or replace view public.bq17_session_duration_cost_last_7_days as
with sessions as (
  select
    s.parking_id,
    greatest(1, floor(extract(epoch from (s.ended_at - s.started_at)) / 60))::int as minutes,
    p.name as parking_name,
    p.price_per_minute
  from public.parking_sessions s
  join public.parking_lots p on p.id = s.parking_id
  where s.status = 'completed'
    and s.ended_at is not null
    and s.ended_at >= now() - interval '7 days'
)
select
  parking_id,
  parking_name,
  count(*)::int as completed_sessions,
  round(avg(minutes))::int as avg_minutes,
  max(minutes)::int as longest_minutes,
  round(avg(minutes * price_per_minute))::float8 as avg_cost
from sessions
group by parking_id, parking_name
order by avg_minutes desc, avg_cost desc;

-- La app la lee como authenticated (copia offline del dashboard).
grant select on public.bq17_session_duration_cost_last_7_days to authenticated;

-- Looker Studio se conecta con parku_bi_reader.
grant select on public.bq17_session_duration_cost_last_7_days to parku_bi_reader;
