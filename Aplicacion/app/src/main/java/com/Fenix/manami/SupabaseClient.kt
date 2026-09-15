package com.Fenix.manami

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://nzbmwrvvqvxkqumbfzvh.supabase.co",
        supabaseKey = "sb_publishable_MGf1znEsGpTshwv0bni5sA_T6JhgAfX"
    ) {
        install(Postgrest)
        install(Auth)
    }
}