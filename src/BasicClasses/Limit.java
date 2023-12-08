package BasicClasses;

public class Limit {
    private double min;
    private double max;
    private double median;

    public Limit(double left, double right) {
        this.min = left;
        this.max = right;
        this.median = (left+right)/2;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean hasSameRange(Limit limit){
        Limit[] limits = {this, limit};
        if(limits[0].getMax() > limits[1].getMax()) {
            Limit buff = limits[0];
            limits[0] = limits[1];
            limits[1] = buff;
        }
        if(limits[0].getMax() > limits[1].getMin())
            return true;
        else
            return false;
    }

    public double getDistanceToCenter(double value){
        return median-value;
    }

    public double getMedian() {
        return median;
    }

    @Override
    public String toString() {
        return "Min: "+min+" Max: "+max;
    }
}
