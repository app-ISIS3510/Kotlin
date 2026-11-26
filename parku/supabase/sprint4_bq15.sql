-- Sprint 4 · BQ15: desde que punto de entrada (mapa, lista, busqueda, favoritos o
-- comparacion) se abre el detalle de un parqueadero, y que porcentaje de esas
-- aperturas termina en un parqueo iniciado.
-- Ejecutar una vez en el SQL Editor de Supabase. No toca nada existente.

create or replace view public.bq15_entry_point_conversion_last_7_days as
with events as (
  select
    event_type,
    metadata->>'entry_point' as entry_point,
    -- Los eventos que estuvieron en la cola traen su hora real en metadata.
    coalesce((metadata->>'occurred_at')::timestamptz, created_at) as occurred_at
  from public.analytics_events
  where event_type in ('parking_detail_viewed', 'parking_started')
    and metadata->>'entry_point' is not null
),
recent as (
  select *
  from events
  where occurred_at >= now() - interval '7 days'
)
select
  entry_point,
  count(*) filter (where event_type = 'parking_detail_viewed')::int as details_opened,
  count(*) filter (where event_type = 'parking_started')::int as parking_starts,
  coalesce(
    round(
      100.0 * count(*) filter (where event_type = 'parking_started')
        / nullif(count(*) filter (where event_type = 'parking_detail_viewed'), 0),
      1
    ),
    0
  )::float8 as conversion_pct
from recent
group by entry_point
order by parking_starts desc, conversion_pct desc;

-- La app la lee como authenticated (copia offline del dashboard).
grant select on public.bq15_entry_point_conversion_last_7_days to authenticated;

-- Looker Studio se conecta con parku_bi_reader, no con authenticated: sin este
-- grant el informe no ve la vista.
grant select on public.bq15_entry_point_conversion_last_7_days to parku_bi_reader;
