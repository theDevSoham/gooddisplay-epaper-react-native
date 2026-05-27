package expo.modules.gooddisplayepaper.bridge

import expo.modules.gooddisplayepaper.models.ColorMode
import expo.modules.gooddisplayepaper.protocol.EpdConfigFactory

data class SupportedPanelRecord(
    val inchCode: Int,
    val label: String,
    val width: Int,
    val height: Int,
    val colorModes: List<Int>,
)

object PanelCatalog {

  const val PROTOCOL_VERSION = "1.0.0"

  private val PANELS =
      listOf(
          PanelSpec(97, "0.97\"", 88, 184, intArrayOf(2, 3, 4)),
          PanelSpec(153, "1.54\" (152×152)", 152, 152, intArrayOf(2, 3, 4)),
          PanelSpec(154, "1.54\" (200×200)", 200, 200, intArrayOf(2, 3, 4)),
          PanelSpec(213, "2.13\"", 128, 250, intArrayOf(2, 3, 4)),
          PanelSpec(266, "2.66\"", 152, 296, intArrayOf(2, 3, 4)),
          PanelSpec(267, "2.66\" HR", 184, 360, intArrayOf(4)),
          PanelSpec(270, "2.7\"", 176, 264, intArrayOf(2, 3, 4)),
          PanelSpec(290, "2.9\"", 128, 296, intArrayOf(2, 3, 4)),
          PanelSpec(291, "2.9\" HR", 168, 384, intArrayOf(4)),
          PanelSpec(370, "3.7\"", 240, 416, intArrayOf(2, 3, 4)),
          PanelSpec(420, "4.2\"", 400, 300, intArrayOf(2, 3)),
      )

  @JvmStatic
  fun supportedPanels(): List<SupportedPanelRecord> {
    return PANELS.map { panel ->
      val modes =
          panel.colorModes.filter { EpdConfigFactory.supports(it, panel.inchCode) }.toList()
      SupportedPanelRecord(
          inchCode = panel.inchCode,
          label = panel.label,
          width = panel.width,
          height = panel.height,
          colorModes = modes,
      )
    }
  }

  @JvmStatic
  fun colorModeFromString(value: String?): Int {
    return when (value?.lowercase()) {
      "mono", "bw", "2" -> ColorMode.MONO
      "tri", "bwr", "3" -> ColorMode.TRI
      "quad", "4g", "4" -> ColorMode.QUAD
      else -> throw IllegalArgumentException("Invalid colorMode: $value")
    }
  }

  private data class PanelSpec(
      val inchCode: Int,
      val label: String,
      val width: Int,
      val height: Int,
      val colorModes: IntArray,
  )
}
