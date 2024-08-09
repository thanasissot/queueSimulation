package sotiroglou.athanasios;

import java.util.*;
import java.util.stream.Collectors;

import static sotiroglou.athanasios.CsvWriter.writeSimulationStatsToCsv;

public class MM1QueueSimulation {
    // GLOBAL CONSTANTS - can be modified to simulate different λ and μ values as well as the total Simulation time
    // SIMULATION_TIME is 60 seconds * 60 minutes * SECONDS_DIVIDER * HOURS

    // SECONDS_DIVIDER is an arbitrary convention, we split SECONDS in TENS.
    // REASONING EXPLAINED = we generate Integers for the distribution values where the appropriate type is a double
    // This helps in simulating the times of arrival and service with ease. But the side effect was the average values
    // calculated were not converging to the theoretical ones. In order to slightly increase precision I decided to
    // split the seconds in TENS and so the loop is executing for example for 5 hours = 18_000 seconds = 180_000 TENS of seconds
    // where a 41 value represents 4.1 seconds
    private static int HOURS = 5;
    private static int SECONDS_DIVIDER = 10;
    private static int SIMULATION_TIME = 60 * 60 * SECONDS_DIVIDER * HOURS; // 5 hours
    private static double LAMBDA = 2.0; // 2 clients per minute
    private static double MEAN = 24; // 0.4 minutes = 24 seconds to complete service for a client
    private static int N = 100;   // how many times to execute the simulation
    private static final List<SimulationStats> statsList = new ArrayList<>();

    public static void main(String[] args) {
        boolean writeToCsv = false;
        if (args.length > 0) {
            HOURS = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            LAMBDA = Double.parseDouble(args[1]);
        }
        if (args.length > 2) {
            MEAN = Double.parseDouble(args[2]);
        }
        if (args.length > 3) {
            N = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
            writeToCsv = Integer.parseInt(args[4]) == 1;
        }

        // execute simulation for N times
        for (int counter = 0; counter < N; counter++) {
            executeSimulation(true);
        }

        // aggregated averages for all simulations executed
        // average customers total in system
        double avgCustomersInSystem = statsList.stream().map(x -> x.avgCustInSystem).mapToDouble(Double::doubleValue).sum() / (1.0 * N);
        // average customers total in system
        double avgCustomersInQueue = statsList.stream().map(x -> x.avgCustInQueue).mapToDouble(Double::doubleValue).sum() / (1.0 * N);
        // average time a customer spent in system
        double avgTimeInSystem = statsList.stream().map(x -> x.avgTimeInSystem).mapToDouble(Double::doubleValue).sum() / (1.0 * N);
        // average time a customer spent in queue
        double avgTimeInQueue = statsList.stream().map(x -> x.avgTimeInQueue).mapToDouble(Double::doubleValue).sum() / (1.0 * N);

        printAveragesToConsole(avgCustomersInSystem, avgCustomersInQueue, avgTimeInSystem, avgTimeInQueue, (1.0/ N));

        if (writeToCsv) {
            writeSimulationStatsToCsv(statsList, "simulation_stats.csv", (1.0 / N));
        }
    }

    /**
     * Main method. Executes the M/M/1 system queue simulation
     * Input is a boolean flag that when TRUE will print the averages for each simulation executed
     * Each loop execution is the 1/10 of a second and the values generated for distributions are multiplied by 10
     * in order to use integers. We arbitrarily follow the notion that seconds are split in 10s, 1 seconds = 10ms
     * EXAMPLE: 47 in serviceTimes is 4.7 seconds
     *
     * Steps:
     * 1. create a collection of values that each one represents the time needed to complete the service of a customer.
     *    The countdown begins when a customer is assigned as currentCustomer.
     * 2. create a collection of values that each one represents the time of arrival of a customer.
     *    The countdown begins when a customer is assigned as currentCustomer.
     * 3. Declare and init variables needed.
     * 4. LOOP
     *    a. if currentTime (loop counter) equals to next Arrival time insert a Customer in Queue
     *    b. if a customer is being serviced and its service time is not 0, decrement the current service time
     *    c. if a customer is being serviced and service time reaches 0, remove customer from current customer
     *          assign endTime for service, and add to ServicedCustomerList
     *    d. if no customer is being serviced and queue has a customer waiting, remove customer from queue and,
     *          assign customer as being serviced, also set the service start time for the customer
     *
     * VARIOUS
     *  -> we keep track of serviced Customers and use their fields to calculate the avg time in system and time in queue
     *  -> we keep track for each iteration the number of customers in system and in queue to calculate averages
     * @param runOncePrintToConsole
     */
    public static void executeSimulation(boolean runOncePrintToConsole) {
        //
        List<Customer> servicedCustomersList = new ArrayList<>();
        List<Integer> customersInSystem = new ArrayList<>();
        List<Integer> customersInQueue = new ArrayList<>();

        // generate values based on exponential value for 1/m = 0.4 minutes = 24 seconds
        // this generates times until it surpasses the total SIMULATION time which is
        // more than enough
        Queue<Integer> serviceTimes = generateServiceTimes(MEAN, SIMULATION_TIME);

        // generates values that follow Poisson Distribution with L = 2
        // these values mark the time of when a customer arrives in the system
        Queue<Integer> clientArrivalTimes = clientArrivalTimes(LAMBDA, SIMULATION_TIME);

        // populate first arrival time
        int nextArrivalTime = clientArrivalTimes.remove();
        int currentServiceTime = 0;

        Customer currentCustomer = null;
        Queue<Customer> customerQueue = new LinkedList<>();
        for (int currentTime = 0; currentTime < SIMULATION_TIME; currentTime++) {
            // if currentTime equals to next arrival time customer enters the system
            if (currentTime == nextArrivalTime) {
                while (currentTime == nextArrivalTime) {
                    Customer newCustomer = new Customer(currentTime);
                    // add to queue
                    customerQueue.add(newCustomer);
                    // update nextArrivalTime
                    nextArrivalTime = clientArrivalTimes.remove();
                }
            }

            // customer service is ongoing decrement the serviceTime
            if (currentCustomer != null && currentServiceTime > 0) {
                currentServiceTime--;
            }
            // customer service is completed
            else if (currentCustomer != null) {
                // remove current customer from System
                currentCustomer.endService(currentTime);
                // add customer to serviced - for calculating results needed
                servicedCustomersList.add(currentCustomer);
                // remove customer from being serviced
                currentCustomer = null;
            }

            // no customer is being serviced
            if (currentCustomer == null && !customerQueue.isEmpty()) {
                // customer exists in queue - assign him as being serviced
                currentCustomer = customerQueue.remove();
                // mark the time customer left the queue and starts being serviced
                currentCustomer.startService(currentTime);
                // assign next service time value
                currentServiceTime = serviceTimes.remove();
            }

            // stats recording
            int customerServiced = currentCustomer != null ? 1 : 0;
            customersInSystem.add(customerServiced + customerQueue.size());
            customersInQueue.add(customerQueue.size());

        }

        // calculate stats for this simulation
        double avgCustomersInSystem = customersInSystem.stream().mapToInt(Integer::intValue).sum() / (1.0 * customersInSystem.size());
        double avgCustomersInQueue = customersInQueue.stream().mapToInt(Integer::intValue).sum() / (1.0 * customersInSystem.size());
        double avgTimeInSystem = servicedCustomersList.stream().map(customer -> customer.getEndTime() - customer.getArrivalTime())
                .collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum() / (1.0 * servicedCustomersList.size());
        double avgTimeInQueue = servicedCustomersList.stream().map(customer -> customer.getStartTime() - customer.getArrivalTime())
                .collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum() / (1.0 * servicedCustomersList.size());

        // flag true - running simulation scenario once - print inner stats
        if (runOncePrintToConsole) {
            printAveragesToConsole(avgCustomersInSystem, avgCustomersInQueue, avgTimeInSystem, avgTimeInQueue, 0.1);
        }

        // keep history of values to present averages for N simulations run
        SimulationStats simulationStats = new SimulationStats(avgCustomersInSystem, avgCustomersInQueue, avgTimeInSystem, avgTimeInQueue);
        statsList.add(simulationStats);
    }



    /**
     generates values that follow the poisson distribution for λ = lambda
     Returns a Collection (Queue) holding the generated values

     NOTE: return as Queue is an implementation decision for the main business logic
     */
    public static Queue<Integer> clientArrivalTimes(double lambda, double timeInterval) {
        Queue<Integer> arrivalTimes = new LinkedList<>();
        double offset = 100.0;

        double currentTime = 0.0;
        while (currentTime < (timeInterval + offset)) {
            double interArrivalTime = -Math.log(1.0 - Math.random()) / (lambda / 60.0); // lambda in clients per second
            currentTime += interArrivalTime;
            if (currentTime < (timeInterval + offset)) {
                arrivalTimes.add((int)(currentTime*10));
            }
        }
        // uncommented to print out related information
//        System.out.println("Generated arrival times (Customers to enter system) is = " + arrivalTimes.size());
//        System.out.println("Arrival times is = " + arrivalTimes);
        return new LinkedList<>(arrivalTimes);
    }

    /**
        generates values that follow the exponential distribution, with 1/μ = mean
        Returns a Collection (Queue) holding the generated values

        NOTE: return as Queue is an implementation decision for the main business logic
     */
    // https://stackoverflow.com/questions/29020652/java-exponential-distribution
    // https://stackoverflow.com/questions/2106503/pseudorandom-number-generator-exponential-
    // https://en.wikipedia.org/wiki/Exponential_distribution
    public static Queue<Integer> generateServiceTimes(double mean, int count) {
        Queue<Integer> values = new LinkedList<>();
        double rate = 1.0 / mean; // Calculate rate
        int sum = 0, i = 0;

        while (sum < count) {
            // Generate a random value using exponential distribution
            double randomValue = Math.random();
            int generatedValue = (int) Math.round((-Math.log(1.0 - randomValue) / rate) * 10);
            values.add(generatedValue);
            sum += generatedValue;
        }
//        System.out.println("Average service time is = " + sum/values.size());
//        System.out.println("Service times = " + values);
        return values;
    }

    private static void printAveragesToConsole(double avgCustomersInSystem, double avgCustomersInQueue, double avgTimeInSystem, double avgTimeInQueue, double divider) {
        // average customers total in system
        System.out.printf("Average number of customers in the system: %.2f \n", avgCustomersInSystem);

        // average customers total in system
        System.out.printf("Average number of customers in the queue: %.2f \n", avgCustomersInQueue);

        // average time a customer spent in system
        System.out.printf("Average time a customer spent in the system: %.2f seconds.\n", (avgTimeInSystem * divider));

        // average time a customer spent in queue
        System.out.printf("Average time a customer spent in the queue: %.2f seconds.\n\n", (avgTimeInQueue * divider));
    }



}
