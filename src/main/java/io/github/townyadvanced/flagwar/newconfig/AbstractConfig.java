package io.github.townyadvanced.flagwar.newconfig;

import io.github.townyadvanced.flagwar.FlagWar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

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
    private final FlagWar plugin;
    private YamlConfiguration configuration;
    public AbstractConfig (FlagWar plugin, String filename) {
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
            FlagWar.getInstance().getLogger().severe("Cannot create configuration file: " + e.getMessage());
            e.printStackTrace();
            
        }
        configuration = YamlConfiguration.loadConfiguration(configFile);
        configuration.options().header(getHeader());

        System.out.println("New configuration file: " + newFile);

        if (newFile) {
            save();
        } else loadData();
    }

    private void loadData() {
        Class<?> clazz = getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            Path path = declaredField.getAnnotation(Path.class);
            if (path == null) continue;
            if (!(Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class || i == Map.class || i == List.class || i == ConfigurationSerializable.class) || declaredField.getType().isPrimitive() || declaredField.getType() == String.class|| declaredField.getType() == Map.class|| declaredField.getType() == List.class|| declaredField.getType() == ConfigurationSerializable.class)) continue;

            if (!configuration.contains(path.path())) continue;

            try {
                if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class)  || declaredField.getType() == Serializable.class) {
                    declaredField.setAccessible(true);
                    declaredField.set(this, declaredField.getType().getMethod("deserialize", ConfigurationSection.class).invoke(null, configuration.getConfigurationSection(path.path())));
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == List.class)  || declaredField.getType() == List.class) {
                    declaredField.setAccessible(true);
                    ParameterizedType type = (ParameterizedType) declaredField.getGenericType();
                    declaredField.set(this, configuration.getList(path.path()).stream().map(i -> deserialize((Class<?>) type.getActualTypeArguments()[0], i)).collect(Collectors.toList()));
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Map.class)  || declaredField.getType() == Map.class) {
                    declaredField.setAccessible(true);
                    ConfigurationSection section = configuration.getConfigurationSection(path.path());
                    Map<String, Object> map = new HashMap<>();
                    ParameterizedType type = (ParameterizedType) declaredField.getGenericType();
                    section.getKeys(false).forEach(k -> {
                        Object value = section.get(k);
                        map.put(k, deserialize((Class<?>) type.getActualTypeArguments()[1], value));
                    });
                    declaredField.set(this, map);
                } else if (declaredField.getType().isPrimitive() || Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == ConfigurationSerializable.class) || declaredField.getType() == String.class) {
                    declaredField.setAccessible(true);
                    declaredField.set(this, configuration.get(path.path()));
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                FlagWar.getInstance().getLogger().severe("Cannot load configuration file: " + e.getMessage());
                e.printStackTrace();
                
            }
        }
    }

    public void save() {
        Class<?> clazz = getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            Path path = declaredField.getAnnotation(Path.class);
            if (path == null) continue;

            if (!(Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class || i == Map.class || i == List.class || i == ConfigurationSerializable.class) || declaredField.getType().isPrimitive() || declaredField.getType() == String.class|| declaredField.getType() == Map.class|| declaredField.getType() == List.class|| declaredField.getType() == ConfigurationSerializable.class)) continue;

            try {
                if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Serializable.class) || declaredField.getType() == Serializable.class) {
                    declaredField.setAccessible(true);
                    Serializable fieldValue = (Serializable) declaredField.get(this);
                    configuration.set(path.path(), fieldValue.serialize());
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == List.class) || declaredField.getType() == List.class) {
                    declaredField.setAccessible(true);
                    List<Object> list = (List<Object>) declaredField.get(this);
                    configuration.set(path.path(), list.stream().map(this::serialize).collect(Collectors.toList()));
                } else if (Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == Map.class) || declaredField.getType() == Map.class) {
                    declaredField.setAccessible(true);
                    Map<String, Object> map = (Map<String, Object>) declaredField.get(this);
                    configuration.set(path.path(), serialize(map));
                } else if (declaredField.getType().isPrimitive() || Arrays.stream(declaredField.getType().getInterfaces()).anyMatch(i -> i == ConfigurationSerializable.class)|| declaredField.getType() == ConfigurationSerializable.class || declaredField.getType() == String.class) {
                    declaredField.setAccessible(true);
                    configuration.set(path.path(), declaredField.get(this));
                }
            } catch (IllegalAccessException e) {
                FlagWar.getInstance().getLogger().severe("Cannot save configuration file: " + e.getMessage());
                e.printStackTrace();
                
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
            return section;
        }
        return v;
    }

    Object deserialize(Class<?> type, Object v) {
        System.out.println(v);
        System.out.println(v.getClass());

        if (v instanceof ConfigurationSection) {
            try {
                return type.getMethod("deserialize", ConfigurationSection.class).invoke(null, (ConfigurationSection) v);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                FlagWar.getInstance().getLogger().severe("Cannot load configuration file: " + e.getMessage());
                e.printStackTrace();
                
            }
        }
        if (v instanceof List) return ((List<Object>) v).stream().map(i -> deserialize(type, i)).collect(Collectors.toList());
        if (v instanceof Map) {
            Map<String, Object> map = new HashMap<>();
            ((Map<String, Object>) v).forEach((k, vv) -> map.put(k, deserialize(type, vv)));
            return map;
        }
        return v;
    }
}
