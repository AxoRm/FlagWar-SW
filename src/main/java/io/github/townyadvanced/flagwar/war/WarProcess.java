package io.github.townyadvanced.flagwar.war;

import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.events.CellAttackCanceledEvent;
import io.github.townyadvanced.flagwar.events.CellAttackEvent;
import io.github.townyadvanced.flagwar.events.CellWonEvent;
import io.github.townyadvanced.flagwar.i18n.Translate;
import io.github.townyadvanced.flagwar.newconfig.Messages;
import io.github.townyadvanced.flagwar.objects.Cell;
import io.github.townyadvanced.flagwar.storage.NewWar;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WarProcess implements Listener {
    TownyAPI towny = TownyAPI.getInstance();
    ZonedDateTime startTime;

    public int currentHour = 0;

    public int attackersActiveFlags = 0;
    public int defendersActiveFlags = 0;
    private int rotationIndex = 0;
    private BossBarManager bossBarManager;
    ZonedDateTime nextCheckTime;
    Town aggressorTown;

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setAggressorAlly(boolean aggressorAlly) {
        this.aggressorAlly = aggressorAlly;
    }

    public void setDefenderAlly(boolean defenderAlly) {
        this.defenderAlly = defenderAlly;
    }

    public boolean isDefenderAlly() {
        return defenderAlly;
    }

    public boolean isAggressorAlly() {
        return aggressorAlly;
    }

    public Town getAggressorTown() {
        return aggressorTown;
    }

    public Town getDefenderTown() {
        return defenderTown;
    }

    Town defenderTown;
    List<Resident> aggressors;
    List<Resident> defenders;
    List<Resident> participants;
    List<String> playerAggressors;
    List<String> playerDefenders;
    List<Resident> mayorsAgg = new ArrayList<>();
    List<Resident> mayorsDef = new ArrayList<>();
    List<String> playerParticipants;

    public Set<Chunk> getAllChunks() {
        return allChunks;
    }

    Set<Chunk> warChunks;
    Set<Chunk> sideWarChunks;
    Set<Chunk> allChunks;

    boolean aggressorAlly = false;
    boolean defenderAlly = false;

    Set<Resident> kickResidents;

    Set<Chunk> warMainChunks;
    Set<Chunk> sideWarMainChunks;
    Set<Chunk> attackChunks;

    Set<Chunk> aggressorWonChunks;

    int initialSize;

    TownBlock spawn;
    Chunk spawnChunk;
    Chunk newSpawnChunk;
    ArrayList<String> online;
    List<String> wasOnline;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Player, BukkitRunnable> teleportTasks = new HashMap<>();
    private final Map<Player, BukkitRunnable> notificationTasks = new HashMap<>();
    Location spawnLocation;
    boolean isSpawnCaptured = false;
    NewWar war;
    public WarProcess(NewWar war) {
        this.war = war;
        //Bukkit.getLogger().info("Here");
        this.defenderTown = war.victim;
        this.aggressorTown = war.attacker;
        initialSize = defenderTown.getTownBlocks().size();
        startTime = ZonedDateTime.now( ZoneId.of("GMT+3"));
        this.aggressors = new ArrayList<>(aggressorTown.getResidents());
        this.defenders = new ArrayList<>(defenderTown.getResidents());

        aggressorTown.addEnemy(defenderTown);
        defenderTown.addEnemy(aggressorTown);

        this.participants = new ArrayList<>();
        this.participants.addAll(aggressors);
        this.participants.addAll(defenders);
        this.kickResidents = new HashSet<>();
        this.playerAggressors = aggressors.stream().map(Resident::getName).toList();
        this.playerDefenders = defenders.stream().map(Resident::getName).toList();
        this.playerParticipants = new ArrayList<>();
        this.playerParticipants.addAll(playerAggressors);
        this.playerParticipants.addAll(playerDefenders);

        online = new ArrayList<>(participants.stream().filter(Resident::isOnline).map(Resident::getName).toList());
        wasOnline = new ArrayList<>(participants.stream().filter(Resident::isOnline).map(Resident::getName).toList());
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getInstance());
        //Bukkit.getLogger().info("Here");
        this.warChunks = new HashSet<>();
        this.sideWarChunks = new HashSet<>();
        this.allChunks = new HashSet<>();
        this.attackChunks = new HashSet<>();
        this.aggressorWonChunks = new HashSet<>();
        spawn = null;
        try {
            spawn = defenderTown.getHomeBlock();
        } catch (TownyException e) {
            //TODO: create auto spawn chooser or not allow war if spawn deleted.
        }
        if (spawn == null) {
            //Bukkit.getLogger().info("");
            return;
        }
        spawnChunk = Objects.requireNonNull(spawn.getWorld().getBukkitWorld()).getChunkAt(spawn.getX(), spawn.getZ());
        newSpawnChunk = Objects.requireNonNull(spawn.getWorld().getBukkitWorld()).getChunkAt(spawn.getX(), spawn.getZ());
        // TODO: Скорее всего нужно будет переместить в асихрон
        findWarChunksAsync().thenCompose(unused -> calculateWarStartLocationAsync()).thenAccept(location -> {
            this.spawnLocation = location;
            startWar();
        }).exceptionally(ex -> {
            Bukkit.getLogger().severe("Failed to initialize war chunks or calculate start location: " + ex.getMessage());
            return null;
        });
        nextCheckTime = startTime.plus(Duration.ofHours(1));
        for (int hour = 1; hour <= 5; hour ++) {
            int finalHour = hour;
            scheduler.schedule(() -> handleWarFinish(finalHour), hour, TimeUnit.HOURS);
        }
        bossBarManager = new BossBarManager(this);
    }

    private void handleWarFinish(int x) {
        currentHour = x;
        if ((double) aggressorWonChunks.size() / warChunks.size() < (double) 15*x/100) {
            looseProcess();
            bossBarManager.clearBossBars();
        }
    }

    private CompletableFuture<Void> findWarChunksAsync() {
        return CompletableFuture.runAsync(() -> {
            Queue<Chunk> queue = new LinkedList<>();
            Set<Chunk> visited = new HashSet<>();
            warMainChunks = new HashSet<>();
            sideWarMainChunks = new HashSet<>();
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
        });
    }

    public void summonAllies(Player player) { //TODO: реализовать механику союзников
        if (playerAggressors.contains(player.getName())) {
            if (!aggressorTown.hasNation()) return;
            try {
                aggressorTown.getNation().getTowns().forEach(town -> {
                    town.getMayor().sendMessage(Component.text("Вас пригласили на участие в войне! Пропишите команду /ally accept если хотите это сделать!"));
                    mayorsAgg.add(town.getMayor());
                });
            } catch (NotRegisteredException e) {
                throw new RuntimeException(e);
            }
        }
        if (playerAggressors.contains(player.getName())) {
            if (!defenderTown.hasNation()) return;
            try {
                defenderTown.getNation().getTowns().forEach(town -> {
                    town.getMayor().sendMessage(Component.text("Вас пригласили на участие в войне! Пропишите команду /ally accept если хотите это сделать!"));
                    mayorsDef.add(town.getMayor());
                });
            } catch (NotRegisteredException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addAlly(Town town, Resident resident) {
        if (mayorsDef.contains(resident)) {
        }
    }

    private void startWar() {
        for (String playerName : playerAggressors) {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null || !player.isOnline()) continue;
            player.teleport(spawnLocation);
            player.sendMessage(Messages.teleportBattleMessage);

            player.sendTitle(Messaging.formatForString(Messages.warStartedTitle), Messaging.formatForString(Messages.warStartedSubTitleAttackers), 10, 180, 20);
        }
        for (String playerName: playerDefenders) {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null || !player.isOnline()) continue;
            player.sendTitle(Messaging.formatForString(Messages.warStartedTitle), Messaging.formatForString(Messages.warStartedSubTitleDefenders),  10, 180, 20);
        }
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public CompletableFuture<Location> calculateWarStartLocationAsync() {
        return CompletableFuture.supplyAsync(() -> {
            for (Chunk chunk : sideWarMainChunks) {
                if (isOnBorder(chunk)) {
                    if (hasNoEnemyPlayersNearby(chunk)) {
                        // Return the center of the chunk as the start location
                        World world = chunk.getWorld();
                        int x = chunk.getX() * 16 + 8;
                        int z = chunk.getZ() * 16 + 8;
                        int y = world.getHighestBlockYAt(x, z);
                        return new Location(world, x, y + 1, z);
                    }
                }
            }
            return null; // No suitable location found
        });
    }

    public String getPrefix(Town town) {
        if (town.equals(this.aggressorTown)) {
            return "&c◆";
        }
        if (town.equals(this.defenderTown)) {
            return "&9■";
        }
        return "";
    }

    public String getStatus(Town town) {
        int totalChunks = warChunks.size();
        int capturedChunks = aggressorWonChunks.size();
        double capitulationThreshold = isSpawnCaptured ? 0.5 : 0.75;
        double currentProgress = (double) capturedChunks / totalChunks;
        int chunksNeededForVictory = (int) Math.ceil(totalChunks * capitulationThreshold);
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("GMT+3"));

        // Calculate war duration and time until next check
        long warDurationInSeconds = Duration.between(startTime, currentTime).getSeconds();

        if (currentTime.isAfter(nextCheckTime)) {
            nextCheckTime = nextCheckTime.plus(Duration.ofHours(1));
        }

        long timeUntilNextCheckInSeconds = Duration.between(currentTime, nextCheckTime).getSeconds();

        // Rotate through different messages
        rotationIndex = (rotationIndex + 1) % 4;
        return switch (rotationIndex) {
            case 0 -> "War Duration: " + formatTime(warDurationInSeconds);
            case 1 ->
                    "Captured Chunks: " + capturedChunks + "/" + totalChunks + " (" + String.format("%.2f%%", currentProgress * 100) + ")";
            case 2 -> "Chunks Needed for Victory: " + chunksNeededForVictory;
            case 3 -> "Time Until Next Check: " + formatTime(timeUntilNextCheckInSeconds);
            default -> "War Status";
        };
    }

    String formatTime(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        long hours = mins / 60;
        mins = mins % 60;
        long days = hours / 24;
        hours = hours % 24;

        StringBuilder time = new StringBuilder();
        if (days > 0) time.append(days).append("д ");
        if (hours > 0 || days > 0) time.append(hours).append("ч ");
        if (mins > 0 || hours > 0 || days > 0) time.append(mins).append("м ");
        time.append(secs).append("с");

        return time.toString();
    }

    public String getSuffix(Town town) {
        if (town.equals(this.aggressorTown)) {
            return "&c◆";
        }
        if (town.equals(this.defenderTown)) {
            return "&9■";
        }
        return "";
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

    public boolean hasNoEnemyPlayersNearby(Chunk chunk) {
        int centerX = chunk.getX() * 16 + 8;
        int centerZ = chunk.getZ() * 16 + 8;
        World world = chunk.getWorld();

        for (Player player : world.getPlayers()) {
            if (isEnemyPlayer(player)) {
                Location playerLocation = player.getLocation();
                int distance = Math.max(Math.abs(centerX - playerLocation.getBlockX()), Math.abs(centerZ - playerLocation.getBlockZ()));
                if (distance <= 40) { // 2.5 chunk distance
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
        player.sendTitle(Messaging.formatForString(Messaging.formatForString(Messages.respawnTitle)), Messaging.formatForString(Messages.respawnSubTitle), 10, 140, 20);

        // Schedule message every 5 minutes
        BukkitRunnable notificationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.sendTitle(Messaging.formatForString(Messages.reminderTitle), Messaging.formatForString(Messages.reminderSubTitle), 10, 140, 20);
                }
            }
        };
        notificationTask.runTaskTimer(FlagWar.getInstance(), 5 * 60 * 20, 5 * 60 * 20); // Every 5 minutes in ticks

        // Schedule teleport after 10 minutes if not returned
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                teleportToWarSpawn(player);
                player.sendMessage(Messaging.formatForComponent(Messages.autoTeleportMessage));
            }
        };
        task.runTaskLater(FlagWar.getInstance(), 20 * 60 * 10); // 10 minutes in ticks

        notificationTasks.put(player, notificationTask);
        teleportTasks.put(player, task);
    }

    public void teleportToWarSpawn(Player player) {
        if (!player.isOnline()) {
            return;
        }

        Runnable teleportAndNotify = () -> {
            player.teleport(spawnLocation);
            player.sendTitle(Messaging.formatForString(Messages.returnBattleTitle),
                    Messaging.formatForString(Messages.returnBattleSubTitle),
                    10, 140, 20);
            cancelAndRemoveTask(notificationTasks, player);
            cancelAndRemoveTask(teleportTasks, player);
        };

        if (hasNoEnemyPlayersNearby(getSpawnLocation().getChunk())) {
            teleportAndNotify.run();
        } else {
            calculateWarStartLocationAsync().thenAccept(location -> {
                this.spawnLocation = location;
                teleportAndNotify.run();
            }).exceptionally(ex -> {
                Bukkit.getLogger().severe("Failed to initialize war chunks or calculate start location: " + ex.getMessage());
                return null;
            });
        }
    }

    private void cancelAndRemoveTask(Map<Player, ? extends BukkitRunnable> taskMap, Player player) {
        if (taskMap.containsKey(player)) {
            taskMap.get(player).cancel();
        }
        taskMap.remove(player);
    }


    public void scanSurround(Chunk chunk) {
        if (aggressorWonChunks.size() < 18) return;
        World world = chunk.getWorld();
        Town attackerTown = null;
        try {
            attackerTown = new WorldCoord(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()).getTownBlock().getTown();
        } catch (TownyException e) {
            e.printStackTrace();
        }
        Town defenderTown;
        if (attackerTown == this.aggressorTown)
            defenderTown = this.defenderTown;
        else defenderTown = this.aggressorTown;
        //assert attackerTown != null;
        //Bukkit.getLogger().info("Chunk win: " + attackerTown.getName());
        List<Chunk> enemyChunks = new ArrayList<>();
        if (attackerTown == null) return;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int newX = chunk.getX() + dir[0];
            int newZ = chunk.getZ() + dir[1];
            Chunk adjacentChunk = world.getChunkAt(newX, newZ);
            TownBlock adjacentTownBlock = new WorldCoord(world.getName(), newX, newZ).getTownBlockOrNull();
            if (adjacentTownBlock == null) continue;
            Town adjacentTown = adjacentTownBlock.getTownOrNull();
            if (adjacentTown != null && !adjacentTown.equals(attackerTown)) {
                enemyChunks.add(adjacentChunk);
            }
        }

        for (Chunk enemyChunk : enemyChunks) {
            //Bukkit.getLogger().info("EnemyChunk: " + enemyChunk.getX() + " " + enemyChunk.getZ());
            Queue<Chunk> queue = new LinkedList<>();
            Set<Chunk> visitedChunks = new HashSet<>();
            queue.add(enemyChunk);
            visitedChunks.add(enemyChunk);
            boolean flag = true;
            while (!queue.isEmpty()) {
                Chunk currentChunk = queue.poll();
                // Perform the BFS for adjacent enemy chunks
                for (int[] dir : directions) {
                    int newX = currentChunk.getX() + dir[0];
                    int newZ = currentChunk.getZ() + dir[1];
                    Chunk adjacentChunk = world.getChunkAt(newX, newZ);
                    TownBlock adjacentTownBlock = new WorldCoord(world.getName(), newX, newZ).getTownBlockOrNull();
                    if (visitedChunks.contains(adjacentChunk)) continue;
                    if (adjacentTownBlock == null) flag = false;
                    Town adjacentTown = adjacentTownBlock.getTownOrNull();
                    if (adjacentTown == null) flag = false;
                    if (adjacentTown != null && !adjacentTown.equals(attackerTown) && !visitedChunks.contains(adjacentChunk)) {
                        //Bukkit.getLogger().info("Founded chunk: " + adjacentChunk.getX() + " " + adjacentChunk.getZ());
                        visitedChunks.add(adjacentChunk);
                        queue.add(adjacentChunk);
                        // Check if the number of found chunks exceeds 4
                        if (visitedChunks.size() > 4) {
                            break; // Cancel the surrounding and continue with the next enemy chunk
                        }
                    }
                }
            }
                // If less than 4 enemy chunks are found, transfer them to attackerTown
            if (visitedChunks.size() <= 4 && flag) {
                for (Chunk foundChunk : visitedChunks) {
                    //Bukkit.getLogger().info("--------------------------------------------------------");
                    //Bukkit.getLogger().info("chunkX: " + foundChunk.getX() + " z: " + foundChunk.getZ());
                    WorldCoord worldCoord = new WorldCoord(world.getName(), foundChunk.getX(), foundChunk.getZ());
                    TownBlock foundTownBlock = worldCoord.getTownBlockOrNull();
                    if (foundTownBlock != null) {
                        Town foundTown = foundTownBlock.getTownOrNull();
                        if (foundTown != null) {
                            // Defender loses townblock
                            transferOrUnclaimOrKeepTownblock(attackerTown, foundTownBlock, foundTown);
                            // Cleanup
                            Towny.getPlugin().updateCache(worldCoord);
                        }
                    }
                }
                for (Resident resident : attackerTown.getResidents()) {
                    if (!resident.isOnline()) continue;
                    resident.sendMessage(Component.text(Messaging.formatForString(Messaging.parsePlaceholders(Messages.cauldronNotificationAttacker, String.valueOf(visitedChunks.size())))));
                }
                for (Resident resident : defenderTown.getResidents()) {
                    if (!resident.isOnline()) continue;
                    resident.sendMessage(Component.text(Messaging.formatForString(Messaging.parsePlaceholders(Messages.cauldronNotificationDefender, String.valueOf(visitedChunks.size())))));
                }
            }
        }
    }

    private void transferOrUnclaimOrKeepTownblock(final Town atkTown, final TownBlock townBlock, final Town defTown) {
        if (FlagWarConfig.isFlaggedTownBlockUnclaimed()) {
            unclaimTownBlock(townBlock);
        } else if (FlagWarConfig.isFlaggedTownBlockTransferred()) {
            transferOwnership(atkTown, townBlock);
        } else {
            String message = Translate.fromPrefixed("area.won.defender-keeps-claims");
            TownyMessaging.sendPrefixedTownMessage(atkTown, message);
            TownyMessaging.sendPrefixedTownMessage(defTown, message);
        }
    }

    private void unclaimTownBlock(final TownBlock townBlock) {
        TownyUniverse.getInstance().getDataSource().removeTownBlock(townBlock);
    }

    private void transferOwnership(final Town attackingTown, final TownBlock townBlock) {
        try {
            townBlock.setTown(attackingTown);
            townBlock.save();
        } catch (Exception te) {
            // Couldn't claim it.
            TownyMessaging.sendErrorMsg(te.getMessage());
            te.printStackTrace();
        }
    }

    public void addAlliesToDefender(Town town) {

    }

    public void winProcess() {
        for (TownBlock block : defenderTown.getTownBlocks()) {
            try {
                if (!defenderTown.getHomeBlock().equals(block)) {
                    transferOwnership(aggressorTown, block);
                }
            } catch (TownyException ignored) {
            }
        }
        for (Resident resident : defenders) {
            if (resident.isMayor()) continue;
            try {
                resident.setTown(aggressorTown);
            } catch (Exception ignored) {
            }
        }
        double capturedBalance = defenderTown.getAccount().getHoldingBalance() * 0.75;
        aggressorTown.getAccount().setBalance(aggressorTown.getDebtBalance() + capturedBalance, "Victory in War");
        defenderTown.getAccount().setBalance(0d, "Defeat in War");
        defenderTown.removeNation();
        defenderTown.setRuined(true);

        aggressorTown.removeEnemy(defenderTown);

        for (Resident resident : defenders) {
            if (resident.isOnline()) resident.sendMessage(Component.text(Messaging.formatForString(Messaging.parsePlaceholders(Messages.lostMessageDefender, aggressorTown.getName()))));
        }
        for (Resident resident : aggressors) {
            if (resident.isOnline()) resident.sendMessage(Component.text(Messaging.formatForString(Messaging.parsePlaceholders(Messages.winMessageAttacker, String.valueOf(capturedBalance)))));
        }
        HandlerList.unregisterAll(this);
        FlagWar.warManager.finishWar(war);
    }


    public void looseProcess() {
        for (Chunk chunk : aggressorWonChunks) {
            TownBlock block = new TownBlock(chunk.getX(), chunk.getZ(), Objects.requireNonNull(towny.getTownyWorld(chunk.getWorld())));
            transferOwnership(defenderTown, block);
        }
//    for (Resident resident : defenders) { TODO: PLEN for losing 25%
//        if (resident.isMayor()) continue;
//        try {
//            resident.setTown(defenderTown);
//        } catch (Exception e) {
//            continue;
//        }
//    }
        defenderTown.setDebtBalance(defenderTown.getDebtBalance() + aggressorTown.getDebtBalance() * 0.25);
        aggressorTown.setDebtBalance(0d);
        for (Resident resident : defenders) {
            if (resident.isOnline()) resident.sendMessage(Component.text(Messaging.formatForString(Messaging.parsePlaceholders(Messages.winMessageDefender, aggressorTown.getName())))); //TODO: messages for the losers
        }
        for (Resident resident : aggressors) {
            if (resident.isOnline()) resident.sendMessage(Component.text(Messaging.formatForString(Messaging.parsePlaceholders(Messages.lostMessageAttacker, String.valueOf(defenderTown.getDebtBalance() * 0.25)))));
        }
        HandlerList.unregisterAll(this);
        FlagWar.warManager.finishWar(war);

    }



    public void updateSpawnChunk() {
        int range = this.initialSize - aggressorWonChunks.size();
        int index = (int) (Math.random() * range);
        TownBlock homeBlock = defenderTown.getTownBlocks().stream().toList().get(index);
        newSpawnChunk = Objects.requireNonNull(homeBlock.getWorld().getBukkitWorld()).getChunkAt(homeBlock.getX(), homeBlock.getZ());
        defenderTown.setHomeBlock(homeBlock);
        // Найдите центр чанка
        int centerX = homeBlock.getX() * 16 + 8;
        int centerZ = homeBlock.getZ() * 16 + 8;

        // Найдите самый верхний блок +1 по координате Y
        int highestY = defenderTown.getWorld().getHighestBlockYAt(centerX, centerZ) + 1;

        // Установите новый спавн для города
        defenderTown.setSpawn(new Location(defenderTown.getWorld(), centerX, highestY, centerZ));
    }

    public void updateSpawnChunk(TownBlock spawn) {
        defenderTown.setHomeBlock(spawn);
        // Найдите центр чанка
        int centerX = spawn.getX() * 16 + 8;
        int centerZ = spawn.getZ() * 16 + 8;

        // Найдите самый верхний блок +1 по координате Y
        int highestY = defenderTown.getWorld().getHighestBlockYAt(centerX, centerZ) + 1;

        // Установите новый спавн для города
        defenderTown.setSpawn(new Location(defenderTown.getWorld(), centerX, highestY, centerZ));
    }


    //EVENTS

    public void onAttack(TownyPlayerDamagePlayerEvent damageEvent) {
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

    @EventHandler
    public void onRespawnEvent(PlayerRespawnEvent respawnEvent) {
        Resident resident = towny.getResident(respawnEvent.getPlayer());
        Town town = towny.getTown(respawnEvent.getPlayer());
        if (town == null || !town.equals(aggressorTown)) return;
        if (!participants.contains(resident)) return;
        startScheduledMessage(respawnEvent.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoinEvent(PlayerJoinEvent joinEvent) {
        Resident resident = towny.getResident(joinEvent.getPlayer());
        if (!participants.contains(resident)) return;
        online.add(joinEvent.getPlayer().getName());
        if (aggressors.contains(resident)) {
            String message;
            if (wasOnline.contains(joinEvent.getPlayer().getName())) message = Messaging.formatForString(Messages.returnBattleSubTitle);
            else message = Messaging.formatForString(Messages.teleportBattleMessage);
            // Schedule teleport after 5 minutes if not returned
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (joinEvent.getPlayer().isOnline()) {
                        joinEvent.getPlayer().teleport(spawnLocation);
                        joinEvent.getPlayer().sendTitle(Messaging.formatForString(Messages.returnBattleTitle), message, 10, 140, 20);
                    }
                }
            };
            task.runTaskLater(FlagWar.getInstance(), 20 * 60 * 5); // 5 minutes in ticks
            teleportTasks.put(joinEvent.getPlayer(), task);
        } else if (defenders.contains(resident)) {
            joinEvent.getPlayer().sendTitle(Messaging.formatForString(Messages.joinNotificationTitle), Messaging.formatForString(Messages.joinNotificationSubTitle), 10, 140, 20);
        }
    }

    @EventHandler
    public void onCellWonEvent(CellWonEvent cellWonEvent) throws TownyException {
        Player player = cellWonEvent.getCellOwner();
        if (!playerParticipants.contains(player.getName())) return;
        Cell cell = cellWonEvent.getCellUnderAttack();
        World world = Bukkit.getWorld(cell.getWorldName());
        int chunkX = cell.getX();
        int chunkZ = cell.getZ();
        assert world != null;
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        Town town = towny.getTown(player);
        if (town == null) return;
        //Bukkit.getLogger().info("Chunk: " + chunkX + " " + chunkZ);
        if (town.equals(aggressorTown)) {
            if (chunk.equals(spawnChunk)) {
                isSpawnCaptured = true;
                player.sendMessage(Messaging.formatForComponent(Messaging.formatForString(Messages.occupiedHomeBlockMessage)));
            }
            if (chunk.equals(newSpawnChunk))
                updateSpawnChunk();
            aggressorWonChunks.add(chunk);
        } else if (town.equals(defenderTown)) {
            if (chunk.equals(spawnChunk)) {
                isSpawnCaptured = false;
                updateSpawnChunk(spawn);
            }
            aggressorWonChunks.remove(chunk);
        }
        if ((isSpawnCaptured && (double) aggressorWonChunks.size() / this.initialSize >= 0.5) || ((double) aggressorWonChunks.size() / this.initialSize >= 0.75)) {
            winProcess();
            bossBarManager.clearBossBars();
            return;
        }
        scanSurround(chunk);
    }
    @EventHandler
    public void onQuitEvent(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        String playerName = player.getName();
        if (online.contains(playerName)) {
            online.remove(playerName);
            Objects.requireNonNull(Objects.requireNonNull(towny.getTown(player)).getMayor().getPlayer()).sendMessage(Messaging.formatForComponent(Messaging.parsePlaceholders(Messages.leaveBattleMessage, player.getName())));
        }
    }

    @EventHandler
    public void onCellAttackEvent(CellAttackEvent event) {
        if (!playerParticipants.contains(event.getPlayer().getName())) return;
        World world = Bukkit.getWorld(event.getData().getWorldName());
        if (world == null) return;
        Chunk attacked = world.getChunkAt(event.getData().getX(), event.getData().getZ());
        attackChunks.add(attacked);
    }
    @EventHandler
    public void onCellAttackCancelEvent(CellAttackCanceledEvent event) {
        World world = Bukkit.getWorld(event.getCell().getWorldName());
        if (world == null) return;
        Chunk attacked = world.getChunkAt(event.getCell().getX(), event.getCell().getZ());
        attackChunks.remove(attacked);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreakBlock(BlockBreakEvent event) {
        if (attackChunks.contains(event.getBlock().getChunk()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceBlockEvent(BlockPlaceEvent event) {
        if (attackChunks.contains(event.getBlock().getChunk()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onTownLeaveEvent(TownLeaveEvent event) {
        if (participants.contains(event.getResident())) {
            event.getResident().getPlayer().sendMessage(Messaging.formatForComponent(Messages.townRestrictedLeave));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTownJoinEvent(TownAddResidentEvent event) {
        if (event.getTown() != aggressorTown && event.getTown() != defenderTown) return;

        // Schedule the removal of the resident on the next tick
        Bukkit.getScheduler().runTask(FlagWar.getInstance(), () -> {
            try {
                event.getTown().removeResident(event.getResident());
                event.getResident().getPlayer().sendMessage(Messaging.formatForComponent(Messages.getTownRestrictedJoin));
            } catch (EmptyTownException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Set<Chunk> getWarChunks() {
        return warChunks;
    }



    public double calculateCapitulationPercentage() {
        double captured = (double) aggressorWonChunks.size() / this.initialSize;
        if (isSpawnCaptured) return Math.min(captured / 0.5, 1)*100;
        return Math.min(captured / 0.75, 1)*100;
    }
}
