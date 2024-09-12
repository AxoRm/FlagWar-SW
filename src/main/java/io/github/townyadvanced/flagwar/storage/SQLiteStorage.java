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
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLiteStorage {
    public static Set<NewWar> newWars = new HashSet<>();

    File file;
    public Connection connection;
    FlagWar plugin;
    public SQLiteStorage (String filePath, FlagWar plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), filePath);
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Cannot create database file " + file.getPath() + " Stack:");
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                plugin.getLogger().severe(stackTraceElement.toString());
            }
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+file.getAbsolutePath());
        } catch (SQLException e) {
            plugin.getLogger().severe("Cannot create SQL connection. Stack:");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
        if (connection != null) initDatabase();
    }

    public void initDatabase() {
        try {
            PreparedStatement createNewWarsTableStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `newWars` (`attacker` TEXT NOT NULL, `victim` TEXT NOT NULL, `year` int NOT NULL, `month` int NOT NULL, `day` int NOT NULL, `hour` int NOT NULL);");
            createNewWarsTableStatement.execute();

            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM `newWars`");
            Map<String, String> toDelete = new HashMap<>();
            while (resultSet.next()) {
                Town attacker = TownyAPI.getInstance().getTown(resultSet.getString("attacker"));
                Town victim = TownyAPI.getInstance().getTown(resultSet.getString("victim"));
                int year = resultSet.getInt("year");
                int day = resultSet.getInt("day");
                int month = resultSet.getInt("month");
                int hour = resultSet.getInt("hour");

                if (attacker == null || victim == null || attacker.isRuined() || victim.isRuined()) {
                    toDelete.put(resultSet.getString("attacker"), resultSet.getString("victim"));
                    continue;
                };

                newWars.add(new NewWar(attacker, victim, year, month, day, hour));
            }
            for (Map.Entry<String, String> warEntry : toDelete.entrySet()) {

                String attacker = warEntry.getKey();
                String victim = warEntry.getValue();

                PreparedStatement deleteUnresolvedStatement = connection.prepareStatement("DELETE FROM `newWars` WHERE `attacker` = ? AND `victim` = ?;");
                deleteUnresolvedStatement.setString(1, attacker);
                deleteUnresolvedStatement.setString(2, victim);

                deleteUnresolvedStatement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveNewWar(NewWar newWar) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement saveStatement = FlagWar.storage.connection.prepareStatement("INSERT INTO `newWars` VALUES (?, ?, ?, ?, ?, ?);");
                    saveStatement.setString(1, newWar.attacker.getName());
                    saveStatement.setString(2, newWar.victim.getName());
                    saveStatement.setInt(3, newWar.year);
                    saveStatement.setInt(4, newWar.month);
                    saveStatement.setInt(5, newWar.day);
                    saveStatement.setInt(6, newWar.hour);
                    saveStatement.execute();
                } catch (SQLException ignored) {
                }
            }
        }.runTaskAsynchronously(FlagWar.getFlagWar());
    }

    public static void deleteNewWar(NewWar newWar) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement deleteStatement = FlagWar.storage.connection.prepareStatement("DELETE FROM `newWars` WHERE `attacker` = ? AND `victim` = ?;");
                    deleteStatement.setString(1, newWar.attacker.getName());
                    deleteStatement.setString(2, newWar.victim.getName());
                    deleteStatement.execute();
                } catch (SQLException ignored) {}
            }
        }.runTaskAsynchronously(FlagWar.getFlagWar());
    }


}
