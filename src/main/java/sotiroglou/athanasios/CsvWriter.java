package sotiroglou.athanasios;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvWriter {
    public static void writeSimulationStatsToCsv(List<SimulationStats> statsList, String fileName, double divider) {
        try (FileWriter csvWriter = new FileWriter(fileName)) {
            // Γράψιμο της κεφαλίδας
            csvWriter.append("N,Average number of customers in the system,Average number of customers in the queue,Average time a customer spent in the system (seconds),Average time a customer spent in the queue (seconds)\n");

            // Γράψιμο των δεδομένων
            for (int i = 0; i < statsList.size(); i++) {
                SimulationStats stats = statsList.get(i);
                csvWriter.append(String.valueOf(i + 1)).append(",")
                        .append(String.format("%.2f", stats.getAvgCustInSystem())).append(",")
                        .append(String.format("%.2f", stats.getAvgCustInQueue())).append(",")
                        .append(String.format("%.2f", stats.getAvgTimeInSystem() * divider)).append(",")
                        .append(String.format("%.2f", stats.getAvgTimeInQueue() * divider)).append("\n");
            }

            csvWriter.flush();
            System.out.println("Data written to CSV successfully!");

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }
}
