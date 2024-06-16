/*
 * Copyright (c) 2024 TownyAdvanced
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.townyadvanced.flagwar.storage;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NewWar implements Listener {
    public Town attacker;
    public Town victim;
    public List<Player> playersAttacker;
    public List<Player> playersVictim;
    public int year;
    public int day;
    public int month;
    public int hour;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public NewWar(Town attacker, Town victim, int year, int month, int day, int hour) {
        this.attacker = attacker;
        this.victim = victim;
        this.year = year;
        this.day = day;
        this.month = month;
        this.hour = hour;
        this.playersAttacker = attacker.getResidents().stream().map(Resident::getPlayer).toList();
        this.playersVictim = victim.getResidents().stream().map(Resident::getPlayer).toList();
    }

    private void sendNotificationToPlayer(Player player, String message) {
        if (player != null && player.isOnline()) {
            // Send action bar message
            player.sendActionBar(Messaging.formatForString(message));
            // Send title message
            player.sendTitle(Messaging.formatForString("&cВнимание! Вам объявили войну!"), Messaging.formatForString(message), 10, 140, 20);
            // Send chat message
            player.sendMessage(Messaging.formatForString(message));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        this.playersAttacker = attacker.getResidents().stream().map(Resident::getPlayer).toList();
        this.playersVictim = victim.getResidents().stream().map(Resident::getPlayer).toList();
        Player player = joinEvent.getPlayer();
        if (playersAttacker.contains(player)) {
            scheduleNotification();
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
}
