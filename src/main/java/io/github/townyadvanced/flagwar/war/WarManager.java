package io.github.townyadvanced.flagwar.war;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.storage.NewWar;
import io.github.townyadvanced.flagwar.storage.SQLiteStorage;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WarManager {
    Set<NewWar> timedWars;
    HashMap<NewWar, PreWarProcess> map = new HashMap<>();
    HashMap<NewWar, WarProcess> wars = new HashMap<>();

    public WarManager() {
        System.out.println("war manager initialazation");
        timedWars = SQLiteStorage.newWars;
        System.out.println("timed war list: " + timedWars.size());
        for (NewWar war : timedWars) {
            startPreWarProcess(war);
        }
    }
    public void startPreWarProcess(NewWar war) {
        System.out.println("starting pre war");
        map.put(war, new PreWarProcess(war));
        SQLiteStorage.saveNewWar(war);
    }

    public HashMap<NewWar, WarProcess> getWars() {
        return wars;
    }

    public void startWar(NewWar war) {
        map.remove(war);
        SQLiteStorage.deleteNewWar(war);
        wars.put(war, new WarProcess(war));
    }

    public WarProcess getWarProcessByPlayer(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null) return null;
        for (Map.Entry<NewWar, WarProcess> entry : wars.entrySet()) {
            Town attacker = entry.getKey().getAttacker();
            if (!attacker.equals(town)) continue;
            return entry.getValue();
        }
        return null;
    }

    public String getStatusPrefix(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null) return null;
        for (Map.Entry<NewWar, WarProcess> entry : wars.entrySet()) {
            Town attacker = entry.getKey().getAttacker();
            Town defender = entry.getKey().getVictim();
            if (attacker.equals(town)) {
                return "&c◆";
            }
            if (defender.equals(town)) {
                return "&9■";
            }
        }
        return "";
    }

    public String getStatus(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null) return null;
        for (Map.Entry<NewWar, WarProcess> entry : wars.entrySet()) {
            Town attacker = entry.getKey().getAttacker();
            Town defender = entry.getKey().getVictim();
            if (attacker.equals(town)) {
                return "&c◆";
            }
            if (defender.equals(town)) {
                return "&9■";
            }
        }
        return "";
    }

    public WarProcess getWarProcess(Town town1, Town town2) {
        for (Map.Entry<NewWar, WarProcess> entry : wars.entrySet()) {
            Town attacker = entry.getKey().attacker;
            Town defender = entry.getKey().victim;
            if ((attacker.equals(town1) && defender.equals(town2)) ||
                    (attacker.equals(town2) && defender.equals(town1))) {
                return entry.getValue();
            }
        }
        return null;
    }

    public WarProcess getWar(Town town) {
        for (Map.Entry<NewWar, WarProcess> entry : wars.entrySet()) {
            Town attacker = entry.getKey().getAttacker();
            Town defender = entry.getKey().getVictim();
            if (!(attacker.equals(town) || defender.equals(town))) continue;
            return entry.getValue();
        }
        return null;
    }

    public PreWarProcess getPreWar(Town town) {
        for (Map.Entry<NewWar, PreWarProcess> entry : map.entrySet()) {
            Town attacker = entry.getKey().getAttacker();
            Town defender = entry.getKey().getVictim();
            if (!(attacker.equals(town) || defender.equals(town))) continue;
            return entry.getValue();
        }
        return null;
    }


    public void FinishWar(NewWar war) {
        wars.remove(war);
    }
}
