package io.github.townyadvanced.flagwar.war;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.newconfig.Messages;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class BossBarManager implements Listener {
    BossBar attackerBossBar;
    BossBar defenderBossBar;
    WarProcess process;
    List<String> attackers;
    List<String> defenders;
    List<String> online;
    List<String> wasOnline;
    Plugin plugin = FlagWar.getInstance();
    private final String[] attackerMessages = {
            "Осталось чанков до капитуляции: {0}",
            "Процент капитуляции противника: {1}%",
            "Время до подсчета: {2}",
            "Если вы не захватите {3}/{4} чанков, то вы проиграете",
            "Время войны: {5} / 5 часов"
    };

    private final String[] defenderMessages = {
            "Осталось чанков до капитуляции: {0}",
            "Вы близки к капитуляции на: {1}%",
            "Время до подсчета: {2}",
            "Если противник не захватит {3}/{4}, то вы выиграете досрочно за 1 час",
            "Время войны: {5} / 5 часов"
    };

    private int attackerMessageIndex = 0;
    private int defenderMessageIndex = 0;

    public BossBarManager(WarProcess process) {
        this.process = process;
        attackers = process.playerAggressors;
        defenders = process.playerDefenders;
        online = process.online;
        wasOnline = process.wasOnline;
        attackerBossBar = createAttackerBossBar();
        defenderBossBar = createDefenderBossBar();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (String playerName : attackers) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) attackerBossBar.addPlayer(player);
        }
        for (String playerName : defenders) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) defenderBossBar.addPlayer(player);
        }
        startBossBarUpdater();
        startBossBarTextUpdater();
    }
    private BossBar createAttackerBossBar() {
        BossBar bossBar = Bukkit.createBossBar("temporary title", BarColor.GREEN, BarStyle.SEGMENTED_20);
        bossBar.setVisible(true);
        return bossBar;
    }

    private BossBar createDefenderBossBar() {
        BossBar bossBar = Bukkit.createBossBar("temporary title", BarColor.RED, BarStyle.SEGMENTED_20);
        bossBar.setVisible(true);
        return bossBar;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player joinedPlayer = joinEvent.getPlayer();
        if (attackers.contains(joinedPlayer.getName())) {
            attackerBossBar.addPlayer(joinedPlayer);
        } else if (defenders.contains(joinedPlayer.getName())) {
            defenderBossBar.addPlayer(joinedPlayer);
        }
    }

    private void startBossBarUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateBossBarProgress();
            }
        }.runTaskTimer(plugin, 0, 5); // Update every 0.25 seconds (5 ticks)
    }

    public void clearBossBars() {
        attackerBossBar.removeAll();
        defenderBossBar.removeAll();
    }

    private void updateBossBarProgress() {
        double percentage = process.calculateCapitulationPercentage()/100;
        attackerBossBar.setProgress(percentage);
        defenderBossBar.setProgress(percentage);
    }

    private void startBossBarTextUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateBossBarText();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void updateBossBarText() {
        int totalChunks = process.warChunks.size();
        int capturedChunks = process.aggressorWonChunks.size();
        double capitulationThreshold = process.isSpawnCaptured ? 0.5 : 0.75;
        double currentProgress = (double) capturedChunks / totalChunks;
        int chunksNeededForVictory = (int) Math.ceil(totalChunks * capitulationThreshold) - capturedChunks;
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("GMT+3"));
        long warDurationInSeconds = Duration.between(process.startTime, currentTime).getSeconds();
        if (currentTime.isAfter(process.nextCheckTime)) {
            process.nextCheckTime = process.nextCheckTime.plus(Duration.ofHours(1));
        }
        int nextHour = process.currentHour + 1;
        int neededChunksForNoLoose = (int) ((double) 15*nextHour/100 * process.warChunks.size());
        long timeUntilNextCheckInSeconds = Duration.between(currentTime, process.nextCheckTime).getSeconds();
        String attackerText = Messaging.parsePlaceholders(Messaging.formatForString(attackerMessages[attackerMessageIndex / 5]), String.valueOf(chunksNeededForVictory), String.valueOf(Math.floor(process.calculateCapitulationPercentage())), process.formatTime(timeUntilNextCheckInSeconds), String.valueOf(capturedChunks), String.valueOf(neededChunksForNoLoose), process.formatTime(warDurationInSeconds));
        attackerBossBar.setTitle(attackerText);
        attackerMessageIndex = (attackerMessageIndex + 1) % (attackerMessages.length*5);

        String defenderText = Messaging.parsePlaceholders(Messaging.formatForString(defenderMessages[defenderMessageIndex / 5]), String.valueOf(chunksNeededForVictory), String.valueOf(Math.floor(process.calculateCapitulationPercentage())), process.formatTime(timeUntilNextCheckInSeconds), String.valueOf(capturedChunks), String.valueOf(neededChunksForNoLoose), process.formatTime(warDurationInSeconds));
        defenderBossBar.setTitle(defenderText);
        defenderMessageIndex = (defenderMessageIndex + 1) % (defenderMessages.length*5);
    }

}
