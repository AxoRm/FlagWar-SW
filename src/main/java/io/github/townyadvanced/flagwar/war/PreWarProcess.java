package io.github.townyadvanced.flagwar.war;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.storage.NewWar;
import io.github.townyadvanced.flagwar.storage.SQLiteStorage;
import io.github.townyadvanced.flagwar.util.Messaging;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PreWarProcess implements Listener {
    Set<NewWar> newWars = SQLiteStorage.newWars; //TODO: Реализовать PreWarProcess extneds NewWar, так как PreWarProcess должен разбирается только с 1 войной, далее сделать список уже из PreWarProcess в классе WarManager
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public PreWarProcess() {
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getFlagWar());
    }

    public void scheduleNotifications() {
        for (NewWar war : newWars) {
            planNotifications(war);
        }
    }

    public void scheduleNotification(NewWar war) {
        newWars.add(war);
        planNotifications(war);
    }

    private void planNotifications(NewWar war) {
        ZonedDateTime warTime = ZonedDateTime.of(war.year, war.month, war.day, war.hour, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();

        long[] notificationTimesInMinutes = {
                24 * 60, 12 * 60, 6 * 60, 3 * 60, 60, 30, 10, 3
        };

        long[] notificationTimesInSeconds = {
                10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0
        };

        scheduleNotification(war, warTime, now.plusSeconds(5), now, ChronoUnit.MINUTES.between(now.plusSeconds(5), warTime), ChronoUnit.MINUTES);

        for (long minutesBefore : notificationTimesInMinutes) {
            scheduleNotification(war, warTime, warTime.minus(minutesBefore, ChronoUnit.MINUTES), now, minutesBefore, ChronoUnit.MINUTES);
        }

        for (long secondsBefore : notificationTimesInSeconds) {
            scheduleNotification(war, warTime, warTime.minus(secondsBefore, ChronoUnit.SECONDS), now, secondsBefore, ChronoUnit.SECONDS);
        }
    }

    private void scheduleNotification(NewWar war, ZonedDateTime warTime, ZonedDateTime notificationTime, ZonedDateTime now, long timeBefore, ChronoUnit unit) {
        long delay = ChronoUnit.SECONDS.between(now, notificationTime);
        if (delay >= 0) {
            scheduler.schedule(() -> sendNotification(war, warTime, timeBefore, unit), delay, TimeUnit.SECONDS);
        }
    }

    private void sendNotification(NewWar war, ZonedDateTime warTime, long timeBefore, ChronoUnit unit) {
        String timeLeftMessage;
        if (unit == ChronoUnit.MINUTES) {
            long days = timeBefore / (24 * 60);
            long hours = (timeBefore % (24 * 60)) / 60;
            long minutes = timeBefore % 60;
            StringBuilder messageBuilder = new StringBuilder();

            if (days > 0) {
                messageBuilder.append(days).append(" д. ");
            }

            if (hours > 0) {
                messageBuilder.append(hours).append(" ч. ");
            }

            if (minutes > 0) {
                messageBuilder.append(minutes).append(" мин. ");
            }
            timeLeftMessage = messageBuilder.toString().trim();
        } else {
            if (timeBefore % 10 == 1) {
                timeLeftMessage = timeBefore + " секунда";
            } else if (timeBefore % 10 >= 2 && timeBefore % 10 <= 4) {
                timeLeftMessage = timeBefore + " секунды";
            } else {
                timeLeftMessage = timeBefore + " секунд";
            }
        }
        String attackerMessage = "&eДо начала войны с &c" + war.victim.getName() + " &eосталось &c" + timeLeftMessage;
        String victimMessage = "&eДо начала войны с &c" + war.attacker.getName() + " &eосталось &c" + timeLeftMessage;

        sendNotificationToTown(war.attacker, attackerMessage);
        sendNotificationToTown(war.victim, victimMessage);
    }

    private void sendNotificationToTown(Town town, String message) {
        // Iterate through each online resident of the town
        for (Resident resident: town.getResidents()) {
            Player player = resident.getPlayer();
            if (player != null && player.isOnline()) {
                // Send action bar message
                player.sendActionBar(Messaging.formatForString(message));
                // Send title message
                player.sendTitle(Messaging.formatForString("&cВнимание! Вам объявили войну!"), Messaging.formatForString(message), 10, 140, 20);
                // Send chat message
                player.sendMessage(Messaging.formatForString(message));
            }
        }
    }
}
