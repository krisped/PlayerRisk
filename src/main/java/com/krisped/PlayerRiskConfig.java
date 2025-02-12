package com.krisped;

import net.runelite.client.config.*;
import java.awt.Color;

@ConfigGroup("playerRisk")
public interface PlayerRiskConfig extends Config {

    // --- Risk Values ---
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
            description = "Enable highlighting for low risk players (>20,000 GP).",
            position = 1,
            section = riskValuesSection
    )
    default boolean enableLowRisk() { return false; }

    @ConfigItem(
            keyName = "lowRiskGP",
            name = "Low Risk GP",
            description = "Minimum GP value for a player to be considered low risk (must be >20,000).",
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
            description = "Enable highlighting for medium risk players (>100,000 GP).",
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
            description = "Enable highlighting for high risk players (>1,000,000 GP).",
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
            description = "Enable highlighting for insane risk players (>10,000,000 GP).",
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

    // --- Display Settings ---
    @ConfigSection(
            name = "Display Settings",
            description = "Configure overlay display options.",
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
            description = "Select text position relative to the player model:\n• OVER: Over head\n• CENTER: Center of model\n• UNDER: Below model\n• DISABLED: No text",
            position = 2,
            section = displaySettingsSection
    )
    default TextPosition textPosition() { return TextPosition.OVER; }

    @ConfigItem(
            keyName = "pvpMode",
            name = "PvP Mode",
            description = "Select which players to highlight:\n• OFF: All players\n• ON: Only players in PvP worlds/Wilderness\n• ATTACKABLE: Only players you can attack based on combat level",
            position = 3,
            section = displaySettingsSection
    )
    default PvPMode pvpMode() { return PvPMode.OFF; }

    @ConfigItem(
            keyName = "minimapDisplayMode",
            name = "Minimap Display Mode",
            description = "Select how risk is displayed on the minimap:\n• NONE: Do not display\n• DOT: Show a colored dot\n• RISK: Display risk value with player name",
            position = 4,
            section = displaySettingsSection
    )
    default MinimapDisplayMode minimapDisplayMode() { return MinimapDisplayMode.NONE; }

    @ConfigItem(
            keyName = "enableTile",
            name = "Enable Tile",
            description = "Draw a tile overlay (using player's canvas tile poly) around the player (contour only).",
            position = 5,
            section = displaySettingsSection
    )
    default boolean enableTile() { return false; }

    @ConfigItem(
            keyName = "enableOutline",
            name = "Enable Outline",
            description = "Draw an outline around the player.",
            position = 6,
            section = displaySettingsSection
    )
    default boolean enableOutline() { return true; }

    @ConfigItem(
            keyName = "enableHull",
            name = "Enable Hull",
            description = "Draw the convex hull (ring) of the player's model.",
            position = 7,
            section = displaySettingsSection
    )
    default boolean enableHull() { return false; }

    enum TextPosition {
        DISABLED("Disabled"),
        OVER("Over"),
        CENTER("Center"),
        UNDER("Under");

        private final String displayName;
        TextPosition(String displayName) { this.displayName = displayName; }
        @Override
        public String toString() { return displayName; }
    }

    enum PvPMode {
        OFF,
        ON,
        ATTACKABLE
    }

    enum MinimapDisplayMode {
        NONE("None"),
        DOT("Dot"),
        RISK("Risk");

        private final String displayName;
        MinimapDisplayMode(String displayName) { this.displayName = displayName; }
        @Override
        public String toString() { return displayName; }
    }
}
