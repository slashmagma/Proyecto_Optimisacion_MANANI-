package com.Fenix.manami

import kotlinx.serialization.Serializable

@Serializable
data class Pedido(
    val id: Int? = null,
    val Cliente: String,
)