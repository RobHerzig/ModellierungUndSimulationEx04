package simulation.lib;

import simulation.lib.counter.Counter;
import simulation.lib.event.CustomerArrivalEvent;
import simulation.lib.event.Event;
import simulation.lib.event.IEventObserver;
import simulation.lib.event.ServiceCompletionEvent;
import simulation.lib.event.SimulationTerminationEvent;
import study.SimulationState;
import study.SimulationStudy;

/**
 * Main Simulator class for the discrete event simulation
 */
public class Simulator implements IEventObserver {
    private long simTimeInRealTime;
    private long now;
    private SortableQueue ec;
    private boolean stop;

    /**
     * Contains simulator statistics and parameters
     */
    private SimulationStudy sims;

    /**
     * Contains simulator state.
     */
    private SimulationState state;

    /**
     * Constructor
     * Create event chain (ec), SimulationStudy- and SimulationState- objects
     * Pushes first event (customer arrival event) to queue
     */
    public Simulator() {
        // create event chain
        ec = new SortableQueue();
        sims = new SimulationStudy(this);
        state = new SimulationState(sims);
        // push the first customer arrival at t = 0
        pushNewEvent(new CustomerArrivalEvent(state, 0));
        // push the termination event at the simulationTime (max duration of simulation)
        pushNewEvent(new SimulationTerminationEvent(sims.simulationTime));
    }

    /**
     * Sets the number of ticks in simulation time per unit in real time
     *
     * @param simTimeInRealTime number of ticks per unit in real time
     */
    public void setSimTimeInRealTime(long simTimeInRealTime) {
        this.simTimeInRealTime = simTimeInRealTime;
    }

    /**
     * Converts real time to sim time
     *
     * @param realTime units in real time
     * @return units in sim time
     * @throws Exception if sim time is out of range
     */
    public long realTimeToSimTime(double realTime) throws NumberFormatException {
        double tmp = realTime * simTimeInRealTime;
        if (tmp > Long.MAX_VALUE)
            throw new NumberFormatException("simulation time out of range: " + tmp + " > " + Long.MAX_VALUE);
        return (long) Math.ceil(tmp);
    }

    /**
     * Converts sim time to real time
     *
     * @param simTime units in sim time
     * @return units in real time
     */
    public double simTimeToRealTime(long simTime) {
        return (double) simTime / simTimeInRealTime;
    }

    /**
     * Starts the simulation
     *
     * @throws Exception is thrown when event order is invalid
     */
    private void run() {
        while (!stop) {
            Event e = (Event) ec.popNextElement();
            if (e != null) {
                //check if event time is in the past
                if (e.getTime() < now) {
                    throw new RuntimeException("Event time " + e.getTime()
                            + " smaller than current time " + now);
                }

                //set event time as new simtime
                now = e.getTime();

                //register for notification
                e.register(this);

                // process event
                e.process();

                //unregister notifications
                e.unregister(this);
            } else {
                System.out.println("Event chain empty.");
                stop = true;
            }
        }
    }

    /**
     * Pushes a new event into the event chain at the correct place
     *
     * @param e the new event
     */
    private void pushNewEvent(Event e) {
        ec.pushNewElement(e);
    }

    /**
     * Stops the simulator
     */
    private void stop() {
        stop = true;
    }

    /**
     * Starts the simulation.
     */
    public void start() {
        stop = false;
        run();
    }

    /**
     * Returns the current sim time
     *
     * @return current sim time
     */
    public long getSimTime() {
        return now;
    }

    /**
     * Resets the simulator
     */
    public void reset() {
        now = 0;
        stop = false;
        ec.clear();
    }

    /**
     * Reports results.
     */
    public void report() {
        sims.report();
    }


    /**
     * Handles update statistics event from IObservableEvent objects.
     */
    public void updateStatisticsHandler(Object sender, Object arg) {
        if (sender.getClass() == CustomerArrivalEvent.class) {
            updateStatsCAE();
        } else if (sender.getClass() == ServiceCompletionEvent.class) {
            updateStatsSCE((Customer) arg);
        }
    }

    /**
     * Update statistics, fired from customer arrival event
     */
    private void updateStatsCAE() {
        updateQueueSize();

        if (state.serverBusy == true) {
            sims.statisticObjects.get(sims.ctcServerUtilization).count(1);
            sims.statisticObjects.get(sims.cthServerUtilization).count(1);
        } else {
            sims.statisticObjects.get(sims.ctcServerUtilization).count(0);
            sims.statisticObjects.get(sims.cthServerUtilization).count(0);
        }

    }

    /**
     * Update statistics, fired from service completion event
     *
     * @param currentCustomer current customer
     */
    private void updateStatsSCE(Customer currentCustomer) {
        updateQueueSize();

        if (currentCustomer != null) {
            // update customer service end time
            currentCustomer.serviceEndTime = getSimTime();

            long waitingTime = currentCustomer.getTimeInQueue();
            long serviceTime = currentCustomer.getTimeInService();
            sims.statisticObjects.get(sims.dtcWaitingTime).count(simTimeToRealTime(waitingTime));
            sims.statisticObjects.get(sims.dthWaitingTime).count(simTimeToRealTime(waitingTime));

            sims.statisticObjects.get(sims.dtcServiceTime).count(simTimeToRealTime(serviceTime));
            sims.statisticObjects.get(sims.dthServiceTime).count(simTimeToRealTime(serviceTime));
            /*
             * TODO Problem 4.2.5 - Update the DiscreteAutocorrelationCounter here
             * Count customer's waiting time
             */

        }
        // update server utilization
        if (state.serverBusy == true) {
            sims.statisticObjects.get(sims.ctcServerUtilization).count(1);
            sims.statisticObjects.get(sims.cthServerUtilization).count(1);
        } else {
            // DES.state.serverBusy == false
            sims.statisticObjects.get(sims.ctcServerUtilization).count(0);
            sims.statisticObjects.get(sims.cthServerUtilization).count(0);
        }
    }

    /**
     * Updates the queue size variable in the simulation state
     */
    private void updateQueueSize() {
        sims.minQS = state.queueSize < sims.minQS ? state.queueSize : sims.minQS;
        sims.maxQS = state.queueSize > sims.maxQS ? state.queueSize : sims.maxQS;
    }

    /**
     * @see IEventObserver#updateQueueOccupancyHandler(Object sender)
     */
    public void updateQueueOccupancyHandler(Object sender) {
        sims.statisticObjects.get(sims.ctcQueueOccupancy).count(state.queueSize);
        sims.statisticObjects.get(sims.cthQueueOccupancy).count(state.queueSize);
    }

    /**
     * @see IEventObserver#pushNewEventHandler(Class<?>)
     */
    public void pushNewEventHandler(Class<?> c) {
        if (c == CustomerArrivalEvent.class) {
            /*
			 * TODO Problem 4.2.1 - Create random interarrival times
			 * Use your random variable contained in the SimulationStudy object
			 * !!! Mind the difference between simulation and real time and do conversions accordingly !!!
			 */
            long interarrivalTime = 0;
            pushNewEvent(new CustomerArrivalEvent(state, this.getSimTime() + interarrivalTime));
        } else if (c == ServiceCompletionEvent.class) {
            /*
			 * TODO Problem 4.2.1 - Create random service times
			 * Use your random variable contained in the SimulationStudy object
			 * !!! Mind the difference between simulation and real time and do conversions accordingly !!!
			 */
            long serviceTime = 0;
            pushNewEvent(new ServiceCompletionEvent(state, this.getSimTime() + serviceTime));
        }
    }

    /**
     * @see IEventObserver#stopEventHandler(Object sender)
     */
    public void stopEventHandler(Object sender) {
        stop();
    }
}
