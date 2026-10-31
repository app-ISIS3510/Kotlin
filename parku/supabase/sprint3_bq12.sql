-- Sprint 3 · BQ12: parqueaderos con mas solicitudes de navegacion (Waze / Google Maps)
-- en los ultimos 7 dias y que porcentaje termina en un parqueo iniciado en ese mismo
-- parqueadero en las 2 horas siguientes.
-- Ejecutar una vez en el SQL Editor de Supabase. No toca nada existente.

create or replace view public.bq12_navigation_conversion_last_7_days as
with events as (
  select
    user_id::text as user_id,
    parking_id::text as parking_id,
    event_type,
    screen,
    -- Los eventos que estuvieron en la cola traen su hora real en metadata.
    coalesce((metadata->>'occurred_at')::timestamptz, created_at) as occurred_at
  from public.analytics_events
  where event_type in ('navigation_opened', 'parking_started')
    and parking_id is not null
),
navigation as (
  select *
  from events
  where event_type = 'navigation_opened'
    and screen = 'parking_detail'
    and occurred_at >= now() - interval '7 days'
),
requests as (
  select
    n.parking_id,
    exists (
      select 1
      from events s
      where s.event_type = 'parking_started'
        and s.user_id = n.user_id
        and s.parking_id = n.parking_id
        and s.occurred_at between n.occurred_at and n.occurred_at + interval '2 hours'
    ) as converted
  from navigation n
)
select
  p.name as parking_name,
  count(*)::int as navigation_requests,
  count(*) filter (where r.converted)::int as parking_starts,
  round(100.0 * count(*) filter (where r.converted) / count(*), 1)::float8 as conversion_pct
from requests r
join public.parking_lots p on p.id::text = r.parking_id
group by p.name
order by navigation_requests desc, conversion_pct desc
limit 10;

grant select on public.bq12_navigation_conversion_last_7_days to authenticated;
