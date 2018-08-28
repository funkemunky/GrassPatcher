package cc.funkemunky.patcher.utils;

import cc.funkemunky.patcher.GrassPatcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMessage {
    private final List<AMText> Text = new ArrayList<>();

    public AMText addText(String Message) {
        AMText Text = new AMText(Message);
        this.Text.add(Text);
        return Text;
    }

    private String getFormattedMessage() {
        StringBuilder Chat = new StringBuilder("[\"\",");
        for (AMText Text : this.Text) {
            Chat.append(Text.getFormattedMessage()).append(",");
        }
        Chat = new StringBuilder(Chat.substring(0, Chat.length() - 1));
        Chat.append("]");
        return Chat.toString();
    }

    public void sendToPlayer(Player player) {
        try {
            Object base;
            Constructor titleConstructor = GrassPatcher.INSTANCE.getReflectionUtils().getNMSClass("PacketPlayOutChat").getConstructor(GrassPatcher.INSTANCE.getReflectionUtils().getNMSClass("IChatBaseComponent"));
            base = GrassPatcher.INSTANCE.getReflectionUtils().isBukkitVerison("1_7") || GrassPatcher.INSTANCE.getReflectionUtils().isBukkitVerison("1_8_R1") ? GrassPatcher.INSTANCE.getReflectionUtils().getNMSClass("ChatSerializer").getMethod("a", String.class).invoke(null, this.getFormattedMessage()) : GrassPatcher.INSTANCE.getReflectionUtils().getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, this.getFormattedMessage());
            Object packet = titleConstructor.newInstance(base);
            this.sendPacket(player, packet);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", GrassPatcher.INSTANCE.getReflectionUtils().getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Class<?> getCBClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public enum ClickableType {
        RunCommand("run_command"),
        SuggestCommand("suggest_command"),
        OpenURL("open_url");

        final String Action;

        ClickableType(String Action) {
            this.Action = Action;
        }
    }

    public class AMText {
        private final Map<String, Map.Entry<String, String>> Modifiers;
        private String Message;

        AMText(String Text) {
            this.Message = "";
            this.Modifiers = new HashMap<>();
            this.Message = Text;
        }

        public String getMessage() {
            return this.Message;
        }

        String getFormattedMessage() {
            StringBuilder Chat = new StringBuilder("{\"text\":\"" + this.Message + "\"");
            for (String Event2 : this.Modifiers.keySet()) {
                Map.Entry<String, String> Modifier = this.Modifiers.get(Event2);
                Chat.append(",\"").append(Event2).append("\":{\"action\":\"").append(Modifier.getKey()).append("\",\"value\":").append(Modifier.getValue()).append("}");
            }
            Chat.append("}");
            return Chat.toString();
        }

        public /* varargs */ AMText addHoverText(String... Text) {
            String Event2 = "hoverEvent";
            String Key = "show_text";
            StringBuilder Value = new StringBuilder();
            if (Text.length == 1) {
                Value = new StringBuilder("{\"text\":\"" + Text[0] + "\"}");
            } else {
                Value = new StringBuilder("{\"text\":\"\",\"extra\":[");
                for (String Message : Text) {
                    Value.append("{\"text\":\"").append(Message).append("\"},");
                }
                Value = new StringBuilder(Value.substring(0, Value.length() - 1));
                Value.append("]}");
            }
            AbstractMap.SimpleEntry<String, String> Values2 = new AbstractMap.SimpleEntry<>(Key, Value.toString());
            this.Modifiers.put(Event2, Values2);
            return this;
        }

        public AMText addHoverItem(ItemStack Item) {
            try {
                String Event2 = "hoverEvent";
                String Key = "show_item";
                Class craftItemStack = GrassPatcher.INSTANCE.getReflectionUtils().getCBClass("CraftItemStack");
                Class items = Class.forName("org.bukkit.inventory.ItemStack");
                Object NMS = craftItemStack.getClass().getMethod("asNMSCopy", items).invoke(Item);
                String Value = NMS.getClass().getMethod("getTag", new Class[0]).toString();
                AbstractMap.SimpleEntry<String, String> Values2 = new AbstractMap.SimpleEntry<>(Key, Value);
                this.Modifiers.put(Event2, Values2);
                return this;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public AMText setClickEvent(ClickableType Type2, String Value) {
            String Event2 = "clickEvent";
            String Key = Type2.Action;
            AbstractMap.SimpleEntry<String, String> Values2 = new AbstractMap.SimpleEntry<>(Key, "\"" + Value + "\"");
            this.Modifiers.put(Event2, Values2);
            return this;
        }
    }

}

