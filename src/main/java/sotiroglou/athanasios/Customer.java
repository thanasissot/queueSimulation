package sotiroglou.athanasios;

public class Customer {

    private int arrivalTime;
    private int startTime;
    private int endTime;

    public Customer(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void startService(int startTime) {
        this.startTime = startTime;
    }

    public void endService(int endTime) {
        this.endTime = endTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Customer arrived at " + arrivalTime +
                ", started service at " + startTime +
                ", finished at " + endTime;
    }
}
