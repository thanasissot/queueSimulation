package sotiroglou.athanasios;

public class SimulationStats {
    double avgCustInSystem;
    double avgCustInQueue;
    double avgTimeInSystem;
    double avgTimeInQueue;

    public SimulationStats(double avgCustInSystem, double avgCustInQueue, double avgTimeInSystem, double avgTimeInQueue) {
        this.avgCustInSystem = avgCustInSystem;
        this.avgCustInQueue = avgCustInQueue;
        this.avgTimeInSystem = avgTimeInSystem;
        this.avgTimeInQueue = avgTimeInQueue;
    }

    public double getAvgCustInSystem() {
        return avgCustInSystem;
    }

    public double getAvgCustInQueue() {
        return avgCustInQueue;
    }

    public double getAvgTimeInSystem() {
        return avgTimeInSystem;
    }

    public double getAvgTimeInQueue() {
        return avgTimeInQueue;
    }
}
