package cc.funkemunky.patcher.commands;

import cc.funkemunky.patcher.GrassPatcher;
import cc.funkemunky.patcher.utils.Color;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class FunkeCommand implements Listener {
    private static FunkeCommand instance;
    private final String name;
    private final String display;
    private final String permission;
    private final String description;
    private final List<FunkeArgument> arguments;

    public FunkeCommand(String name, String display, String description, String permission) {
        this.name = name;
        this.display = display;
        this.permission = permission;
        this.description = description;
        this.arguments = new ArrayList<>();
        instance = this;

        GrassPatcher.INSTANCE.getServer().getPluginManager().registerEvents(this, GrassPatcher.INSTANCE);
        this.addArguments();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().replaceAll("/", "").split(" ");

        //Bukkit.broadcastMessage(event.getMessage() + ", " + args[0]);
        if (args[0].equalsIgnoreCase(name)) {
            Player sender = event.getPlayer();
            if (this.permission != null && !sender.hasPermission(this.permission)) {
                sender.sendMessage(Color.Red + "No permission.");
                return;
            }
            if (args.length == 1) {
                sender.sendMessage(GrassPatcher.INSTANCE.getMiscUtils().line(Color.Dark_Gray));
                sender.sendMessage(Color.Gold + Color.Bold + this.display + Color.Yellow + " Command Help " + Color.White + "Page (1 / " + (arguments.size() / 6) + ")");
                sender.sendMessage("");
                sender.sendMessage(Color.translate("&b<> &7= required. &b[] &7= optional."));
                sender.sendMessage("");
                int max = Math.min(6, arguments.size());

                for (int i = 0; i < max; i++) {
                    FunkeArgument argument = arguments.get(i);
                    sender.sendMessage(Color.Gray + "/" + args[0].toLowerCase() + Color.White + " " + argument.getDisplay() + Color.Gray + " to " + argument.getDescription());
                }
                sender.sendMessage(GrassPatcher.INSTANCE.getMiscUtils().line(Color.Dark_Gray));
            } else {
                try {
                    int page = Integer.parseInt(args[2]);
                    sender.sendMessage(GrassPatcher.INSTANCE.getMiscUtils().line(Color.Dark_Gray));
                    sender.sendMessage(Color.Gold + Color.Bold + this.display + Color.Yellow + " Command Help " + Color.White + "Page (" + page + " / " + (arguments.size() / 6) + ")");
                    sender.sendMessage("");
                    sender.sendMessage(Color.translate("&b<> &7= required. &b[] &7= optional."));
                    sender.sendMessage("");
                    int max = Math.min(6 * page, arguments.size());

                    for (int i = (6 * page - 5); i < max; i++) {
                        FunkeArgument argument = arguments.get(i);
                        sender.sendMessage(Color.Gray + "/" + args[0].toLowerCase() + Color.White + " " + argument.getDisplay() + Color.Gray + " to " + argument.getDescription());
                    }
                    sender.sendMessage(GrassPatcher.INSTANCE.getMiscUtils().line(Color.Dark_Gray));
                } catch (Exception e) {
                    for (FunkeArgument argument : this.arguments) {

                        if (!args[1].equalsIgnoreCase(argument.getName()) && !argument.getAliases().contains(args[1].toLowerCase()))
                            continue;

                        if ((argument.getPermission() == null || sender.hasPermission("GrassPatcher.admin")
                                || sender.hasPermission(permission))) {
                            argument.onArgument(sender, args);
                            break;
                        }
                        sender.sendMessage(Color.Red + "No permission.");
                        break;
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    protected abstract void addArguments();
}

