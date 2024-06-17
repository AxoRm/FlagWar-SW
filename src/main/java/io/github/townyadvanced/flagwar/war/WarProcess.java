package io.github.townyadvanced.flagwar.war;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamageEntityEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.events.CellWonEvent;
import io.github.townyadvanced.flagwar.objects.Cell;
import io.github.townyadvanced.flagwar.storage.NewWar;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WarProcess implements Listener {
    TownyAPI towny = TownyAPI.getInstance();
    ZonedDateTime startTime;
    int respawns = 0;
    Town aggressorTown;
    Town defenderTown;
    List<Resident> aggressors;
    List<Resident> defenders;
    List<Resident> participants;
    List<Player> playerAggressors;
    List<Player> playerDefenders;
    List<Player> playerParticipants;
    Set<Chunk> warChunks;
    Set<Chunk> sideWarChunks;
    Set<Chunk> allChunks;

    Set<Chunk> warMainChunks;
    Set<Chunk> sideWarMainChunks;

    List<Player> online;
    List<Player> wasOnline;
    String warAggressorPrefix = "&c◆";
    String warAggressorPostfix = "&c◆";
    String warDefenderPrefix = "&9■";
    String warDefenderPostfix = "&9■";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Player, BukkitTask> teleportTasks = new HashMap<>();
    Location spawnLocation;

    public WarProcess(NewWar war) {
        this.defenderTown = war.victim;
        this.aggressorTown = war.attacker;
        startTime = war.warTime;
        this.aggressors = new ArrayList<>(aggressorTown.getResidents());
        this.defenders = new ArrayList<>(defenderTown.getResidents());
        this.participants = new ArrayList<>();
        this.participants.addAll(aggressors);
        this.participants.addAll(defenders);

        this.playerAggressors = aggressors.stream().map(Resident::getPlayer).toList();
        this.playerDefenders = defenders.stream().map(Resident::getPlayer).toList();
        this.playerParticipants = new ArrayList<>();
        this.playerParticipants.addAll(playerAggressors);
        this.playerParticipants.addAll(playerDefenders);

        online = participants.stream().filter(Resident::isOnline).map(Resident::getPlayer).toList();
        wasOnline = participants.stream().filter(Resident::isOnline).map(Resident::getPlayer).toList();
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getInstance());

        this.warChunks = new HashSet<>();
        this.sideWarChunks = new HashSet<>();
        this.allChunks = new HashSet<>();
        Location spawn = null;
        try {
            spawn = defenderTown.getSpawn();
        } catch (TownyException e) {
            //TODO: create auto spawn chooser or not allow war if spawn deleted.
        }
        assert spawn != null;
        Chunk spawnChunk = spawn.getChunk();

        // BFS для нахождения всех чанков, принадлежащих городу
        Queue<Chunk> queue = new LinkedList<>();
        Set<Chunk> visited = new HashSet<>();
        queue.add(spawnChunk);
        visited.add(spawnChunk);

        while (!queue.isEmpty()) {
            Chunk currentChunk = queue.poll();
            warMainChunks.add(currentChunk);

            int chunkX = currentChunk.getX();
            int chunkZ = currentChunk.getZ();
            World world = currentChunk.getWorld();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue; // Пропускаем текущий чанк
                    Chunk adjacentChunk = world.getChunkAt(chunkX + dx, chunkZ + dz);

                    if (!visited.contains(adjacentChunk)) {
                        WorldCoord coord = new WorldCoord(world.getName(), chunkX + dx, chunkZ + dz);
                        TownBlock townBlock = coord.getTownBlockOrNull();
                        if (townBlock != null && townBlock.getTownOrNull() == defenderTown) {
                            queue.add(adjacentChunk);
                        } else {
                            // Если это граничный чанк, добавляем его в sideWarMainChunks
                            sideWarMainChunks.add(adjacentChunk);
                        }
                        visited.add(adjacentChunk);
                    }
                }
            }
        }

        // Добавляем чанки в радиусе 2 чанков от граничных чанков города
        Set<Chunk> additionalSideChunks = new HashSet<>();
        for (Chunk sideChunk : sideWarMainChunks) {
            int chunkX = sideChunk.getX();
            int chunkZ = sideChunk.getZ();
            World world = sideChunk.getWorld();

            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    Chunk adjacentChunk = world.getChunkAt(chunkX + dx, chunkZ + dz);
                    if (!warMainChunks.contains(adjacentChunk) && !sideWarMainChunks.contains(adjacentChunk)) {
                        additionalSideChunks.add(adjacentChunk);
                    }
                }
            }
        }
        sideWarMainChunks.addAll(additionalSideChunks);
        // Add all chunks of the defender town to warChunks
        Set<WorldCoord> townCoords = new HashSet<>();
        for (TownBlock townBlock : defenderTown.getTownBlocks()) {
            World world = Bukkit.getWorld(townBlock.getWorld().getName());
            if (world != null) {
                Chunk chunk = world.getChunkAt(townBlock.getX(), townBlock.getZ());
                warChunks.add(chunk);
                townCoords.add(townBlock.getWorldCoord());
            }
        }
        // Add chunks around the town that are within 2 chunks distance to sideWarChunks
        for (WorldCoord coord : townCoords) {
            World world = Bukkit.getWorld(coord.getWorldName());
            if (world != null) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        WorldCoord adjacentCoord = new WorldCoord(coord.getWorldName(), coord.getX() + x, coord.getZ() + z);
                        if (!townCoords.contains(adjacentCoord)) {
                            Chunk adjacentChunk = world.getChunkAt(adjacentCoord.getX(), adjacentCoord.getZ());
                            sideWarChunks.add(adjacentChunk);
                        }
                    }
                }
            }
        }
        // Combine warChunks and sideWarChunks into allChunks
        allChunks.addAll(warChunks);
        allChunks.addAll(sideWarChunks);
        this.spawnLocation = calculateWarStartLocation();
        startWar();
    }

    private void startWar() {
        for (Player player : playerAggressors) {
            if (!player.isOnline()) continue;
            player.teleport(spawnLocation);
            player.sendMessage("Вы телепортированы на поле боя!");
        }
    }

    private Location calculateWarStartLocation() {
        for (Chunk chunk : sideWarMainChunks) {
            if (isOnBorder(chunk)) {
                if (hasNoEnemyPlayersNearby(chunk)) {
                    // Return the center of the chunk as the start location
                    World world = chunk.getWorld();
                    int x = chunk.getX() * 16 + 8;
                    int z = chunk.getZ() * 16 + 8;
                    int y = world.getHighestBlockYAt(x, z);
                    return new Location(world, x, y+1, z);
                }
            }
        }
        return null; // No suitable location found
    }

    private boolean isOnBorder(Chunk chunk) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        World world = chunk.getWorld();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // Skip the current chunk
                Chunk adjacentChunk = world.getChunkAt(chunkX + dx, chunkZ + dz);
                if (!sideWarChunks.contains(adjacentChunk) && !warChunks.contains(adjacentChunk)) {
                    return true; // Found a border chunk
                }
            }
        }
        return false;
    }

    private boolean hasNoEnemyPlayersNearby(Chunk chunk) {
        int centerX = chunk.getX() * 16 + 8;
        int centerZ = chunk.getZ() * 16 + 8;
        World world = chunk.getWorld();

        for (Player player : world.getPlayers()) {
            if (isEnemyPlayer(player)) {
                Location playerLocation = player.getLocation();
                int distance = Math.max(Math.abs(centerX - playerLocation.getBlockX()), Math.abs(centerZ - playerLocation.getBlockZ()));
                if (distance <= 40) { // 5 chunks distance
                    return false; // Found an enemy player nearby
                }
            }
        }
        return true;
    }

    private boolean isEnemyPlayer(Player player) {
        Resident resident = towny.getResident(player);
        if (resident == null) return false;
        Town playerTown = resident.getTownOrNull();
        return playerTown != null && defenderTown == playerTown;
    }

    public void startScheduledMessage(Player player) {
        // Send message immediately
        player.sendTitle("Вы возродились!", "Используйте команду /return, чтобы вернуться на поле боя", 10, 70, 20);

        // Schedule message every 5 minutes
        scheduler.scheduleAtFixedRate(() -> {
            if (player.isOnline()) {
                player.sendTitle("Не забывайте!", "Вы можете вернуться на поле боя командой /return", 10, 70, 20);
            }
        }, 5, 5, TimeUnit.MINUTES);

        // Schedule teleport after 10 minutes if not returned
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                teleportToWarSpawn(player);
            }
        }.runTaskLater(FlagWar.getInstance(), 20 * 60 * 10); // 10 minutes in ticks

        teleportTasks.put(player, task);
    }

    public void teleportToWarSpawn(Player player) {
        if (player.isOnline()) {
            player.teleport(spawnLocation);
            player.sendTitle("Телепортация", "Вы были возвращены на поле боя", 10, 40, 20);
        }
    }

    public void scanSurround(Town town, int chunkX, int chunkZ, String worldName) {
        Set<WorldCoord> opponentCoords = new HashSet<>();
        Town opponentTown = (town.equals(aggressorTown)) ? defenderTown : aggressorTown;
        for (TownBlock townBlock : opponentTown.getTownBlocks()) {
            opponentCoords.add(townBlock.getWorldCoord());
        }

        // Check surrounding chunks
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    }

    //EVENTS

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackEvent(TownyPlayerDamagePlayerEvent damageEvent) {
        Town attackerTown = damageEvent.getAttackerTown();
        Town victimTown = damageEvent.getVictimTown();
        if (attackerTown == null || victimTown == null) return;
        Resident attackingResident = damageEvent.getAttackingResident();
        Resident victimResident = damageEvent.getVictimResident();
        if (attackingResident == null || victimResident == null) return;
        if (participants.contains(attackingResident) && participants.contains(victimResident)) {
            if (attackerTown.equals(victimTown)) {
                damageEvent.setCancelled(true);
                return;
            }
            Player attacker = attackingResident.getPlayer();
            if (attacker == null) return;
            if (allChunks.contains(attacker.getChunk())) {
                damageEvent.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackEvent(TownyPlayerDamageEntityEvent damageEvent) {
        Player attacker = damageEvent.getAttackingPlayer();
        if (attacker == null) return;
        if (participants.contains(towny.getResident(attacker))) {
            if (allChunks.contains(damageEvent.getEntity().getChunk())) {
                damageEvent.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExplosionDamagesEvent(TownyExplosionDamagesEntityEvent explosionDamagesEvent) {
        if (!explosionDamagesEvent.isCancelled()) return;
        Chunk chunk = explosionDamagesEvent.getLocation().getChunk();
        if (allChunks.contains(chunk)) explosionDamagesEvent.setCancelled(false);
    }

    @EventHandler
    public void onRespawnEvent(PlayerRespawnEvent respawnEvent) {
        Resident resident = towny.getResident(respawnEvent.getPlayer());
        Town town = towny.getTown(respawnEvent.getPlayer());
        if (town == null || !town.equals(aggressorTown)) return;
        if (!participants.contains(resident)) return;
        startScheduledMessage(respawnEvent.getPlayer());
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent joinEvent) {
        Resident resident = towny.getResident(joinEvent.getPlayer());
        if (!participants.contains(resident)) return;
        online.add(joinEvent.getPlayer());
        String message;
        if (wasOnline.contains(joinEvent.getPlayer())) message = "Вы были возвращены на поле боя";
        else message = "Вы были телепортированы на поле боя";
        // Schedule teleport after 5 minutes if not returned
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (joinEvent.getPlayer().isOnline()) {
                    joinEvent.getPlayer().teleport(spawnLocation);
                    joinEvent.getPlayer().sendTitle("Телепортация", message, 10, 70, 20);
                }
            }
        }.runTaskLater(FlagWar.getInstance(), 20 * 60 * 5); // 5 minutes in ticks

        teleportTasks.put(joinEvent.getPlayer(), task);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent deathEvent) {
        Player player = deathEvent.getEntity();
        startScheduledMessage(player);
    }

    @EventHandler
    public void onCellWonEvent(CellWonEvent cellWonEvent) {
        Player player = cellWonEvent.getCellOwner();
        Cell cell = cellWonEvent.getCellUnderAttack();
        int chunkX = cell.getX();
        int chunkZ = cell.getZ();
         //scanSurround(); // Assuming player has a method getTown(), adjust if necessary
    }
    @EventHandler
    public void onQuitEvcent(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        if (online.contains(player)) {
            online.remove(player);
            Objects.requireNonNull(towny.getTown(player)).getMayor().getPlayer().sendMessage("Милорд, школьник с ником + " + player.getName() + " покинул битву!");
        }
    }
}
