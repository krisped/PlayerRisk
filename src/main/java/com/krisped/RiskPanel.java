package com.krisped;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.kit.KitType;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RiskPanel extends PluginPanel
{
    private static final String NO_PLAYER_SELECTED = "No player selected";
    private JPanel equipmentPanels;
    private JPanel header;
    public JLabel nameLabel;
    private final ItemManager itemManager;
    private final Client client; // Brukes for å hente local player
    private final ClientThread clientThread; // For å kjøre kode på klienttråden
    private JButton checkOwnRiskButton; // Knapp for å sjekke eget risk

    /**
     * Konstruktør som tar imot Client, ClientThread og ItemManager.
     */
    public RiskPanel(Client client, ClientThread clientThread, ItemManager itemManager)
    {
        this.itemManager = itemManager;
        this.client = client;
        this.clientThread = clientThread;

        // Oppsett av layout med GroupLayout
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(0, 0, 0, 0));
        setOpaque(false);

        // Panelet for utstyrsinfo med GridBagLayout
        equipmentPanels = new JPanel(new GridBagLayout());
        equipmentPanels.setOpaque(false);

        // Header-panelet med spillernavn
        header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        header.setOpaque(false);

        nameLabel = new JLabel(NO_PLAYER_SELECTED, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(FontManager.getRunescapeFont());
        nameLabel.setOpaque(false);
        header.add(nameLabel, BorderLayout.CENTER);

        // Opprett knappen "Check your own risk"
        checkOwnRiskButton = new JButton("Check your own risk");
        checkOwnRiskButton.setFont(FontManager.getRunescapeFont());
        checkOwnRiskButton.addActionListener(e -> checkOwnRisk());

        // Sett opp layout med header, equipmentPanels og knappen
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(header)
                        .addComponent(equipmentPanels)
                        .addComponent(checkOwnRiskButton)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(header)
                        .addGap(10)
                        .addComponent(equipmentPanels)
                        .addGap(10)
                        .addComponent(checkOwnRiskButton)
        );

        // Start med et tomt oppsett
        updateEquipment(new HashMap<>(), new HashMap<>(), "");
    }

    /**
     * Oppdaterer panelet med spillerens utstyr og risiko per slot.
     */
    public void updateEquipment(Map<KitType, ItemComposition> equipment, Map<KitType, Integer> equipmentRisks, String playerName)
    {
        if (playerName == null || playerName.isEmpty())
        {
            nameLabel.setText(NO_PLAYER_SELECTED);
        }
        else
        {
            nameLabel.setText("Player: " + playerName);
        }

        SwingUtilities.invokeLater(() ->
        {
            equipmentPanels.removeAll();
            // Opprett en ny GridBagConstraints for hvert kall slik at vi starter øverst
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;

            AtomicLong totalRisk = new AtomicLong();
            for (Map.Entry<KitType, ItemComposition> entry : equipment.entrySet())
            {
                KitType kit = entry.getKey();
                ItemComposition item = entry.getValue();
                int risk = equipmentRisks.getOrDefault(kit, 0);
                totalRisk.addAndGet(risk);
                AsyncBufferedImage image = itemManager.getImage(item.getId());
                JPanel itemPanel = createRiskItemPanel(item, kit, image, risk);
                itemPanel.setOpaque(false);
                equipmentPanels.add(itemPanel, gbc);
                gbc.gridy++;
            }
            if (!nameLabel.getText().equals(NO_PLAYER_SELECTED))
            {
                JPanel totalPanel = createTotalPanel(totalRisk.get());
                totalPanel.setOpaque(false);
                equipmentPanels.add(totalPanel, gbc);
                gbc.gridy++;
            }
            equipmentPanels.revalidate();
            equipmentPanels.repaint();
        });
    }

    /**
     * Lager et panel for et enkelt utstyrs-slot med risikovurdering.
     */
    private JPanel createRiskItemPanel(ItemComposition item, KitType kit, AsyncBufferedImage image, int risk)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        image.addTo(iconLabel);
        panel.add(iconLabel);
        panel.add(Box.createHorizontalStrut(8));

        // Delpanel for tekst
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel name = new JLabel(item.getName());
        name.setFont(FontManager.getRunescapeFont());
        name.setForeground(Color.WHITE);
        name.setOpaque(false);

        // Formater kit-typen med stor forbokstav
        String kitName = StringUtils.capitalize(kit.toString().toLowerCase());
        JLabel slot = new JLabel(kitName);
        slot.setFont(FontManager.getRunescapeSmallFont());
        slot.setForeground(Color.GRAY);
        slot.setOpaque(false);

        JLabel riskLabel = new JLabel(QuantityFormatter.quantityToStackSize(risk));
        riskLabel.setFont(FontManager.getRunescapeFont());
        if (risk > 1_000_000)
        {
            riskLabel.setForeground(Color.GREEN);
        }
        else if (risk > 100_000)
        {
            riskLabel.setForeground(Color.WHITE);
        }
        else if (risk == 0)
        {
            riskLabel.setForeground(Color.LIGHT_GRAY);
        }
        else
        {
            riskLabel.setForeground(Color.YELLOW);
        }
        riskLabel.setToolTipText(java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(risk));
        riskLabel.setOpaque(false);

        textPanel.add(name);
        textPanel.add(slot);
        textPanel.add(riskLabel);

        panel.add(textPanel);
        return panel;
    }

    /**
     * Lager et panel for totalrisiko.
     */
    private JPanel createTotalPanel(long totalRisk)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel.setOpaque(false);

        JLabel label = new JLabel("Total: ");
        label.setFont(FontManager.getRunescapeFont());
        label.setForeground(Color.WHITE);
        label.setOpaque(false);
        JLabel totalLabel = new JLabel(QuantityFormatter.quantityToStackSize(totalRisk));
        totalLabel.setFont(FontManager.getRunescapeFont());
        if (totalRisk > 1_000_000)
        {
            totalLabel.setForeground(Color.GREEN);
        }
        else if (totalRisk > 100_000)
        {
            totalLabel.setForeground(Color.WHITE);
        }
        else if (totalRisk == 0)
        {
            totalLabel.setForeground(Color.LIGHT_GRAY);
        }
        else
        {
            totalLabel.setForeground(Color.YELLOW);
        }
        totalLabel.setOpaque(false);
        totalLabel.setToolTipText(java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalRisk));

        panel.add(Box.createHorizontalStrut(8));
        panel.add(label);
        panel.add(totalLabel);
        return panel;
    }

    /**
     * Henter ut utstyr for local player og oppdaterer panelet.
     * Koden som kaller client.getItemDefinition kjøres nå på klienttråden.
     */
    private void checkOwnRisk()
    {
        Player local = client.getLocalPlayer();
        if (local == null || local.getPlayerComposition() == null)
        {
            JOptionPane.showMessageDialog(this, "Local player data is unavailable.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kjører koden på klienttråden
        clientThread.invokeLater(() -> {
            Map<KitType, ItemComposition> equipment = new HashMap<>();
            Map<KitType, Integer> equipmentRisks = new HashMap<>();

            for (KitType kit : KitType.values())
            {
                int itemId = local.getPlayerComposition().getEquipmentId(kit);
                if (itemId > 0)
                {
                    // Nå kalles denne metoden på klienttråden
                    ItemComposition comp = client.getItemDefinition(itemId);
                    if (comp != null)
                    {
                        equipment.put(kit, comp);
                        equipmentRisks.put(kit, itemManager.getItemPrice(itemId));
                    }
                }
            }
            // Oppdaterer UI-en på Swing-tråden
            SwingUtilities.invokeLater(() -> {
                updateEquipment(equipment, equipmentRisks, local.getName());
            });
        });
    }
}
