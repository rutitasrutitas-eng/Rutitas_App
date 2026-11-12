package mx.edu.uttt.rutitas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import mx.edu.uttt.rutitas.BuildConfig
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.model.Route
import mx.edu.uttt.rutitas.model.RouteStatus
import mx.edu.uttt.rutitas.ui.theme.RColors

/** Posiciones relativas (0..1) para el mapa DEMO. */
object MapPlaces {
    private val rel = mapOf(
        "UTTT" to Offset(0.80f, 0.16f),
        "Coyote" to Offset(0.14f, 0.76f),
        "Tlahuelilpan" to Offset(0.50f, 0.88f),
        "El Carmen" to Offset(0.16f, 0.30f),
        "Tula Centro" to Offset(0.46f, 0.55f),
        "Atitalaquia" to Offset(0.58f, 0.30f),
        "Plaza Jardines" to Offset(0.40f, 0.62f)
    )
    fun rel(name: String): Offset? = rel.entries.firstOrNull { name.contains(it.key, true) }?.value

    /** Posición del usuario en el mapa demo: cerca de la zona elegida. */
    fun userRel(zone: String): Offset? = rel(zone)?.let {
        Offset((it.x + 0.05f).coerceAtMost(0.96f), (it.y - 0.06f).coerceAtLeast(0.04f))
    }

    /** Punto de encuentro más cercano a la ubicación del usuario. */
    fun nearestPoint(points: List<String>, zone: String): String? {
        val u = userRel(zone) ?: return null
        return points.mapNotNull { n -> rel(n)?.let { n to (it - u).getDistance() } }.minByOrNull { it.second }?.first
    }
}

/** Coordenadas APROXIMADAS de demostración para Google Maps. Ajústalas con tus puntos reales. */
object GeoPlaces {
    private val geo = mapOf(
        "UTTT" to LatLng(20.0786, -99.3334),
        "Coyote" to LatLng(20.0420, -99.3900),
        "Tlahuelilpan" to LatLng(20.1130, -99.2260),
        "El Carmen" to LatLng(20.1000, -99.3900),
        "Tula Centro" to LatLng(20.0527, -99.3436),
        "Atitalaquia" to LatLng(20.0560, -99.2220),
        "Plaza Jardines" to LatLng(20.0560, -99.3500)
    )
    fun find(name: String): LatLng? = geo.entries.firstOrNull { name.contains(it.key, true) }?.value

    /** Zona demo más cercana a unas coordenadas reales (GPS). */
    fun nearestZone(lat: Double, lng: Double): String? = DemoData.zones.mapNotNull { z ->
        geo.entries.firstOrNull { z.contains(it.key, true) }?.value?.let { p ->
            val dLat = p.latitude - lat
            val dLng = (p.longitude - lng) * Math.cos(Math.toRadians(lat))
            z to (dLat * dLat + dLng * dLng)
        }
    }.minByOrNull { it.second }?.first
}

private fun pointAlong(pts: List<Offset>, t: Float): Offset {
    if (pts.size < 2) return pts.firstOrNull() ?: Offset.Zero
    val segs = pts.zipWithNext { a, b -> (b - a).getDistance() }
    var d = segs.sum() * t.coerceIn(0f, 1f)
    for (i in segs.indices) {
        if (d <= segs[i] || i == segs.lastIndex) {
            val f = if (segs[i] == 0f) 0f else (d / segs[i]).coerceIn(0f, 1f)
            return pts[i] + (pts[i + 1] - pts[i]) * f
        }
        d -= segs[i]
    }
    return pts.last()
}

@Composable
fun DemoMap(
    routes: List<Route>, modifier: Modifier = Modifier,
    highlight: Long? = null, driverProgress: Float? = null,
    meetingPoints: List<String> = emptyList(), selectedPoint: String? = null, userZone: String? = null
) {
    val measurer = rememberTextMeasurer()
    Box(modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFE6EDF7))) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            fun p(o: Offset) = Offset(o.x * w, o.y * h)

            for (i in 1..5) drawLine(Color.White, Offset(0f, h * i / 6f), Offset(w, h * i / 6f), strokeWidth = 14f)
            for (i in 1..4) drawLine(Color.White, Offset(w * i / 5f, 0f), Offset(w * i / 5f, h), strokeWidth = 14f)
            drawRoundRect(Color(0xFFD3E2F5), Offset(w * 0.64f, h * 0.52f), Size(w * 0.26f, h * 0.30f), CornerRadius(28f))

            routes.forEach { r ->
                val rel = r.stops.mapNotNull { MapPlaces.rel(it) }
                if (rel.size >= 2) {
                    val pts = rel.map { p(it) }
                    val path = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        pts.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    val isH = highlight == null || highlight == r.id
                    val color = when {
                        r.status == RouteStatus.INTERRUPTED -> RColors.Coral
                        isH -> RColors.Blue
                        else -> Color(0xFF93B4F5)
                    }
                    drawPath(path, color, style = Stroke(width = if (isH) 10f else 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            val origins = if (meetingPoints.isEmpty()) routes.map { it.origin }.distinct() else emptyList()
            origins.forEach { name ->
                MapPlaces.rel(name)?.let { o ->
                    val c = p(o)
                    drawCircle(Color.White, 16f, c)
                    drawCircle(RColors.Blue, 10f, c)
                    drawText(measurer, name, Offset(c.x + 20f, c.y - 16f), TextStyle(fontSize = 11.sp, color = RColors.Text, fontWeight = FontWeight.SemiBold))
                }
            }
            MapPlaces.rel("UTTT")?.let { o ->
                val c = p(o)
                drawCircle(Color.White, 22f, c)
                drawCircle(RColors.Coral, 15f, c)
                drawText(measurer, "UTTT", Offset(c.x - 56f, c.y + 24f), TextStyle(fontSize = 12.sp, color = RColors.DarkBlue, fontWeight = FontWeight.Bold))
            }
            // Puntos de encuentro numerados
            meetingPoints.forEachIndexed { i, name ->
                MapPlaces.rel(name)?.let { o ->
                    val c = p(o)
                    val sel = name == selectedPoint
                    drawCircle(Color.White, 26f, c)
                    drawCircle(if (sel) RColors.Coral else RColors.Yellow, 19f, c)
                    drawText(measurer, "${i + 1}", Offset(c.x - 7f, c.y - 14f), TextStyle(fontSize = 13.sp, color = RColors.Text, fontWeight = FontWeight.Bold))
                    drawText(measurer, name, Offset(c.x + 30f, c.y - 14f), TextStyle(fontSize = 11.sp, color = RColors.Text, fontWeight = FontWeight.SemiBold))
                }
            }
            // Ubicación del usuario y línea al punto más cercano
            userZone?.let { z ->
                MapPlaces.userRel(z)?.let { u ->
                    val c = p(u)
                    MapPlaces.nearestPoint(meetingPoints, z)?.let { near ->
                        MapPlaces.rel(near)?.let { n ->
                            drawLine(RColors.DarkBlue, c, p(n), strokeWidth = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f)))
                        }
                    }
                    drawCircle(Color.White, 22f, c)
                    drawCircle(RColors.DarkBlue, 14f, c)
                    drawText(measurer, "Tú", Offset(c.x + 26f, c.y + 6f), TextStyle(fontSize = 12.sp, color = RColors.DarkBlue, fontWeight = FontWeight.Bold))
                }
            }
            if (driverProgress != null) {
                val target = routes.firstOrNull { it.id == highlight } ?: routes.firstOrNull()
                val rel = target?.stops?.mapNotNull { MapPlaces.rel(it) }.orEmpty()
                if (rel.size >= 2) {
                    val c = p(pointAlong(rel, driverProgress))
                    drawCircle(RColors.DarkBlue, 22f, c)
                    drawCircle(RColors.Yellow, 16f, c)
                }
            }
        }
        Surface(
            Modifier.align(Alignment.TopEnd).padding(10.dp),
            color = Color.White.copy(alpha = 0.92f), shape = RoundedCornerShape(50)
        ) { Text("Mapa demo", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, color = RColors.DarkBlue) }
    }
}

/** Usa Google Maps si hay MAPS_API_KEY; si no, muestra el mapa DEMO. Nunca bloquea la app. */
@Composable
fun RutitasMap(
    routes: List<Route>, modifier: Modifier = Modifier,
    highlight: Long? = null, driverProgress: Float? = null,
    meetingPoints: List<String> = emptyList(), selectedPoint: String? = null, userZone: String? = null
) {
    if (BuildConfig.MAPS_API_KEY.isBlank()) {
        DemoMap(routes, modifier, highlight, driverProgress, meetingPoints, selectedPoint, userZone)
    } else {
        val cam = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(20.07, -99.30), 11f) }
        Box(modifier.clip(RoundedCornerShape(20.dp))) {
            GoogleMap(Modifier.matchParentSize(), cameraPositionState = cam) {
                routes.forEach { r ->
                    val pts = r.stops.mapNotNull { GeoPlaces.find(it) }
                    if (pts.size >= 2) Polyline(points = pts, color = if (r.status == RouteStatus.INTERRUPTED) RColors.Coral else RColors.Blue, width = 8f)
                    GeoPlaces.find(r.origin)?.let { Marker(state = MarkerState(it), title = r.origin) }
                }
                GeoPlaces.find("UTTT")?.let { Marker(state = MarkerState(it), title = "UTTT") }
                meetingPoints.forEachIndexed { i, name ->
                    GeoPlaces.find(name)?.let { Marker(state = MarkerState(it), title = "${i + 1}. $name") }
                }
                userZone?.let { z ->
                    GeoPlaces.find(z)?.let { Marker(state = MarkerState(LatLng(it.latitude + 0.003, it.longitude + 0.003)), title = "Tu ubicación") }
                }
                if (driverProgress != null) {
                    val target = routes.firstOrNull { it.id == highlight } ?: routes.firstOrNull()
                    val pts = target?.stops?.mapNotNull { GeoPlaces.find(it) }.orEmpty()
                    if (pts.size >= 2) {
                        val pos = driverProgress.coerceIn(0f, 1f) * (pts.size - 1)
                        val i = pos.toInt().coerceAtMost(pts.size - 2)
                        val f = (pos - i).toDouble()
                        val a = pts[i]; val b = pts[i + 1]
                        Marker(
                            state = MarkerState(LatLng(a.latitude + (b.latitude - a.latitude) * f, a.longitude + (b.longitude - a.longitude) * f)),
                            title = "Conductor"
                        )
                    }
                }
            }
        }
    }
}
