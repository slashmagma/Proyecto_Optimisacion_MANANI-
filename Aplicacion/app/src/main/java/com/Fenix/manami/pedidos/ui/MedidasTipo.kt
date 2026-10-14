package com.Fenix.manami.pedidos.ui

sealed class MedidasTipo {
    data class Pantalon(val data: com.Fenix.manami.pedidos.data.PantalonModelo) : MedidasTipo()
    data class Polera(val data: com.Fenix.manami.pedidos.data.PoleraModelo) : MedidasTipo()
    data class BirreteEstola(val data: com.Fenix.manami.pedidos.data.BirreteEstolaModelo) : MedidasTipo()
}
