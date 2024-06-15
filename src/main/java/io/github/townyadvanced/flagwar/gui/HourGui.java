package io.github.townyadvanced.flagwar.gui;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.storage.NewWar;
import io.github.townyadvanced.flagwar.storage.SQLiteStorage;
import io.github.townyadvanced.flagwar.util.Messaging;
import io.github.townyadvanced.flagwar.util.SkullCreator;
import io.github.townyadvanced.flagwar.war.PreWarProcess;
import net.kyori.adventure.text.Component;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HourGui implements Listener {
    Town town;
    TownyAPI towny = TownyAPI.getInstance();
    ZonedDateTime today;
    ZonedDateTime selectedDay;
    Player player;
    Town enemyTown;
    private final List<Integer> slots = new ArrayList<>();
    private final Inventory inventory;

    public HourGui(ZonedDateTime today, ZonedDateTime selectedDay, Component highName, Player player, Town enemyTown, Town town) {
        this.enemyTown = enemyTown;
        this.town = town;
        Component inventoryTitle = Component.text("Война: ")
                .append(Component.text(enemyTown.getName())) // Append enemyTownName
                .append(Component.text(", ")) // Append a comma and space
                .append(highName.replaceText(builder -> builder.matchLiteral(ChatColor.YELLOW.toString()).replacement(""))) // Append modified highName
                .append(Component.text(", Час?"));
        inventory = Bukkit.createInventory(null, 9, inventoryTitle);
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getInstance());
        this.player = player;
        this.today = today;
        this.selectedDay = selectedDay;
        initializeItems();
    }

    public void initializeItems() {
        for (int i = 13; i <= 19; i++) {
            ItemStack hourHead = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2JiYzA2YThkNmIxNDkyZTQwZjBlN2MzYjYzMmI2ZmQ4ZTY2ZGM0NWMxNTIzNDk5MGNhYTU0MTBhYzNhYzNmZCJ9fX0=");
            ItemMeta meta = hourHead.getItemMeta();
            assert meta != null;
            String hourString = i + ":00";
            meta.displayName(Messaging.formatForComponent("&f" + hourString));

            ZonedDateTime selectedHour = selectedDay.withHour(i).withMinute(0).withSecond(0).withNano(0);

            long millisUntilSelectedHour = selectedHour.toInstant().toEpochMilli() - today.toInstant().toEpochMilli();
            long hoursUntilSelectedHour = millisUntilSelectedHour / (1000 * 60 * 60);
            if (hoursUntilSelectedHour >= 36) {
                meta.displayName(Messaging.formatForComponent("&e" + hourString));
                meta.lore(Messaging.formatForList(List.of("&aНажмите, чтобы выбрать", "&aэтот час войны!")));
                slots.add(i-12);
            } else {
                meta.lore(Messaging.formatForList(List.of("&cВы не можете начать", "&cвойну в этот час!")));
            }

            hourHead.setItemMeta(meta);
            inventory.setItem(i - 12, hourHead); // Mapping 13:00-19:00 to slots 1-7
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
        int hour = e.getSlot() + 12;
        int day = selectedDay.getDayOfMonth();
        int month = selectedDay.getMonthValue();
        int year = selectedDay.getYear();
        NewWar newWar = new NewWar(town, enemyTown, year, month, day, hour);
        PreWarProcess preWarProcess = new PreWarProcess();
        preWarProcess.scheduleNotification(newWar);
        inventory.close();
    }

    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }
    }
}
