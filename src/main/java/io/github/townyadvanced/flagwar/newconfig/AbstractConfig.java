package io.github.townyadvanced.flagwar.newconfig;

import io.github.townyadvanced.flagwar.FlagWar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class AbstractConfig {
    private final String fileName;
    private final Plugin plugin;
    private YamlConfiguration configuration;
    public AbstractConfig (Plugin plugin, String filename) {
        this.fileName = filename;
        this.plugin = plugin;
    }
    public abstract String getHeader();

    public void reload() {
        File configFile = new File(plugin.getDataFolder(), fileName);

        boolean newFile = false;

        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
            if (!configFile.exists()) {
                configFile.createNewFile();
                newFile = true;
            }
        } catch (IOException e) {
            FlagWar.getFlagWar().getLogger().severe("Cannot create configuration file: " + e.getMessage());
        }
        configuration = YamlConfiguration.loadConfiguration(configFile);
        configuration.options().header(getHeader());

        if (newFile) {
            save();
        } else loadData();
    }

    private void loadData() {
        Class<?> clazz = getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            Path path = declaredField.getAnnotation(Path.class);
            if (path == null) continue;
            if (!(Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class || i == Map.class || i == List.class || i == ConfigurationSerializable.class) || declaredField.getType().isPrimitive() || declaredField.getType() == String.class)) continue;

            if (!configuration.contains(path.path())) continue;

            try {
                if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class)) {
                    declaredField.set(this, declaredField.getType().getMethod("deserialize", ConfigurationSection.class).invoke(null, configuration.getConfigurationSection(path.path())));
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == List.class)) {
                    ParameterizedType type = (ParameterizedType) declaredField.getGenericType();
                    declaredField.set(this, configuration.getList(path.path()).stream().map(i -> deserialize((Class<?>) type.getActualTypeArguments()[0], i)).collect(Collectors.toList()));
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Map.class)) {
                    ConfigurationSection section = configuration.getConfigurationSection(path.path());
                    Map<String, Object> map = new HashMap<>();
                    section.getKeys(false).forEach(k -> {
                        Object value = section.get(k);
                        map.put(k, deserialize(value.getClass(), value));
                    });
                    declaredField.set(this, map);
                } else if (declaredField.getType().isPrimitive() || declaredField.getType() == String.class || Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == ConfigurationSerializable.class)) {
                    declaredField.set(this, configuration.get(path.path()));
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                FlagWar.getFlagWar().getLogger().severe("Cannot load configuration file: " + e.getMessage());
            }
        }
    }

    public void save() {
        Class<?> clazz = getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            Path path = declaredField.getAnnotation(Path.class);
            if (path == null) continue;

            if (!(Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class || i == Map.class || i == List.class || i == ConfigurationSerializable.class) || declaredField.getType().isPrimitive() || declaredField.getType() == String.class)) continue;
            try {
                if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class)) {
                    Serializable fieldValue = (Serializable) declaredField.get(this);
                    configuration.set(path.path(), fieldValue.serialize());
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == List.class)) {
                    List<Object> list = (List<Object>) declaredField.get(this);
                    configuration.set(path.path(), list.stream().map(this::serialize).collect(Collectors.toList()));
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Map.class)) {
                    Map<String, Object> map = (Map<String, Object>) declaredField.get(this);
                    Map<String, Object> newMap = new HashMap<>();

                    map.forEach((k, v) -> newMap.put(k, serialize(v)));
                    configuration.set(path.path(), newMap);
                } else if (declaredField.getType().isPrimitive() || Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == ConfigurationSerializable.class)) {
                    configuration.set(path.path(), declaredField.get(this));
                }
            } catch (IllegalAccessException e) {
                FlagWar.getFlagWar().getLogger().severe("Cannot save configuration file: " + e.getMessage());
            }
        }

        try {
            configuration.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Object serialize(Object v) {
        if (v instanceof Serializable) return ((Serializable) v).serialize();
        if (v instanceof List) return ((List<Object>) v).stream().map(this::serialize).collect(Collectors.toList());
        if (v instanceof Map) {
            ConfigurationSection section = new YamlConfiguration();
            ((Map<String, Object>) v).forEach((k, vv) -> section.set(k, serialize(vv)));
        }
        return v;
    }

    Object deserialize(Class<?> type, Object v) {
        if (v instanceof ConfigurationSection) {
            try {
                return type.getMethod("deserialize", ConfigurationSection.class).invoke(null, v);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                FlagWar.getFlagWar().getLogger().severe("Cannot load configuration file: " + e.getMessage());
            }
        }
        if (v instanceof List) return ((List<Object>) v).stream().map(i -> deserialize(type, i)).collect(Collectors.toList());
        return v;
    }
}
