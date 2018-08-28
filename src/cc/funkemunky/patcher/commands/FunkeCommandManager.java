package cc.funkemunky.patcher.commands;


import cc.funkemunky.patcher.commands.grassPatcher.GrassCommand;

import java.util.ArrayList;
import java.util.List;

public class FunkeCommandManager {
    public final List<FunkeCommand> commands;

    public FunkeCommandManager() {
        commands = new ArrayList<>();
        this.initialization();
    }

    private void initialization() {
        commands.add(new GrassCommand());
    }

    public void removeAllCommands() {
        commands.clear();
    }
}

