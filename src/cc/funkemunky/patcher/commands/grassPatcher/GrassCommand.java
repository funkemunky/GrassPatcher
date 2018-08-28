package cc.funkemunky.patcher.commands.grassPatcher;

import cc.funkemunky.patcher.commands.FunkeCommand;
import cc.funkemunky.patcher.commands.grassPatcher.args.ReloadArgument;

public class GrassCommand extends FunkeCommand {

    public GrassCommand() {
        super("grass", "GrassPatcher", "The main grass command.", "grass.admin");
    }

    @Override
    protected void addArguments() {
        getArguments().add(new ReloadArgument("reload", "reload", "reload the plugin", "fiona.staff"));
    }
}
