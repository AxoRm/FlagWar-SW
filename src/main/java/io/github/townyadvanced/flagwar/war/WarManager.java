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
        timedWars = SQLiteStorage.newWars;
        for (NewWar war : timedWars) {
           map.put(war, new PreWarProcess(war));
        }
    }
    public void startPreWarProcess(NewWar war) {
        map.put(war, new PreWarProcess(war));
    }
    public void startWar(NewWar war) {
        wars.put(war, new WarProcess(war));
    }

    public WarProcess getWarProcessByPlayer(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null) return null;
        for (Map.Entry<NewWar, WarProcess> entry : wars.entrySet()) {
            Town attacker = entry.getKey().getAttacker();
            if (!attacker.equals(town)) continue;
            entry.getValue().
        }
    }
}
