package com.sportos.watch.core.export

import android.content.Context
import com.sportos.watch.core.security.PrivacyZoneFilter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val timeMs: Long,
    val heartRate: Int?,
    val cadence: Int?
)

class GpxExporter(private val context: Context) {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Generates a valid GPX 1.1 XML file containing the given track points.
     * Optionally filters points using PrivacyZoneFilter to obscure sensitive locations.
     * Optionally includes Garmin TrackPointExtension for Heart Rate and Cadence.
     */
    fun exportToGpx(
        sessionName: String,
        points: List<TrackPoint>,
        privacyFilter: PrivacyZoneFilter? = null
    ): File {
        val effectivePoints = if (privacyFilter != null) {
            points.filterNot { privacyFilter.isInsidePrivacyZone(it.latitude, it.longitude) }
        } else {
            points
        }
        val fileName = "SportOS_${sessionName.replace(" ", "_")}_${System.currentTimeMillis()}.gpx"
        val file = File(context.getExternalFilesDir(null), fileName)

        FileOutputStream(file).bufferedWriter().use { writer ->
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.write("<gpx version=\"1.1\" creator=\"SportOS Wear\" ")
            writer.write("xmlns=\"http://www.topografix.com/GPX/1/1\" ")
            writer.write("xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\">\n")
            
            writer.write("  <metadata>\n")
            writer.write("    <time>${timeFormat.format(Date(System.currentTimeMillis()))}</time>\n")
            writer.write("  </metadata>\n")
            
            writer.write("  <trk>\n")
            writer.write("    <name>$sessionName</name>\n")
            writer.write("    <trkseg>\n")

            effectivePoints.forEach { pt ->
                writer.write("      <trkpt lat=\"${pt.latitude}\" lon=\"${pt.longitude}\">\n")
                pt.elevationMeters?.let {
                    writer.write("        <ele>$it</ele>\n")
                }
                writer.write("        <time>${timeFormat.format(Date(pt.timeMs))}</time>\n")

                if (pt.heartRate != null || pt.cadence != null) {
                    writer.write("        <extensions>\n")
                    writer.write("          <gpxtpx:TrackPointExtension>\n")
                    pt.heartRate?.let {
                        writer.write("            <gpxtpx:hr>$it</gpxtpx:hr>\n")
                    }
                    pt.cadence?.let {
                        writer.write("            <gpxtpx:cad>$it</gpxtpx:cad>\n")
                    }
                    writer.write("          </gpxtpx:TrackPointExtension>\n")
                    writer.write("        </extensions>\n")
                }
                writer.write("      </trkpt>\n")
            }

            writer.write("    </trkseg>\n")
            writer.write("  </trk>\n")
            writer.write("</gpx>\n")
        }

        return file
    }
}
