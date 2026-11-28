-- Sprint 4 · BQ18 (Tipo 1): en los ultimos 7 dias, que accion del parqueo (iniciar,
-- cambiar la hora de recogida o terminar) fallo mas veces, que porcentaje de sus
-- intentos fallo y cuantas de esas fallas fueron por perdida de conectividad.
-- La app envia un evento 'parking_action_result' por intento, con metadata:
--   action          start_parking | change_pickup_time | end_parking
--   outcome         success | failure
--   failure_reason  connectivity | other   (solo en las fallas)
-- Ejecutar una vez en el SQL Editor de Supabase. No toca nada existente.

create or replace view public.bq18_parking_action_failures_last_7_days as
with actions(action) as (
  -- Las tres acciones salen siempre, aunque no tengan intentos en la semana.
  values ('start_parking'), ('change_pickup_time'), ('end_parking')
),
events as (
  select
    metadata->>'action' as action,
    metadata->>'outcome' as outcome,
    metadata->>'failure_reason' as failure_reason,
    -- Los eventos que estuvieron en la cola offline traen su hora real en metadata.
    coalesce((metadata->>'occurred_at')::timestamptz, created_at) as occurred_at
  from public.analytics_events
  where event_type = 'parking_action_result'
),
recent as (
  select *
  from events
  where occurred_at >= now() - interval '7 days'
),
totals as (
  select
    a.action,
    count(r.action)::int as attempts,
    count(r.action) filter (where r.outcome = 'failure')::int as failures,
    count(r.action) filter (
      where r.outcome = 'failure' and r.failure_reason = 'connectivity'
    )::int as connectivity_failures
  from actions a
  left join recent r on r.action = a.action
  group by a.action
)
select
  action,
  attempts,
  failures,
  connectivity_failures,
  (failures - connectivity_failures) as other_failures,
  coalesce(round(100.0 * failures / nullif(attempts, 0), 1), 0)::float8 as failure_pct,
  coalesce(round(100.0 * connectivity_failures / nullif(failures, 0), 1), 0)::float8
    as connectivity_share_pct
from totals
order by failures desc, failure_pct desc, action;

-- La app la lee como authenticated (copia offline del dashboard).
grant select on public.bq18_parking_action_failures_last_7_days to authenticated;

-- Looker Studio se conecta con parku_bi_reader.
grant select on public.bq18_parking_action_failures_last_7_days to parku_bi_reader;
