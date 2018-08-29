package com.ngxdev.tinyprotocol.api;

import cc.funkemunky.patcher.GrassPatcher;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class TinyProtocolHandler {
    private static AbstractTinyProtocol instance;

    public TinyProtocolHandler() {
        TinyProtocolHandler self = this;
        instance = ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_8) ? new TinyProtocol1_7(GrassPatcher.INSTANCE) {
            @Override
            public Object onPacketOutAsync(Player receiver, Object packet) {
                return self.onPacketOutAsync(receiver, packet);
            }

            @Override
            public Object onPacketInAsync(Player sender, Object packet) {
                return self.onPacketInAsync(sender, packet);
            }
        } : new TinyProtocol1_8(GrassPatcher.INSTANCE) {
            @Override
            public Object onPacketOutAsync(Player receiver, Object packet) {
                return self.onPacketOutAsync(receiver, packet);
            }

            @Override
            public Object onPacketInAsync(Player sender, Object packet) {
                return self.onPacketInAsync(sender, packet);
            }
        };
    }

    // Purely for making the code cleaner
    public static void sendPacket(Player player, Object packet) {
        instance.sendPacket(player, packet);
    }

    public static int getProtocolVersion(Player player) {
        return instance.getProtocolVersion(player);
    }

    public Object onPacketOutAsync(Player sender, Object packet) {
        String name = packet.getClass().getName();
        int index = name.lastIndexOf(".");
        String packetName = name.substring(index + 1);

        if(packetName.equals("PacketPlayOutSpawnEntity")) {
            try {
                Field field = GrassPatcher.INSTANCE.getReflectionUtils().getNMSClass("PacketPlayOutSpawnEntity").getDeclaredField("j");
                field.setAccessible(true);

                if((Integer)field.get(packet) == 50) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return packet;
    }

    public Object onPacketInAsync(Player sender, Object packet) {
        return packet;
    }
}

