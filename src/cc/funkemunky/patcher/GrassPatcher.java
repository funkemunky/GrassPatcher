package cc.funkemunky.patcher;

import cc.funkemunky.patcher.commands.FunkeCommandManager;
import cc.funkemunky.patcher.listeners.EntitySpawnEvent;
import cc.funkemunky.patcher.objects.PatchedRedstoneWirev1_8_R1;
import cc.funkemunky.patcher.objects.PatchedBlockTNT;
import cc.funkemunky.patcher.utils.Config;
import cc.funkemunky.patcher.utils.CustomEntityType;
import cc.funkemunky.patcher.utils.MiscUtils;
import cc.funkemunky.patcher.utils.ReflectUtil;
import cc.funkemunky.patcher.utils.ReflectionUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class GrassPatcher extends JavaPlugin {

    public static GrassPatcher INSTANCE;
    public Config configObject;
    private ReflectionUtils reflectionUtils;
    private MiscUtils miscUtils;
    private ExecutorService[] services = new ExecutorService[Runtime.getRuntime().availableProcessors()];
    private int current = 0;
    private Object registryObject = reflectionUtils.newInstance(reflectionUtils.getNMSClass("Registry"));
    private FunkeCommandManager commandManager;

    private Object get(final String s) {
        if(reflectionUtils.isBukkitVerison("1_8_R1")) {
            return net.minecraft.server.v1_8_R1.Block.REGISTRY.get(new net.minecraft.server.v1_8_R1.MinecraftKey(s));
        } else {
            return Block.REGISTRY.get(new MinecraftKey(s));
        }
    }

    private void add(final int i, final String s, final Object blockObject) {
        if(reflectionUtils.isBukkitVerison("1_8_R1")) {
            net.minecraft.server.v1_8_R1.Block block = (net.minecraft.server.v1_8_R1.Block) blockObject;

            net.minecraft.server.v1_8_R1.Block.REGISTRY.a(i, new MinecraftKey(s), block);

            for (final net.minecraft.server.v1_8_R1.IBlockData iblockdata : (ImmutableList<net.minecraft.server.v1_8_R1.IBlockData>) block.O().a()) {
                final int k = net.minecraft.server.v1_8_R1.Block.REGISTRY.b(block) << 4 | block.toLegacyData(iblockdata);
                net.minecraft.server.v1_8_R1.Block.d.a(iblockdata, k);
            }
        } else {
            Block block = (Block) blockObject;
            Block.REGISTRY.a(i, new MinecraftKey(s), block);
            for (final IBlockData iblockdata : block.P().a()) {
                final int k = Block.REGISTRY.b(block) << 4 | block.toLegacyData(iblockdata);
                Block.d.a(iblockdata, k);
            }
        }
    }

    @Override
    public void onEnable() {
        for (int i = 0; i < services.length - 1; i++) {
            services[i] = Executors.newSingleThreadScheduledExecutor();
        }

        INSTANCE = this;
        commandManager = new FunkeCommandManager();
        if(reflectionUtils.isBukkitVerison("1_8_R1")) {
            cc.funkemunky.patcher.objects.RedstoneWire wire = new PatchedRedstoneWirev1_8_R1();
            add(55, "redstone_wire", (net.minecraft.server.v1_8_R1.Block) wire);
            ReflectUtil.setStatic("REDSTONE_WIRE", net.minecraft.server.v1_8_R1.Blocks.class, get("redstone_wire"));
        }

        init();
        add(46, "tnt", new PatchedBlockTNT());
        ReflectUtil.setStatic("TNT", Blocks.class, get("tnt"));
        initializeUtils();
    }

    public void init() {
        removeEntity("PrimedTnt", 20);
        removeEntity("FallingSand", 21);


        CustomEntityType.registerEntities();
    }

    private void initializeUtils() {
        reflectionUtils = new ReflectionUtils();
        miscUtils = new MiscUtils();
        configObject = new Config();

        getServer().getPluginManager().registerEvents(new EntitySpawnEvent(), this);
    }

    private void removeEntity(String name, int id) {
        try {
          if(reflectionUtils.isBukkitVerison("1_8_R1")) {
              Field fieldOne = net.minecraft.server.v1_8_R1.EntityTypes.class.getDeclaredField("c");
              Field fieldTwo = net.minecraft.server.v1_8_R1.EntityTypes.class.getDeclaredField("e");
              fieldOne.setAccessible(true);
              fieldTwo.setAccessible(true);

              Map<String, Class<? extends net.minecraft.server.v1_8_R1.Entity>> c = (Map<String, Class<? extends net.minecraft.server.v1_8_R1.Entity>>) fieldOne.get(Maps.newHashMap());
              Map<Integer, Class<? extends net.minecraft.server.v1_8_R1.Entity>> e = (Map<Integer, Class<? extends net.minecraft.server.v1_8_R1.Entity>>) fieldTwo.get(Maps.newHashMap());

              c.remove(name);
              e.remove(id);
          } else {
              Field fieldOne = EntityTypes.class.getDeclaredField("c");
              Field fieldTwo = EntityTypes.class.getDeclaredField("e");
              fieldOne.setAccessible(true);
              fieldTwo.setAccessible(true);

              Map<String, Class<? extends Entity>> c = (Map<String, Class<? extends Entity>>) fieldOne.get(Maps.newHashMap());
              Map<Integer, Class<? extends Entity>> e = (Map<Integer, Class<? extends Entity>>) fieldTwo.get(Maps.newHashMap());

              c.remove(name);
              e.remove(id);
          }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ExecutorService getExecutor() {
        current = current < services.length - 2 ? current + 1 : 0;
        return services[current];
    }
}
