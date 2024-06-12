package io.github.townyadvanced.flagwar.gui;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.SkullCreator;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
public class Gui implements Listener {
    private final Inventory inventory;
    public Gui(Player player) {
        inventory = Bukkit.createInventory(null, 9, Component.text("Войны"));
        initializeItems(player);
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getInstance());
    }


    public void initializeItems(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        ItemStack head = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTk3ZTRlMjdhMDRhZmE1ZjA2MTA4MjY1YTliZmI3OTc2MzAzOTFjN2YzZDg4MGQyNDRmNjEwYmIxZmYzOTNkOCJ9fX0=");
        ItemMeta meta = head.getItemMeta();
        assert meta != null;
        meta.lore(List.of(Component.text(""), Component.text("")));
        meta.displayName(Component.text("Начать войну"));
        head.setItemMeta(meta);
        inventory.setItem(4, head);
    }


    protected ItemStack createGuiItem(String materialText) {
        Material material = Material.matchMaterial(materialText);
        if (material == null) {
            Bukkit.getLogger().info(DynByer.messages.format(DynByer.messages.getString("invalidMaterial"), materialText));
            DynByer.instance.getServer().getPluginManager().disablePlugin(DynByer.instance);
            return null;
        }
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setLore(Config.format(Config.lore, String.valueOf(String.format(Locale.US, "%,.2f", coefficient*price)), String.valueOf( String.format(Locale.US,"%,.2f",(price * coefficient * (1 - Math.pow(standartCoefficient, 64 / period))) / (1 - standartCoefficient)))));
        item.setItemMeta(meta);
        return item;
    }

    public void displayInventory(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!Objects.equals(e.getClickedInventory(), inventory)) return;
        final Player player = (Player) e.getWhoClicked();
        e.setCancelled(true);
        initializeItems(player);
        final ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir() || fontMaterials.contains(clickedItem.getType())) return;
        Item item = DynByer.items.stream().filter(aitem -> aitem.getSlot() == e.getSlot()).findFirst().orElse(new Item("cobblestone",0,0,0,0));
        Map<String, DatabaseItem> databaseItems = Database.databaseItems.getOrDefault(player.getName(), new HashMap<>());
        DatabaseItem databaseItem = databaseItems.getOrDefault(item.getId() + "_" + e.getSlot(), new DatabaseItem(0, item.getId() + "_" + e.getSlot()));
        if (!e.isLeftClick())
            return;
        if (e.isShiftClick()) {
            if (!economy.sellStackItem(player, item.getStartPrice() * getCoefficient(player, item), Material.matchMaterial(item.getId()), item.getCoefficient(), item.getPeriod(), databaseItem)) {
                player.sendMessage(DynByer.messages.getString("notEnoughBlocks"));
                return;
            }
        } else {
            if (!economy.sellItem(player, item.getStartPrice() * getCoefficient(player, item), Material.matchMaterial(item.getId()))) {
                player.sendMessage(DynByer.messages.getString("notEnoughBlocks"));
                return;
            }
            databaseItem.addSelled(1);
        }
        databaseItems.put(item.getId() + "_" + e.getSlot(), databaseItem);
        Database.databaseItems.put(player.getName(), databaseItems);
        initializeItems(player);
    }
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }
    }
}
