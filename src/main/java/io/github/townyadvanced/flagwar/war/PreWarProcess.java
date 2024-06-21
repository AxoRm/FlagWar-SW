package io.github.townyadvanced.flagwar.war;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.newconfig.Messages;
import io.github.townyadvanced.flagwar.storage.NewWar;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PreWarProcess implements Listener {
    NewWar war;
    ZonedDateTime warTime;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public PreWarProcess(NewWar war) {
        this.war = war;
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getFlagWar());
        planNotifications();
    }

    private void planNotifications() {
        warTime = war.warTime;
        ZonedDateTime now = ZonedDateTime.now();

        long[] notificationTimesInSeconds = {
                24 * 3600, 12 * 3600, 6 * 3600, 3 * 3600, 3600, 30 * 60, 10 * 60, 3 * 60,
                60, 30, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
        };
        scheduleNotifications(now.plusSeconds(5), now, ChronoUnit.SECONDS.between(now.plusSeconds(5), warTime));

        for (long secondsBefore : notificationTimesInSeconds) {
            scheduleNotifications(warTime.minus(secondsBefore, ChronoUnit.SECONDS), now, secondsBefore);
        }
        scheduleWarStart(now);
    }

    private void scheduleWarStart(ZonedDateTime now) {
        scheduler.schedule(() -> FlagWar.warManager.startWar(war), ChronoUnit.SECONDS.between(now, warTime), TimeUnit.SECONDS);
    }
    private void scheduleNotifications(ZonedDateTime notificationTime, ZonedDateTime now, long timeBefore) {
        long delay = ChronoUnit.SECONDS.between(now, notificationTime);
        if (delay >= 0) {
            scheduler.schedule(() -> sendNotifications(timeBefore), delay, TimeUnit.SECONDS);
        }
    }

    private void scheduleNotification(Player player, String message, ZonedDateTime notificationTime, ZonedDateTime now) {
        long delay = ChronoUnit.SECONDS.between(now, notificationTime);
        if (delay >= 0) {
            scheduler.schedule(() -> sendNotificationToPlayer(player, message, false), delay, TimeUnit.SECONDS);
        }
    }

    private void sendNotifications(long timeBefore) {
        String timeLeftMessage = getTimeFormattedMessage(timeBefore);
        String attackerMessage = Messaging.parsePlaceholders(Messages.timeLeftMessage, war.victim.getName(), timeLeftMessage);
        String victimMessage = Messaging.parsePlaceholders(Messages.timeLeftMessage, war.attacker.getName(), timeLeftMessage);


        sendNotificationToTown(war.attacker, attackerMessage, true);
        sendNotificationToTown(war.victim, victimMessage, false);
    }

    private String getTimeFormattedMessage(long timeBefore) {
        long days = timeBefore / (24 * 3600);
        timeBefore %= 24*3600;
        long hours = timeBefore / 3600;
        timeBefore %= 3600;
        long minutes = timeBefore / 60;
        long seconds = timeBefore % 60;
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
        if (minutes == 0 && seconds > 0) {
            messageBuilder.append(seconds).append(" с. ");
        }
        return  messageBuilder.toString().trim();
    }

    private void sendNotificationToTown(Town town, String message, boolean attacker) {
        // Iterate through each online resident of the town
        for (Resident resident: town.getResidents()) {
            Player player = resident.getPlayer();
            if (player != null && player.isOnline()) {
                // Send action bar message
                player.sendActionBar(Messaging.formatForString(message));
                // Send title message
                player.sendTitle(Messaging.formatForString(attacker? Messages.attackerNotificationTitle : Messages.victimNotificationTitle), Messaging.formatForString(message), 10, 140, 20);
                // Send chat message
                player.sendMessage(Messaging.formatForString(message));
            }
        }
    }

    private void sendNotificationToPlayer(Player player, String message, boolean attacker) {
        // Send action bar message
        player.sendActionBar(Messaging.formatForString(message));
        // Send title message
        player.sendTitle(Messaging.formatForString(attacker? Messages.attackerNotificationTitle : Messages.victimNotificationTitle), Messaging.formatForString(message), 10, 140, 20);
        // Send chat message
        player.sendMessage(Messaging.formatForString(message));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT+3"));
        if (ChronoUnit.SECONDS.between(now, warTime) < 60) return;
        Player player = joinEvent.getPlayer();
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null) return;
        if (war.attacker.equals(town)) {
            String message = Messaging.parsePlaceholders(Messages.timeLeftMessage, war.victim.getName(), getTimeFormattedMessage(ChronoUnit.SECONDS.between(now, warTime)));
            scheduleNotification(player, message, now.plusSeconds(20), now);
        }
        if (war.victim.equals(town)) {
            String message = Messaging.parsePlaceholders(Messages.timeLeftMessage, war.attacker.getName(), getTimeFormattedMessage(ChronoUnit.SECONDS.between(now, warTime)));
            sendNotificationToPlayer(player, message, true);
        }
    }
}
