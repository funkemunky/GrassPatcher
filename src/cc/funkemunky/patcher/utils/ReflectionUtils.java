package cc.funkemunky.patcher.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtils {
    private String serverVersion;
    private String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public Class<?> EntityPlayer = getNMSClass("EntityPlayer");
    private Class<?> Entity = getNMSClass("Entity");
    private Class<?> CraftPlayer = getCBClass("entity.CraftPlayer");
    private Class<?> CraftEntity = getCBClass("entity.CraftEntity");
    private Class<?> CraftWorld = getCBClass("CraftWorld");
    private Class<?> World = getNMSClass("World");

    public ReflectionUtils() {
        serverVersion = Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    public Object getEntityPlayer(Player player) {
        return getMethodValue(getMethod(CraftPlayer, "getHandle"), player);
    }

    public Object getEntity(Entity entity) {
        return getMethodValue(getMethod(CraftEntity, "getHandle"), entity);
    }

    public boolean isUsingItem(Player player) {
        if (!isNewVersion()) {
            Object handle = getEntityPlayer(player);
            Class<?> entityHuman = getNMSClass("EntityHuman");
            boolean isNull = (boolean) getMethodValue(getMethod(entityHuman, "bS"), handle);

            if (!isNull) {
                Object activeItem = getFieldValue(getFieldByName(entityHuman, "g"), handle);
                Object item = getMethodValue(getMethod(getNMSClass("ItemStack"), "getItem"), activeItem);
                Object varEnum = getMethodValue(getMethod(item.getClass(), "e", getNMSClass("ItemStack")), item, activeItem);

                return varEnum == getEnum(getNMSClass("EnumAnimation"), "NONE");
            }

        } else {
            return player.isBlocking();
        }
        return false;
    }

    public void setStatic(String name, Class clazz, Object val) {
        try {
            Field ex = clazz.getDeclaredField(name);
            ex.setAccessible(true);
            if (Modifier.isFinal(ex.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(ex, ex.getModifiers() & -17);
            }

            ex.set(null, val);
        } catch (ReflectiveOperationException var5) {
            var5.printStackTrace();
        }

    }

    public boolean isBukkitVerison(String version) {
        return serverVersion.contains(version);
    }

    public boolean isNewVersion() {
        return isBukkitVerison("1_9") || isBukkitVerison("1_1");
    }

    public Class<?> getCBClass(String string) {
        return getClass("org.bukkit.craftbukkit." + version + "." + string);
    }

    public Class<?> getClass(String string) {
        try {
            return Class.forName(string);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Enum<?> getEnum(Class<?> clazz, String enumName) {
        return Enum.valueOf((Class<Enum>) clazz, enumName);
    }

    public Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName) != null ? clazz.getDeclaredField(fieldName) : clazz.getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);

            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object setFieldValue(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return field.getDeclaringClass();
    }

    public Object getWorldHandle(org.bukkit.World world) {
        return getMethodValue(getMethod(CraftWorld, "getHandle"), world);
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = clazz.getMethod(methodName, args);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getMethodValue(Method method, Object object, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getFieldValue(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Object newInstance(Class<?> objectClass) {
        try {
            return objectClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Object newInstance(Class<?> objectClass, Object... args) {
        try {
            return objectClass.getConstructor(args.getClass()).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Class<?> getNMSClass(String string) {
        return getClass("net.minecraft.server." + version + "." + string);
    }
}
