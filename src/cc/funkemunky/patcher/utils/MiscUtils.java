package cc.funkemunky.patcher.utils;

import org.bukkit.Bukkit;

public class MiscUtils {

    public void printToConsole(String string) {
        Bukkit.getConsoleSender().sendMessage(Color.translate(string));
    }

    public String line(String color) {
        return color + Color.Strikethrough + "-----------------------------------------------------";
    }
}
