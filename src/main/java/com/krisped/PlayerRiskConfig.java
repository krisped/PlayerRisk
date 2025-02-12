package com.krisped;

import net.runelite.client.config.*;
import java.awt.Color;

@ConfigGroup("playerRisk")
public interface PlayerRiskConfig extends Config
{
    @ConfigItem(
            keyName = "lowValueRisk",
            name = "Low Value Risk",
            description = "Minimum GP-verdi for at en spiller skal regnes som lav risiko",
            position = 1
    )
    default int lowValueRisk() { return 20_000; }

    @Alpha
    @ConfigItem(
            keyName = "lowValueColor",
            name = "Low Value Color",
            description = "Farge for spillere med lav risiko",
            position = 2
    )
    default Color lowValueColor() { return Color.BLUE; }

    @ConfigItem(
            keyName = "mediumValueRisk",
            name = "Medium Value Risk",
            description = "Minimum GP-verdi for at en spiller skal regnes som medium risiko",
            position = 3
    )
    default int mediumValueRisk() { return 100_000; }

    @Alpha
    @ConfigItem(
            keyName = "mediumValueColor",
            name = "Medium Value Color",
            description = "Farge for spillere med medium risiko",
            position = 4
    )
    default Color mediumValueColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "highValueRisk",
            name = "High Value Risk",
            description = "Minimum GP-verdi for at en spiller skal regnes som høy risiko",
            position = 5
    )
    default int highValueRisk() { return 1_000_000; }

    @Alpha
    @ConfigItem(
            keyName = "highValueColor",
            name = "High Value Color",
            description = "Farge for spillere med høy risiko",
            position = 6
    )
    default Color highValueColor() { return Color.ORANGE; }

    @ConfigItem(
            keyName = "insaneValueRisk",
            name = "Insane Value Risk",
            description = "Minimum GP-verdi for at en spiller skal regnes som insane risiko",
            position = 7
    )
    default int insaneValueRisk() { return 10_000_000; }

    @Alpha
    @ConfigItem(
            keyName = "insaneValueColor",
            name = "Insane Value Color",
            description = "Farge for spillere med insane risiko",
            position = 8
    )
    default Color insaneValueColor() { return Color.PINK; }

    @ConfigItem(
            keyName = "outlineThickness",
            name = "Outline Thickness",
            description = "Tykkelse på omrisset rundt spillere",
            position = 9
    )
    default int outlineThickness() { return 2; }

    @ConfigItem(
            keyName = "textPosition",
            name = "Text Position",
            description = "Hvor risikoteksten skal vises",
            position = 10
    )
    default TextPosition textPosition() { return TextPosition.ABOVE; }

    enum TextPosition
    {
        NONE,
        ABOVE,
        MIDDLE,
        BELOW
    }
}
