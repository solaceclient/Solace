package huysuh.Utils;

public class Timer {
    public long lastMS = System.currentTimeMillis();

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public double getTimeElapsed() {
        return (System.currentTimeMillis() - lastMS);
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if(System.currentTimeMillis() - lastMS > time) {
            if(reset)
                reset();

            return true;
        }

        return false;
    }
}