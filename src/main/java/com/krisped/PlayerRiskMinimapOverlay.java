package com.krisped;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class PlayerRiskMinimapOverlay extends Overlay
{
    private final Client client;
    private final ItemManager itemManager;
    private final PlayerRiskConfig config;
    private final CombatManager combatManager;

    // Hvis du ønsker at plugin-en skal kunne aktivere/deaktivere overlay,
    // kan du styre det med setEnabled(...) i plugin-koden
    private boolean enabled = true;

    @Inject
    public PlayerRiskMinimapOverlay(Client client,
                                    ItemManager itemManager,
                                    PlayerRiskConfig config,
                                    CombatManager combatManager)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        this.combatManager = combatManager;

        // Konfigurer overlay-egenskaper
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!enabled || client.getLocalPlayer() == null)
        {
            return null;
        }

        // Dersom config sier "ikke highlight i combat" og du er i combat -> return null
        if (config.disableHighlightInCombat() && combatManager.isInCombat())
        {
            return null;
        }

        // Gå gjennom alle spillere
        for (Player player : client.getPlayers())
        {
            if (player == null || player.getName() == null)
            {
                continue;
            }

            // Hopp over local player hvis highlightLocalPlayer = false
            if (player.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer())
            {
                continue;
            }

            // Sjekk om PvP-filteret godkjenner denne spilleren
            if (!passesPvpFilter(player))
            {
                continue;
            }

            // Sjekk om skull-filteret godkjenner denne spilleren
            if (!passesSkullFilter(player))
            {
                continue;
            }

            // Finn spillerens totale risk (basert på gear)
            long totalRisk = RiskCalculator.calculateRisk(player, itemManager);

            // Finn en farge å tegne med (null hvis risk er for lavt)
            Color riskColor = getRiskColor(totalRisk);
            if (riskColor == null)
            {
                continue;
            }

            // Formatér risk til f.eks. 100K, 1.2M osv.
            String riskText = formatRiskValue(totalRisk);

            // Hent minimap-lokasjon (kan være null hvis spilleren er langt unna e.l.)
            Point minimapLoc = player.getMinimapLocation();
            if (minimapLoc == null)
            {
                continue;
            }

            // Tegn enten en dot eller en liten tekst
            switch (config.minimapDisplayMode())
            {
                case DOT:
                {
                    int size = 4; // punktets diameter
                    graphics.setColor(riskColor);
                    graphics.fillOval(
                            minimapLoc.getX() - size / 2,
                            minimapLoc.getY() - size / 2,
                            size,
                            size
                    );
                    break;
                }
                case RISK:
                {
                    OverlayUtil.renderTextLocation(graphics, minimapLoc, riskText, riskColor);
                    break;
                }
                default:
                {
                    // NONE -> ikke tegn noe
                    break;
                }
            }
        }

        return null;
    }

    /**
     * Returnerer true hvis spilleren bestå pvp-filteret i config.
     */
    private boolean passesPvpFilter(Player player)
    {
        PlayerRiskConfig.PvPMode mode = config.pvpMode();
        if (mode == PlayerRiskConfig.PvPMode.DISABLED)
        {
            return true; // Ingen filtrering
        }

        boolean inPvPWorld = client.getWorldType().contains(WorldType.PVP);
        boolean inWilderness = client.getVar(Varbits.IN_WILDERNESS) > 0;

        // Ved PvPMode=ON eller ATTACKABLE kreves enten PvPWorld eller Wilderness
        if (!inPvPWorld && !inWilderness)
        {
            return false;
        }

        // Ved ATTACKABLE sjekk også combat-lvl differanse
        if (mode == PlayerRiskConfig.PvPMode.ATTACKABLE)
        {
            Player local = client.getLocalPlayer();
            if (local != null)
            {
                int localCombat = local.getCombatLevel();
                int targetCombat = player.getCombatLevel();

                int allowedDiff = 15;
                if (inWilderness)
                {
                    // Wilderness-lvl
                    allowedDiff += client.getVar(Varbits.IN_WILDERNESS);
                }

                if (Math.abs(localCombat - targetCombat) > allowedDiff)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returnerer true hvis spilleren består skull-filteret i config.
     * (Kun relevant hvis pvpMode != DISABLED)
     */
    private boolean passesSkullFilter(Player player)
    {
        // Hvis config.skullMode() == ALL -> ingen filtrering
        PlayerRiskConfig.SkullMode mode = config.skullMode();
        if (mode == PlayerRiskConfig.SkullMode.ALL)
        {
            return true;
        }

        boolean isSkulled = (player.getSkullIcon() != -1);

        if (mode == PlayerRiskConfig.SkullMode.SKULLED && !isSkulled)
        {
            return false;
        }
        if (mode == PlayerRiskConfig.SkullMode.UNSKULLED && isSkulled)
        {
            return false;
        }

        return true;
    }

    /**
     * Plukker riktig farge basert på totalRisk, eller null hvis risk < laveste threshold.
     */
    private Color getRiskColor(long totalRisk)
    {
        if (totalRisk > config.insaneRiskGP())
        {
            return config.insaneRiskColor();
        }
        else if (totalRisk > config.highRiskGP())
        {
            return config.highRiskColor();
        }
        else if (totalRisk > config.mediumRiskGP())
        {
            return config.mediumRiskColor();
        }
        else if (totalRisk > config.lowRiskGP())
        {
            return config.lowRiskColor();
        }
        // Returner null hvis under "lowRiskGP"
        return null;
    }

    /**
     * Omformer tall til f.eks. 128K, 2.4M, etc.
     */
    private String formatRiskValue(long value)
    {
        if (value >= 1_000_000)
        {
            return String.format("%.1fM", value / 1_000_000.0);
        }
        else if (value >= 1_000)
        {
            return String.format("%dK", value / 1_000);
        }
        else
        {
            return String.valueOf(value);
        }
    }
}
