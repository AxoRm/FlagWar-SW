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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NewWar implements Listener {
    public ZonedDateTime warTime;
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
        warTime = ZonedDateTime.of(year, month, day, hour, 0, 0, 0, ZoneId.of("GMT+3"));
        this.playersAttacker = attacker.getResidents().stream().map(Resident::getPlayer).toList();
        this.playersVictim = victim.getResidents().stream().map(Resident::getPlayer).toList();
    }

    public NewWar(Town attacker, Town victim, ZonedDateTime time) {
        warTime = time;
        this.attacker = attacker;
        this.victim = victim;
        this.playersAttacker = attacker.getResidents().stream().map(Resident::getPlayer).toList();
        this.playersVictim = victim.getResidents().stream().map(Resident::getPlayer).toList();
    }

    public Town getAttacker() {
        return attacker;
    }

    public Town getVictim() {
        return victim;
    }
}
