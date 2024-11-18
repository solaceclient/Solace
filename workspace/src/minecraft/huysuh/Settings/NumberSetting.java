package huysuh.Settings;

public class NumberSetting extends Setting {

    public enum Icon {
        PERCENTAGE("%"),
        HEART("❤"),
        MILISECONDS("ms"),
        SECONDS("s");

        public String icon;

        Icon(String s) {
            this.icon = s;
        }
    }

    private double value, minimum, maximum, increment;
    private Icon icon;

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        if (increment == 0.1){
            this.value = Math.max(this.minimum, Math.min(this.maximum, Math.round(value * 10.0) / 10.0));
        } else if (increment == 0.01){
            this.value = Math.max(this.minimum, Math.min(this.maximum, Math.round(value * 100.0) / 100.0));
        } else if (increment == 0.001) {
            this.value = Math.max(this.minimum, Math.min(this.maximum, Math.round(value * 1000.0) / 1000.0));
        } else {
            this.value = Math.max(this.minimum, Math.min(this.maximum, Math.round(value)));
        }
    }

    public void increment(boolean positive) {
        setValue(getValue() + (positive ? 1 : -1) * this.increment);
    }

    public double getMinimum() {
        return this.minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return this.maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public double getIncrement() {
        return this.increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

    public Icon getIcon() {return icon;}

    public void setIcon(Icon icon) {this.icon = icon;}

    public NumberSetting(String name, double value, double minimum, double maximum, double increment, Icon icon) {
        this.setName(name);

        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
        this.icon = icon;
    }

    public NumberSetting(String name, double value, double minimum, double maximum, double increment) {
        this.setName(name);

        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }
}
