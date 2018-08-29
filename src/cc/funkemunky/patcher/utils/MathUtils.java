package cc.funkemunky.patcher.utils;

public class MathUtils {
    public static int floor(double var0) {
        int var2 = (int) var0;
        return var0 < var2 ? var2 - 1 : var2;
    }
}
