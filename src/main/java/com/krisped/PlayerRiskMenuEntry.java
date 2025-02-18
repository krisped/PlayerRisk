package com.krisped;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import java.awt.Color;

@PluginDescriptor(
        name = "[KP] Player Risk Highlighter",
        description = "Legger til en 'Risk Check'-meny ved høyreklikk på spillere",
        tags = {"risk", "pvp", "menu"}
)
@Slf4j
@Singleton
public class PlayerRiskMenuEntry extends Plugin
{
    public static final String INSPECT_RISK = "Risk Check";

    @Inject
    private Client client;

    @Inject
    private Provider<MenuManager> menuManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private PlayerRiskConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    // Lagre spillerinfo for senere bruk (dersom spillerdata ikke hentes direkte)
    private final Map<Integer, PlayerInfo> storedPlayers = new HashMap<>();

    @Subscribe
    public void onMenuOpened(MenuOpened event)
    {
        // Når menyen åpnes, lagres alle spillere fra menyoppføringene
        for (MenuEntry entry : event.getMenuEntries())
        {
            if (entry.getActor() instanceof Player)
            {
                Player p = (Player) entry.getActor();
                storedPlayers.put(p.getId(), new PlayerInfo(p.getId(), p.getName(), p.getPlayerComposition()));
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        // Vi håndterer kun spiller-oppføringer
        if (event.getType() != MenuAction.RUNELITE_PLAYER.getId())
        {
            return;
        }
        // Bruk riskMenuMode fra konfigurasjonen:
        // - RIGHT_CLICK: alltid legg til
        // - SHIFT_RIGHT_CLICK: legg til kun om SHIFT er trykket
        // - DISABLED: fjern meny
        switch (config.riskMenuMode())
        {
            case DISABLED:
                removeMenuItem();
                return;
            case SHIFT_RIGHT_CLICK:
                if (!client.isKeyPressed(KeyCode.KC_SHIFT))
                {
                    removeMenuItem();
                    return;
                }
                // Fall-through: hvis SHIFT er trykket, legg til
            case RIGHT_CLICK:
                addMenuItem();
                break;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuAction() != MenuAction.RUNELITE_PLAYER)
        {
            return;
        }
        // Endret: bruk contains for å sjekke menyteksten slik at fargekoder ignoreres
        if (!event.getMenuOption().contains(INSPECT_RISK))
        {
            return;
        }

        // Hent spillerinfo basert på eventets id
        PlayerInfo info = storedPlayers.get(event.getId());
        if (info == null)
        {
            return;
        }
        // Prøv å hente spilleren direkte
        Player target = client.getTopLevelWorldView().players().byIndex(event.getId());
        if (target == null)
        {
            target = getPlayerFromInfo(info);
        }
        if (target == null)
        {
            return;
        }
        // Sjekk om det er lokal spiller, og om vi skal vise den
        if (target.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer())
        {
            return;
        }
        // Filtrer basert på skull-status
        PlayerRiskConfig.SkullMode skullMode = config.skullMode();
        boolean isSkulled = target.getSkullIcon() != -1;
        if ((skullMode == PlayerRiskConfig.SkullMode.UNSKULLED && isSkulled) ||
                (skullMode == PlayerRiskConfig.SkullMode.SKULLED && !isSkulled))
        {
            return;
        }

        // Kalkuler risiko
        long risk = RiskCalculator.calculateRisk(target, itemManager);
        String formattedRisk = NumberFormat.getNumberInstance().format(risk);

        // Utfør handling basert på config for riskMenuAction
        if (config.riskMenuAction() == PlayerRiskConfig.RiskMenuAction.CHAT ||
                config.riskMenuAction() == PlayerRiskConfig.RiskMenuAction.ALL)
        {
            String message = "Player " + target.getName() + " is risking " + formattedRisk + " GP.";
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
        }
        if (config.riskMenuAction() == PlayerRiskConfig.RiskMenuAction.SIDE_PANEL ||
                config.riskMenuAction() == PlayerRiskConfig.RiskMenuAction.ALL)
        {
            // Bygg utstyrskart og oppdater sidepanelet
            Map<KitType, net.runelite.api.ItemComposition> equipment = new HashMap<>();
            Map<KitType, Integer> equipmentPrices = new HashMap<>();
            if (target.getPlayerComposition() != null)
            {
                for (KitType kit : KitType.values())
                {
                    int itemId = target.getPlayerComposition().getEquipmentId(kit);
                    if (itemId > 0)
                    {
                        net.runelite.api.ItemComposition comp = client.getItemDefinition(itemId);
                        equipment.put(kit, comp);
                        equipmentPrices.put(kit, itemManager.getItemPrice(itemId));
                    }
                }
            }
            final Player finalTarget = target;
            SwingUtilities.invokeLater(() ->
            {
                clientToolbar.openPanel(PlayerRiskPlugin.getRiskNavigationButton());
                PlayerRiskPlugin.getRiskPanel().updateEquipment(equipment, equipmentPrices, finalTarget.getName());
            });
        }
        // Tøm lagrede spillere for å unngå utdatert info
        storedPlayers.clear();
    }

    // Hjelpemetode for å hente spiller basert på lagret info
    private Player getPlayerFromInfo(PlayerInfo info)
    {
        for (Player p : client.getPlayers())
        {
            if (p != null && p.getId() == info.getId())
            {
                return p;
            }
        }
        return null;
    }

    private void addMenuItem()
    {
        // Hent fargen fra konfigurasjonen
        Color menuColor = config.riskMenuColor();
        String colorHex = String.format("%02X%02X%02X", menuColor.getRed(), menuColor.getGreen(), menuColor.getBlue());
        String coloredOption = "<col=" + colorHex + ">" + INSPECT_RISK + "</col>";
        if (Arrays.stream(client.getPlayerOptions()).noneMatch(coloredOption::equals))
        {
            menuManager.get().addPlayerMenuItem(coloredOption);
        }
    }

    private void removeMenuItem()
    {
        menuManager.get().removePlayerMenuItem(INSPECT_RISK);
    }

    // Enkel lagringsklasse for spillerinfo
    private static class PlayerInfo
    {
        private final int id;
        private final String name;
        private final PlayerComposition playerComposition;

        PlayerInfo(int id, String name, PlayerComposition composition)
        {
            this.id = id;
            this.name = name;
            this.playerComposition = composition;
        }

        public int getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public PlayerComposition getPlayerComposition()
        {
            return playerComposition;
        }
    }
}
