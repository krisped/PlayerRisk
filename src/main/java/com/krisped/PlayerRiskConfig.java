package com.krisped;

import net.runelite.client.config.*;
import java.awt.Color;

@ConfigGroup("playerRisk")
public interface PlayerRiskConfig extends Config
{
    // --- Risk Values ---
    @ConfigSection(
            name = "Risk Values",
            description = "Configure risk categories: enable, GP threshold, and color.",
            position = 0,
            closedByDefault = true
    )
    String riskValuesSection = "riskValuesSection";

    @ConfigItem(
            keyName = "enableLowRisk",
            name = "Enable Low Risk",
            description = "Enable highlighting for low risk players (>20,000 GP).",
            position = 1,
            section = riskValuesSection
    )
    default boolean enableLowRisk() { return false; }

    @Range(min = 1000)
    @ConfigItem(
            keyName = "lowRiskGP",
            name = "Low Risk GP",
            description = "Minimum GP value for a player to be considered low risk (cannot be lower than 1000).",
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
    default Color lowRiskColor() { return new Color(0x66, 0xB2, 0xFF, 0xFF); }

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
    default Color mediumRiskColor() { return new Color(0x99, 0xFF, 0x99, 0xFF); }

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
    default Color highRiskColor() { return new Color(0xFF, 0x96, 0x00, 0xFF); }

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
    default Color insaneRiskColor() { return new Color(0xFF, 0x62, 0xB2, 0xFF); }

    // --- Highlight Options ---
    @ConfigSection(
            name = "Highlight Options",
            description = "Configure risk highlighting: text, outline, tile, hull, minimap, etc.",
            position = 13,
            closedByDefault = true
    )
    String highlightSection = "highlightSection";

    @ConfigItem(
            keyName = "riskText",
            name = "Risk Text",
            description = "Select risk text position relative to the player model.",
            position = 1,
            section = highlightSection
    )
    default TextPosition riskText() { return TextPosition.OVER; }

    @ConfigItem(
            keyName = "outlineThickness",
            name = "Outline Thickness",
            description = "Thickness of the outline drawn around players.",
            position = 2,
            section = highlightSection
    )
    default int outlineThickness() { return 2; }

    @ConfigItem(
            keyName = "minimapDisplayMode",
            name = "Minimap Display Mode",
            description = "Select how risk is displayed on the minimap.",
            position = 3,
            section = highlightSection
    )
    default MinimapDisplayMode minimapDisplayMode() { return MinimapDisplayMode.NONE; }

    @ConfigItem(
            keyName = "enableTile",
            name = "Enable Tile",
            description = "Draw a tile overlay around the player.",
            position = 4,
            section = highlightSection
    )
    default boolean enableTile() { return false; }

    @ConfigItem(
            keyName = "enableOutline",
            name = "Enable Outline",
            description = "Draw an outline around the player.",
            position = 5,
            section = highlightSection
    )
    default boolean enableOutline() { return true; }

    @ConfigItem(
            keyName = "enableHull",
            name = "Enable Hull",
            description = "Draw the convex hull (ring) of the player's model.",
            position = 6,
            section = highlightSection
    )
    default boolean enableHull() { return false; }

    @ConfigItem(
            keyName = "highlightLocalPlayer",
            name = "Highlight Own Player",
            description = "Include your own (local) player in risk highlighting and overlay.",
            position = 7,
            section = highlightSection
    )
    default boolean highlightLocalPlayer() { return false; }

    @ConfigItem(
            keyName = "pvpMode",
            name = "PvP Mode",
            description = "Select which players to highlight:\n• OFF: All players\n• ON: Only players in PvP worlds/Wilderness\n• ATTACKABLE: Only players you can attack based on combat level",
            position = 8,
            section = highlightSection
    )
    default PvPMode pvpMode() { return PvPMode.OFF; }

    @ConfigItem(
            keyName = "skullMode",
            name = "Skull Mode",
            description = "Velg hvilke spillere som skal highlighte: Unskulled, Skulled eller Both",
            position = 9,
            section = highlightSection
    )
    default SkullMode skullMode() { return SkullMode.BOTH; }

    @ConfigItem(
            keyName = "showOverlay",
            name = "Show Risk Overlay",
            description = "Toggle the display of the risk summary overlay",
            position = 10,
            section = highlightSection
    )
    default boolean showOverlay() { return true; }

    @ConfigItem(
            keyName = "overlayDisplayType",
            name = "Overlay Display Type",
            description = "Select how the overlay displays risk:\n• Disabled: Do not display overlay\n• Risk Categories: Show category names (Low, Medium, High, Insane)\n• Risk Amounts: Show threshold amounts",
            position = 11,
            section = highlightSection
    )
    default OverlayDisplayType overlayDisplayType() { return OverlayDisplayType.RISK_CATEGORIES; }

    // --- Combat Options ---
    @ConfigSection(
            name = "Combat Options",
            description = "Configure combat-related highlighting settings.",
            position = 14,
            closedByDefault = true
    )
    String combatSection = "combatSection";

    @ConfigItem(
            keyName = "disableHighlightInCombat",
            name = "Turn off in Combat",
            description = "Disable risk highlights while you are in combat.",
            position = 1,
            section = combatSection
    )
    default boolean disableHighlightInCombat() { return false; }

    @Range(min = 1, max = 60)
    @ConfigItem(
            keyName = "combatTimeout",
            name = "Combat Timeout (sec)",
            description = "Seconds after combat ends before risk highlights re-enable.",
            position = 2,
            section = combatSection
    )
    default int combatTimeout() { return 5; }

    // --- Risk Menu Options ---
    @ConfigSection(
            name = "Risk Menu Options",
            description = "Configure when and how the Risk Check menu appears.",
            position = 15,
            closedByDefault = true
    )
    String riskMenuSection = "riskMenuSection";

    @ConfigItem(
            keyName = "riskMenuMode",
            name = "Risk Menu Mode",
            description = "Choose when to show the Risk Check menu: Disabled, RightClick, Shift + RightClick.",
            position = 1,
            section = riskMenuSection
    )
    default RiskMenuMode riskMenuMode() { return RiskMenuMode.RIGHT_CLICK; }

    @Alpha
    @ConfigItem(
            keyName = "riskMenuColor",
            name = "Risk Menu Color",
            description = "Choose a color for the Risk Check menu option.",
            position = 2,
            section = riskMenuSection
    )
    default Color riskMenuColor() { return new Color(0xFF, 0xA5, 0x00, 0xFF); }

    // --- Enum Definitions ---
    enum TextPosition
    {
        DISABLED("Disabled"),
        OVER("Over"),
        CENTER("Center"),
        UNDER("Under");

        private final String displayName;
        TextPosition(String displayName)
        {
            this.displayName = displayName;
        }
        @Override
        public String toString()
        {
            return displayName;
        }
    }

    enum PvPMode
    {
        OFF,
        ON,
        ATTACKABLE
    }

    enum MinimapDisplayMode
    {
        NONE("None"),
        DOT("Dot"),
        RISK("Risk");

        private final String displayName;
        MinimapDisplayMode(String displayName)
        {
            this.displayName = displayName;
        }
        @Override
        public String toString()
        {
            return displayName;
        }
    }

    enum OverlayDisplayType
    {
        DISABLED("Disabled"),
        RISK_CATEGORIES("Risk Categories"),
        RISK_AMOUNTS("Risk Amounts");

        private final String displayName;
        OverlayDisplayType(String displayName)
        {
            this.displayName = displayName;
        }
        @Override
        public String toString()
        {
            return displayName;
        }
    }

    enum RiskMenuMode
    {
        DISABLED,
        RIGHT_CLICK,
        SHIFT_RIGHT_CLICK
    }

    // Ny enum for Skull Mode
    enum SkullMode
    {
        UNSKULLED("Unskulled"),
        SKULLED("Skulled"),
        BOTH("Both");

        private final String displayName;
        SkullMode(String displayName)
        {
            this.displayName = displayName;
        }
        @Override
        public String toString()
        {
            return displayName;
        }
    }
}
