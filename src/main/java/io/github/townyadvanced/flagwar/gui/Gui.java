package io.github.townyadvanced.flagwar.gui;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.SkullCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
public class Gui implements Listener {
    Player player;
    Town enemyTown;
    Town town;
    TownyAPI towny = TownyAPI.getInstance();
    private final Inventory inventory;
    public Gui(Player player, Town enemyTown, Town town) {
        this.player = player;
        this.enemyTown = enemyTown;
        this.town = town;
        Component inventoryTitle = Component.text("Война: ")
                .append(Component.text(enemyTown.getName()));
        inventory = Bukkit.createInventory(null, 9, inventoryTitle);
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getInstance());
        initializeItems(player);
    }


    public void initializeItems(Player player) {
        Resident resident = towny.getResident(player);
        ItemStack head = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTk3ZTRlMjdhMDRhZmE1ZjA2MTA4MjY1YTliZmI3OTc2MzAzOTFjN2YzZDg4MGQyNDRmNjEwYmIxZmYzOTNkOCJ9fX0=");
        ItemMeta meta = head.getItemMeta();
        assert meta != null;
        meta.lore(List.of(Component.text(""), Component.text("")));
        meta.displayName(Component.text("§r§f" + "Начать войну"));
        head.setItemMeta(meta);
        inventory.setItem(4, head);
    }

    public void displayInventory(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!Objects.equals(e.getClickedInventory(), inventory)) return;
        if (e.getSlot() != 4) return;
        final Resident resident = towny.getResident(player);
        TimeGui time = new TimeGui(player, enemyTown, town);
        time.displayInventory(player);
        e.setCancelled(true);
    }
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }
    }
}
