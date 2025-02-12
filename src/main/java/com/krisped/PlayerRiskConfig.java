package com.krisped;

import net.runelite.client.config.*;

import java.awt.Color;

@ConfigGroup("playerRisk")
public interface PlayerRiskConfig extends Config
{
    @ConfigItem(
            keyName = "lowRiskThreshold",
            name = "Low Value Price",
            description = "Minimum GP value for a player to be considered above low risk",
            position = 1
    )
    default int lowRiskThreshold() { return 20_000; }

    @ConfigItem(
            keyName = "mediumRiskThreshold",
            name = "Medium Value Price",
            description = "Minimum GP value for a player to be considered medium risk",
            position = 2
    )
    default int mediumRiskThreshold() { return 100_000; }

    @ConfigItem(
            keyName = "highRiskThreshold",
            name = "High Value Price",
            description = "Minimum GP value for a player to be considered high risk",
            position = 3
    )
    default int highRiskThreshold() { return 1_000_000; }

    @ConfigItem(
            keyName = "insaneRiskThreshold",
            name = "Insane Value Price",
            description = "Minimum GP value for a player to be considered insane risk",
            position = 4
    )
    default int insaneRiskThreshold() { return 10_000_000; }

    @ConfigItem(
            keyName = "outlineThickness",
            name = "Outline Thickness",
            description = "Thickness of the outline around risky players",
            position = 5
    )
    default int outlineThickness() { return 2; }

    @ConfigItem(
            keyName = "textPosition",
            name = "Text Position",
            description = "Where to display the risk text",
            position = 6
    )
    default TextPosition textPosition() { return TextPosition.ABOVE; }

    enum TextPosition
    {
        ABOVE,
        MIDDLE,
        BELOW
    }
}
