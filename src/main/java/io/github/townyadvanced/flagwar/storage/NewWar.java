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

import com.palmergames.bukkit.towny.object.Town;

public class NewWar {
    public Town attacker;
    public Town victim;
    public int year;
    public int day;
    public int month;
    public int hour;

    public NewWar(Town attacker, Town victim, int year, int month, int day, int hour) {
        this.attacker = attacker;
        this.victim = victim;
        this.year = year;
        this.day = day;
        this.month = month;
        this.hour = hour;
    }
}
