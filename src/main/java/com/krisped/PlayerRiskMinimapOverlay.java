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

    // Du kan styre dette fra plugin-koden om ønskelig
    private boolean enabled = true;

    @Inject
    public PlayerRiskMinimapOverlay(
            Client client,
            ItemManager itemManager,
            PlayerRiskConfig config,
            CombatManager combatManager
    )
    {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        this.combatManager = combatManager;

        // Overlay-innstillinger
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
        // Hvis overlay er deaktivert, eller local player mangler
        if (!enabled || client.getLocalPlayer() == null)
        {
            return null;
        }

        // Hvis highlight er slått av i combat
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

            // Hvis "highlightLocalPlayer" = false, hopp over deg selv
            if (player.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer())
            {
                continue;
            }

            // Filtrer på PvP Mode (ON / ATTACKABLE => krever PvP-område, etc.)
            if (!passesPvpFilter(player))
            {
                continue;
            }

            // Hvis pvpMode != DISABLED, filtrer på skull mode
            if (!passesSkullFilter(player))
            {
                continue;
            }

            // Finn risiko basert på gear
            long totalRisk = RiskCalculator.calculateRisk(player, itemManager);

            // Finn en farge basert på om risk er > low/med/high/insane
            Color riskColor = getRiskColor(totalRisk);
            if (riskColor == null)
            {
                // Under lowRiskGP => ingenting tegnes
                continue;
            }

            // Formatér tall til f.eks. "123K"
            String riskText = formatRiskValue(totalRisk);

            // Minimapposisjon
            Point minimapLoc = player.getMinimapLocation();
            if (minimapLoc == null)
            {
                continue;
            }

            // Tegn enten en dot eller en tekst
            switch (config.minimapDisplayMode())
            {
                case DOT:
                {
                    int size = 4; // Sirkelstørrelse
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
                    // Tegn f.eks. "123K" i valgte farge
                    OverlayUtil.renderTextLocation(graphics, minimapLoc, riskText, riskColor);
                    break;
                }
                default:
                    // NONE => ikke tegn noe
                    break;
            }
        }

        return null;
    }

    /**
     * Avgjør om spilleren passer pvpMode-filteret.
     * pvpMode=DISABLED => ingen PvP-sjekk.
     */
    private boolean passesPvpFilter(Player player)
    {
        PlayerRiskConfig.PvPMode pvpMode = config.pvpMode();

        if (pvpMode == PlayerRiskConfig.PvPMode.DISABLED)
        {
            // Ingen filtrering
            return true;
        }

        // Ellers kreves det at spilleren er i PvP-verden eller Wilderness
        boolean inPvPWorld = client.getWorldType().contains(WorldType.PVP);
        boolean inWilderness = (client.getVar(Varbits.IN_WILDERNESS) > 0);

        if (!inPvPWorld && !inWilderness)
        {
            return false;
        }

        // Hvis pvpMode=ATTACKABLE => sjekk combat-lvl differanse
        if (pvpMode == PlayerRiskConfig.PvPMode.ATTACKABLE)
        {
            Player local = client.getLocalPlayer();
            if (local != null)
            {
                int localCombat = local.getCombatLevel();
                int targetCombat = player.getCombatLevel();

                // 15 er base. I Wilderness øker differansen med wilderness-lvl
                int allowedDiff = 15;
                if (inWilderness)
                {
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
     * Avgjør om spilleren passer skullMode-filteret.
     * Brukes kun hvis pvpMode != DISABLED
     */
    private boolean passesSkullFilter(Player player)
    {
        // Hvis pvpMode=DISABLED -> ignorer skull-mode
        if (config.pvpMode() == PlayerRiskConfig.PvPMode.DISABLED)
        {
            return true;
        }

        PlayerRiskConfig.SkullMode skullMode = config.skullMode();
        if (skullMode == PlayerRiskConfig.SkullMode.ALL)
        {
            return true;
        }

        boolean isSkulled = (player.getSkullIcon() != -1);

        if (skullMode == PlayerRiskConfig.SkullMode.SKULLED && !isSkulled)
        {
            return false;
        }
        if (skullMode == PlayerRiskConfig.SkullMode.UNSKULLED && isSkulled)
        {
            return false;
        }

        return true;
    }

    /**
     * Plukker riktig overlay-farge basert på risk-verdien.
     * Returnerer null hvis spilleren har < lowRiskGP.
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
        // Ingenting
        return null;
    }

    /**
     * Konverterer f.eks. 1234567 -> "1.2M"
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
