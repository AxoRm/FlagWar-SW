package io.github.townyadvanced.flagwar.gui;

import com.google.common.collect.Lists;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.newconfig.Messages;
import io.github.townyadvanced.flagwar.util.Messaging;
import io.github.townyadvanced.flagwar.util.SkullCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimeGui implements Listener {
    Town enemyTown;
    Town town;
    Player player;
    ZonedDateTime today;
    TownyAPI towny = TownyAPI.getInstance();
    private final Inventory inventory;
    private final List<Integer> slots = new ArrayList<>();

    public TimeGui(Player player, Town enemyTown, Town town) {
        this.enemyTown = enemyTown;
        this.town = town;
        this.player = player;
        Component inventoryTitle = Component.text("Война: ")
                .append(Component.text(enemyTown.getName()))
                .append(Component.text(", День?"));
        inventory = Bukkit.createInventory(null, 9, inventoryTitle);
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getInstance());
        today = ZonedDateTime.now(ZoneId.of("GMT+3"));
        initializeItems();
    }

    public void initializeItems() {
        // Устанавливаем голову с названием "Сегодня"
        ItemStack todayHead = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWQ0NTJiZmNkYzNlYTE0ZjllNjEyYjFjOTZhYmVmOTdjMTBlOTZjNzExNmVhMmE0YjFhNWRmOGQ0YWExNzJmOSJ9fX0=");
        ItemMeta todayMeta = todayHead.getItemMeta();
        assert todayMeta != null;
        todayMeta.displayName(Component.text(Messages.dayGuiToday));
        todayMeta.lore(Messaging.formatForList(Messages.dayGuiDisallowedDayLore));
        todayHead.setItemMeta(todayMeta);
        inventory.setItem(1, todayHead);

        // Заполняем оставшиеся слоты датами
        for (int i = 2; i < 8; i++) {
            String dateString = getDateFormatted(i - 1);
            ItemStack head = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWQ0NTJiZmNkYzNlYTE0ZjllNjEyYjFjOTZhYmVmOTdjMTBlOTZjNzExNmVhMmE0YjFhNWRmOGQ0YWExNzJmOSJ9fX0=");
            ItemMeta meta = head.getItemMeta();
            assert meta != null;
            meta.displayName(Messaging.formatForComponent("&f" + dateString));
            ZonedDateTime date = today.plusDays(i - 1).withHour(19).withMinute(0).withSecond(0).withNano(0);
            long millisUntilDate = date.toInstant().toEpochMilli() - today.toInstant().toEpochMilli();
            long hoursUntilDate = millisUntilDate / (1000 * 60 * 60);
            List<String> lore = Arrays.asList("12", "13");
            meta.lore(Messaging.formatForList(Messages.dayGuiDisallowedDayLore));
            if (hoursUntilDate >= 36) {
                meta.displayName(Messaging.formatForComponent("&e" + dateString));
                meta.lore(Messaging.formatForList(Messages.dayGuiAllowedDayLore));
                slots.add(i);
            }
            head.setItemMeta(meta);
            inventory.setItem(i, head);
        }
    }

    public void displayInventory(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!Objects.equals(e.getClickedInventory(), inventory)) return;
        e.setCancelled(true);
        if (!slots.contains(e.getSlot())) return;
        Component name = Objects.requireNonNull(e.getCurrentItem()).getItemMeta().displayName();
        String dateFormatted = getDateFormatted(e.getSlot() - 1);
        ZonedDateTime selectedDate = today.plusDays(e.getSlot() - 1);
        assert name != null;
        HourGui hourGui = new HourGui(today, selectedDate, name, player, enemyTown, town);
        hourGui.displayInventory(player);
    }

    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }
    }

    private String getDateFormatted(int i) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("ru"));
        ZonedDateTime date = today.plusDays(i);
        return date.format(formatter);
    }
}
