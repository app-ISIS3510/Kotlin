package com.example.parku.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Mismo proyecto de Supabase que usa la version en Flutter (ver su lib/main.dart).
 *
 * La clave es "publishable": esta pensada para ir en el cliente, igual que en
 * Flutter. Quien protege los datos son las politicas RLS del proyecto, no esta
 * clave. Aun asi, si mas adelante quieren sacarla del codigo, el sitio natural
 * es local.properties + BuildConfig.
 */
private const val SUPABASE_URL = "https://wwvkgpstphxeldscncyf.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_i7kN0azgQ2JYTD1bm33ZFA_c6mLTnjN"

val supabase: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY,
    ) {
        install(Auth)
        install(Postgrest)
    }
}
