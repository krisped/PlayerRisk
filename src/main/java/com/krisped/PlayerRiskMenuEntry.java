package com.krisped;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.KeyCode;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.ClientToolbar;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class PlayerRiskMenuEntry {

    public static final String INSPECT_RISK = "Risk Check";

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private MenuManager menuManager;

    @Inject
    private PlayerRiskConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (event.getType() != MenuAction.RUNELITE_PLAYER.getId()) {
            return;
        }

        Player player = client.getCachedPlayers()[event.getIdentifier()];
        if (player == null) {
            return;
        }

        if (player.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer()) {
            return;
        }

        PlayerRiskConfig.SkullMode skullMode = config.skullMode();
        boolean isSkulled = player.getSkullIcon() != -1;
        if (skullMode == PlayerRiskConfig.SkullMode.UNSKULLED && isSkulled)
            return;
        else if (skullMode == PlayerRiskConfig.SkullMode.SKULLED && !isSkulled)
            return;

        if (config.riskMenuMode() == PlayerRiskConfig.RiskMenuMode.DISABLED) {
            return;
        }

        if (config.riskMenuMode() == PlayerRiskConfig.RiskMenuMode.SHIFT_RIGHT_CLICK &&
                !client.isKeyPressed(KeyCode.KC_SHIFT)) {
            return;
        }

        for (MenuEntry entry : client.getMenuEntries()) {
            if (entry.getOption().contains(INSPECT_RISK) &&
                    entry.getIdentifier() == event.getIdentifier()) {
                return;
            }
        }

        Color menuColor = config.riskMenuColor();
        String colorHex = toHex(menuColor);

        client.createMenuEntry(-1)
                .setOption("<col=" + colorHex + ">" + INSPECT_RISK + "</col>")
                .setTarget(event.getTarget())
                .setType(MenuAction.RUNELITE_PLAYER)
                .setIdentifier(event.getIdentifier());
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!event.getMenuOption().contains(INSPECT_RISK)) {
            return;
        }

        Player targetPlayer = client.getCachedPlayers()[event.getMenuEntry().getIdentifier()];
        if (targetPlayer == null) {
            return;
        }

        if (targetPlayer.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer()) {
            return;
        }

        PlayerRiskConfig.SkullMode skullMode = config.skullMode();
        boolean isSkulled = targetPlayer.getSkullIcon() != -1;
        if (skullMode == PlayerRiskConfig.SkullMode.UNSKULLED && isSkulled)
            return;
        else if (skullMode == PlayerRiskConfig.SkullMode.SKULLED && !isSkulled)
            return;

        long risk = RiskCalculator.calculateRisk(targetPlayer, itemManager);
        String formattedRisk = NumberFormat.getNumberInstance(Locale.US).format(risk);

        PlayerRiskConfig.RiskMenuAction action = config.riskMenuAction();

        if (action == PlayerRiskConfig.RiskMenuAction.CHAT || action == PlayerRiskConfig.RiskMenuAction.BOTH) {
            String message = "<col=483D8B>Player " + targetPlayer.getName() + " is risking " + formattedRisk + " GP.</col>";
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
        }

        if (action == PlayerRiskConfig.RiskMenuAction.SIDE_PANEL || action == PlayerRiskConfig.RiskMenuAction.BOTH) {
            Map<net.runelite.api.kit.KitType, net.runelite.api.ItemComposition> equipment = new HashMap<>();
            Map<net.runelite.api.kit.KitType, Integer> equipmentPrices = new HashMap<>();
            if (targetPlayer.getPlayerComposition() != null) {
                for (net.runelite.api.kit.KitType kit : net.runelite.api.kit.KitType.values()) {
                    int itemId = targetPlayer.getPlayerComposition().getEquipmentId(kit);
                    if (itemId > 0) {
                        net.runelite.api.ItemComposition comp = client.getItemDefinition(itemId);
                        equipment.put(kit, comp);
                        equipmentPrices.put(kit, itemManager.getItemPrice(itemId));
                    }
                }
            }
            // Tving opp sidepanelet ved å åpne navigasjonsknappen og oppdatere panelet
            SwingUtilities.invokeLater(() -> {
                clientToolbar.openPanel(PlayerRiskPlugin.getRiskNavigationButton());
                PlayerRiskPlugin.getRiskPanel().updateEquipment(equipment, equipmentPrices, targetPlayer.getName());
            });
        }
    }

    private String toHex(Color color) {
        return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
