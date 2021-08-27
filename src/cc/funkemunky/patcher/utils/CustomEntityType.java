package cc.funkemunky.patcher.utils;

import cc.funkemunky.patcher.objects.PatchedEntityFallingBlock;
import cc.funkemunky.patcher.objects.PatchedPrimedTnt;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityFallingBlock;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;

public enum CustomEntityType {

    PrimedTNT("PrimedTnt", 20, EntityType.PRIMED_TNT, EntityTNTPrimed.class, PatchedPrimedTnt.class);
    FallingSand("FallingSand", 21, EntityType.FALLING_BLOCK, EntityFallingBlock.class, PatchedEntityFallingBlock.class);

    private String name;
    private int id;
    private EntityType entityType;
    private Class<? extends Entity> nmsClass;
    private Class<? extends Entity> customClass;

    CustomEntityType(String name, int id, EntityType entityType, Class<? extends Entity> nmsClass, Class<? extends Entity> customClass) {
        this.name = name;
        this.id = id;
        this.entityType = entityType;
        this.nmsClass = nmsClass;
        this.customClass = customClass;
    }

    public static void registerEntities() {
        for (CustomEntityType entity : values()) {
            try {
                Method a = EntityTypes.class.getDeclaredMethod("a", new Class<?>[]{Class.class, String.class, int.class});
                a.setAccessible(true);
                a.invoke(null, entity.getCustomClass(), entity.getName(), entity.getID());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public int getID() {
        return this.id;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public Class<? extends Entity> getNMSClass() {
        return this.nmsClass;
    }

    public Class<? extends Entity> getCustomClass() {
        return this.customClass;
    }
}
