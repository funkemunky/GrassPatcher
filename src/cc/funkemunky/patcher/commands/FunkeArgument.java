package cc.funkemunky.patcher.commands;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class FunkeArgument {
    private FunkeCommand parent;
    private String name;
    private String display;
    private String description;
    private List<String> aliases = new ArrayList<>();
    private Map<Integer, List<String>> tabComplete = new HashMap<>();
    private String[] permission;

    protected FunkeArgument(String name, String display, String description) {
        this.name = name;
        this.display = display;
        this.description = description;
    }

    protected FunkeArgument(String name, String display, String description, String... permission) {
        this.name = name;
        this.display = display;
        this.description = description;
        this.permission = permission;
    }

    protected void addAlias(String alias) {
        aliases.add(alias);
    }

    protected void addTabComplete(int arg, String name) {
        List<String> completion = tabComplete.getOrDefault(arg, new ArrayList<>());

        completion.add(name);

        tabComplete.put(arg, completion);
    }

    public abstract void onArgument(Player player, String[] args);

    protected FunkeCommand getParent() {
        return this.parent;
    }
}

