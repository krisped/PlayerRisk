package com.krisped;

import net.runelite.client.config.*;
import java.awt.Color;

@ConfigGroup("playerRisk")
public interface PlayerRiskConfig extends Config
{
    // --- Risk Values Section ---
    @ConfigSection(
            name = "Risk Values",
            description = "Configure risk categories: enable, GP threshold, and color.",
            position = 0,
            closedByDefault = false
    )
    String riskValuesSection = "riskValuesSection";

    // Low Risk
    @ConfigItem(
            keyName = "enableLowRisk",
            name = "Enable Low Risk",
            description = "Enable highlighting for low risk players.",
            position = 1,
            section = riskValuesSection
    )
    default boolean enableLowRisk() { return false; }

    @ConfigItem(
            keyName = "lowRiskGP",
            name = "Low Risk GP",
            description = "Minimum GP value for a player to be considered low risk.",
            position = 2,
            section = riskValuesSection
    )
    default int lowRiskGP() { return 20000; }

    @Alpha
    @ConfigItem(
            keyName = "lowRiskColor",
            name = "Low Risk Color",
            description = "Color for low risk players.",
            position = 3,
            section = riskValuesSection
    )
    default Color lowRiskColor() { return Color.BLUE; }

    // Medium Risk
    @ConfigItem(
            keyName = "enableMediumRisk",
            name = "Enable Medium Risk",
            description = "Enable highlighting for medium risk players.",
            position = 4,
            section = riskValuesSection
    )
    default boolean enableMediumRisk() { return false; }

    @ConfigItem(
            keyName = "mediumRiskGP",
            name = "Medium Risk GP",
            description = "Minimum GP value for a player to be considered medium risk.",
            position = 5,
            section = riskValuesSection
    )
    default int mediumRiskGP() { return 100000; }

    @Alpha
    @ConfigItem(
            keyName = "mediumRiskColor",
            name = "Medium Risk Color",
            description = "Color for medium risk players.",
            position = 6,
            section = riskValuesSection
    )
    default Color mediumRiskColor() { return Color.GREEN; }

    // High Risk
    @ConfigItem(
            keyName = "enableHighRisk",
            name = "Enable High Risk",
            description = "Enable highlighting for high risk players.",
            position = 7,
            section = riskValuesSection
    )
    default boolean enableHighRisk() { return false; }

    @ConfigItem(
            keyName = "highRiskGP",
            name = "High Risk GP",
            description = "Minimum GP value for a player to be considered high risk.",
            position = 8,
            section = riskValuesSection
    )
    default int highRiskGP() { return 1000000; }

    @Alpha
    @ConfigItem(
            keyName = "highRiskColor",
            name = "High Risk Color",
            description = "Color for high risk players.",
            position = 9,
            section = riskValuesSection
    )
    default Color highRiskColor() { return Color.ORANGE; }

    // Insane Risk
    @ConfigItem(
            keyName = "enableInsaneRisk",
            name = "Enable Insane Risk",
            description = "Enable highlighting for insane risk players.",
            position = 10,
            section = riskValuesSection
    )
    default boolean enableInsaneRisk() { return false; }

    @ConfigItem(
            keyName = "insaneRiskGP",
            name = "Insane Risk GP",
            description = "Minimum GP value for a player to be considered insane risk.",
            position = 11,
            section = riskValuesSection
    )
    default int insaneRiskGP() { return 10000000; }

    @Alpha
    @ConfigItem(
            keyName = "insaneRiskColor",
            name = "Insane Risk Color",
            description = "Color for insane risk players.",
            position = 12,
            section = riskValuesSection
    )
    default Color insaneRiskColor() { return Color.PINK; }

    // --- Display Settings Section ---
    @ConfigSection(
            name = "Display Settings",
            description = "Configure display options such as text position and PvP mode.",
            position = 1,
            closedByDefault = false
    )
    String displaySettingsSection = "displaySettingsSection";

    @ConfigItem(
            keyName = "outlineThickness",
            name = "Outline Thickness",
            description = "Thickness of the outline drawn around players.",
            position = 1,
            section = displaySettingsSection
    )
    default int outlineThickness() { return 2; }

    @ConfigItem(
            keyName = "textPosition",
            name = "Text Position",
            description = "Where to display the risk text.",
            position = 2,
            section = displaySettingsSection
    )
    default TextPosition textPosition() { return TextPosition.ABOVE; }

    @ConfigItem(
            keyName = "pvpMode",
            name = "PvP Mode",
            description = "Select which players to highlight:\n" +
                    "• OFF: All players in all worlds.\n" +
                    "• ON: Only players in PvP worlds (or Wilderness).\n" +
                    "• ATTACKABLE: Only players you can attack based on your combat level.",
            position = 3,
            section = displaySettingsSection
    )
    default PvPMode pvpMode() { return PvPMode.OFF; }

    enum TextPosition
    {
        NONE,
        ABOVE,
        MIDDLE,
        BELOW
    }

    enum PvPMode
    {
        OFF,
        ON,
        ATTACKABLE
    }
}
