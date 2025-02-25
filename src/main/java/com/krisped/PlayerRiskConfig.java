package com.krisped;

import net.runelite.client.config.*;
import java.awt.Color;

@ConfigGroup("playerRisk")
public interface PlayerRiskConfig extends Config
{
    // -- NYE FELTER (Utenfor seksjoner, for hotkey-aktivering) --
    @ConfigItem(
            keyName = "useHotkeyActivation",
            name = "Use Hotkey Activation",
            description = "If enabled, the entire plugin is only active while the selected hotkey is held.",
            position = -2
    )
    default boolean useHotkeyActivation() { return false; }

    @ConfigItem(
            keyName = "activationHotkey",
            name = "Activation Hotkey",
            description = "Choose which hotkey (ALT, CTRL, SHIFT) to hold for activation if 'Use Hotkey Activation' is on.",
            position = -1
    )
    default HotkeyOption activationHotkey() { return HotkeyOption.ALT; }

    // Valgbare hotkeys
    enum HotkeyOption
    {
        SHIFT,
        CTRL,
        ALT
    }

    // --- Risk Values (med nye minimumsgrenser) ---
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

    @ConfigItem(
            keyName = "enableMediumRisk",
            name = "Enable Medium Risk",
            description = "Enable highlighting for medium risk players (>100,000 GP).",
            position = 4,
            section = riskValuesSection
    )
    default boolean enableMediumRisk() { return false; }

    @Range(min = 50000)
    @ConfigItem(
            keyName = "mediumRiskGP",
            name = "Medium Risk GP",
            description = "Minimum GP value for a player to be considered medium risk (cannot be lower than 50,000).",
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

    @ConfigItem(
            keyName = "enableHighRisk",
            name = "Enable High Risk",
            description = "Enable highlighting for high risk players (>1,000,000 GP).",
            position = 7,
            section = riskValuesSection
    )
    default boolean enableHighRisk() { return false; }

    @Range(min = 100000)
    @ConfigItem(
            keyName = "highRiskGP",
            name = "High Risk GP",
            description = "Minimum GP value for a player to be considered high risk (cannot be lower than 100,000).",
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

    @ConfigItem(
            keyName = "enableInsaneRisk",
            name = "Enable Insane Risk",
            description = "Enable highlighting for insane risk players (>10,000,000 GP).",
            position = 10,
            section = riskValuesSection
    )
    default boolean enableInsaneRisk() { return false; }

    @Range(min = 1000000)
    @ConfigItem(
            keyName = "insaneRiskGP",
            name = "Insane Risk GP",
            description = "Minimum GP value for a player to be considered insane risk (cannot be lower than 1,000,000).",
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
            description = "Configure in-game highlighting: Risk Text, Show Playernames, Hull, Outline, Tile, Own Player, Minimap Display Mode.",
            position = 1,
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
            keyName = "showPlayernames",
            name = "Show Playernames",
            description = "Display the player's name before the risk text.",
            position = 2,
            section = highlightSection
    )
    default boolean showPlayernames() { return false; }

    @ConfigItem(
            keyName = "outlineThickness",
            name = "Outline Thickness",
            description = "Thickness of the outline drawn around players.",
            position = 3,
            section = highlightSection
    )
    default int outlineThickness() { return 2; }

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
            keyName = "minimapDisplayMode",
            name = "Minimap Display Mode",
            description = "Select how risk is displayed on the minimap.",
            position = 8,
            section = highlightSection
    )
    default MinimapDisplayMode minimapDisplayMode() { return MinimapDisplayMode.NONE; }

    // --- Defence Text Options ---
    @ConfigItem(
            keyName = "defenceText",
            name = "Show Defence lvl",
            description = "Viser defence level til spiller. Valg: Disabled, Over, Centre, Under",
            position = 9,
            section = highlightSection
    )
    default DefenceTextPosition defenceText() { return DefenceTextPosition.DISABLED; }

    enum DefenceTextPosition
    {
        DISABLED("Disabled"),
        OVER("Over"),
        CENTRE("Centre"),
        UNDER("Under");

        private final String displayName;
        DefenceTextPosition(String displayName)
        {
            this.displayName = displayName;
        }
        @Override
        public String toString()
        {
            return displayName;
        }
    }

    // --- Filter Options ---
    @ConfigSection(
            name = "Filter Options",
            description = "Configure filters for which players to highlight.",
            position = 2,
            closedByDefault = true
    )
    String filterSection = "filterSection";

    @ConfigItem(
            keyName = "pvpMode",
            name = "PvP Mode",
            description = "Select which players to highlight:\n• DISABLED: All players\n• ON: Only players in PvP worlds/Wilderness\n• ATTACKABLE: Only players you can attack based on combat level",
            position = 1,
            section = filterSection
    )
    default PvPMode pvpMode() { return PvPMode.DISABLED; }

    @ConfigItem(
            keyName = "skullMode",
            name = "Skull Mode",
            description = "Choose which players to highlight based on skull status: Unskulled, Skulled or All. (Only active if PvP Mode is ON or ATTACKABLE.)",
            position = 2,
            section = filterSection
    )
    default SkullMode skullMode() { return SkullMode.ALL; }

    // --- Overlay Options ---
    @ConfigSection(
            name = "Overlay Options",
            description = "Configure overlay displays such as risk summary.",
            position = 3,
            closedByDefault = true
    )
    String overlaySection = "overlaySection";

    @ConfigItem(
            keyName = "showOverlay",
            name = "Show Risk Overlay",
            description = "Toggle the display of the risk summary overlay.",
            position = 1,
            section = overlaySection
    )
    default boolean showOverlay() { return true; }

    @ConfigItem(
            keyName = "overlayDisplayType",
            name = "Overlay Display Type",
            description = "Select how the overlay displays risk:\n• Disabled: Do not display overlay\n• Risk Categories: Show category names (Low, Medium, High, Insane)\n• Risk Amounts: Show threshold amounts",
            position = 2,
            section = overlaySection
    )
    default OverlayDisplayType overlayDisplayType() { return OverlayDisplayType.RISK_CATEGORIES; }

    // --- Combat Options ---
    @ConfigSection(
            name = "Combat Options",
            description = "Configure combat-related highlighting settings.",
            position = 4,
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
            position = 5,
            closedByDefault = true
    )
    String riskMenuSection = "riskMenuSection";

    @ConfigItem(
            keyName = "holdShift",
            name = "Hold Shift",
            description = "Angir om du må holde shift for å aktivere Risk Check",
            position = 1,
            section = riskMenuSection
    )
    default boolean holdShift() { return false; }

    @ConfigItem(
            keyName = "riskMenuColor",
            name = "Risk Menu Color",
            description = "Choose a color for the Risk Check menu option.",
            position = 2,
            section = riskMenuSection
    )
    default Color riskMenuColor() { return new Color(0xFF, 0xA5, 0x00, 0xFF); }

    @ConfigItem(
            keyName = "riskMenuAction",
            name = "Risk Menu Action",
            description = "Choose what happens when you click Risk Check: Chat, Side Panel, or All.",
            position = 3,
            section = riskMenuSection
    )
    default RiskMenuAction riskMenuAction() { return RiskMenuAction.CHAT; }

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
        DISABLED,
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

    enum SkullMode
    {
        UNSKULLED("Unskulled"),
        SKULLED("Skulled"),
        ALL("All");

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

    enum RiskMenuAction
    {
        CHAT("Chat"),
        SIDE_PANEL("Side Panel"),
        ALL("All");

        private final String displayName;
        RiskMenuAction(String displayName)
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
