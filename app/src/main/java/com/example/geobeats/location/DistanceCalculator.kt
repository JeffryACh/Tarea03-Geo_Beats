package com.example.geobeats.location

import kotlin.math.*

/**
 * Utilidad matemática para cálculos geográficos.
 * Se utiliza un 'object' para acceder a los métodos de forma directa (estática).
 */
object DistanceCalculator {

    /**
     * Calcula la distancia en metros entre dos coordenadas usando la fórmula de Haversine.
     * Es ideal para distancias cortas (geocercas) ya que considera la curvatura de la Tierra.
     */
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radiusOfEarth = 6371000.0 // Radio de la Tierra en metros

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radiusOfEarth * c
    }
}