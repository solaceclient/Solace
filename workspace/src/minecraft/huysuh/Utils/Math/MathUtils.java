package huysuh.Utils.Math;

public class MathUtils {
    public static double randomNumber(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static double isNegative() {
        return (Math.random() < 0.5)
                ? -1
                : 1;
    }
}
