package cc.funkemunky.patcher.utils;

import cc.funkemunky.patcher.GrassPatcher;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Config {

    public static int max_tnt_per_tick = 100;
    public static boolean tnt_knockback = true;
    public static boolean fixcannons = true;


    List<Section> sections;

    public Config() {
        sections = new ArrayList<>();

        GrassPatcher.INSTANCE.saveDefaultConfig();
        init();
    }

    private void init() {
        max_tnt_per_tick = (int) getOptimization("tnt", "max-tnt-per-tick");
        tnt_knockback = (boolean) getOptimization("tnt", "player-knockback");
        fixcannons = (boolean) getOptimization("tnt", "fix-cannons");
    }

    public Section getSectionByName(String name) {
        AtomicReference<Section> toReturn = null;
        sections.stream().filter(section -> section.getName().equalsIgnoreCase(name)).forEach(toReturn::set);
        return toReturn.get();
    }

    private Object getOptimization(String category, String name) {
        String path = "optimizations." + category + "." + name;
        if(GrassPatcher.INSTANCE.getConfig().get(path) == null) {
            try {
                GrassPatcher.INSTANCE.getConfig().set(path,this.getClass().getDeclaredField(name).get(GrassPatcher.INSTANCE.configObject));
                GrassPatcher.INSTANCE.saveConfig();
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return GrassPatcher.INSTANCE.getConfig().get(path);
    }

    @Getter
    public static class Setting {
        private String path;
        private Object defaultSetting;

        public Setting(String path, Object defaultSetting) {
            this.path = path;
            this.defaultSetting = defaultSetting;
        }
    }

    @Getter
    public class Section {
        private String name;
        private List<Setting> settings;

        public Section(String name) {
            this.name = name;
        }

        public void addSetting(Setting setting) {
            settings.add(setting);
        }

        public Setting getSetting(String path) {
            AtomicReference<Setting> toReturn = null;
            settings.stream().filter(setting -> setting.getPath().equalsIgnoreCase(name)).forEach(setting -> toReturn.set(setting));
            return toReturn.get();
        }

    }
}
