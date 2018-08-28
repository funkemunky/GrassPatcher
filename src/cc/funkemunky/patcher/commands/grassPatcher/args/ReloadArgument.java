package cc.funkemunky.patcher.commands.grassPatcher.args;

import cc.funkemunky.patcher.GrassPatcher;
import cc.funkemunky.patcher.commands.FunkeArgument;
import org.bukkit.entity.Player;

public class ReloadArgument extends FunkeArgument {

    public ReloadArgument(String name, String display, String description, String permission) {
        super(name, display, description, permission);
    }

    @Override
    public void onArgument(Player player, String[] args) {
        GrassPatcher.INSTANCE.reloadConfig();
        GrassPatcher.INSTANCE.getServer().getPluginManager().disablePlugin(GrassPatcher.INSTANCE);
        GrassPatcher.INSTANCE.getServer().getPluginManager().enablePlugin(GrassPatcher.INSTANCE);
        player.sendMessage("Reloaded!");
    }
}
