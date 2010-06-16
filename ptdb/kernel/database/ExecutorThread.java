package ptdb.kernel.database;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.Task;
import ptdb.common.dto.TaskQueue;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.util.DBConnectorFactory;

///////////////////////////////////////////////////////////////////
//// ExecutorThread

/**
 * Execute the queries asynchronously.
 *
 * <p>Monitor the asynchronous connection's task queue
 * and execute tasks one by one over a synchronous connection.</p>
 *
 * @author Ashwini Bijwe, Yousef Alsaeed
 *
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class ExecutorThread implements Runnable {

    /** Construct an instance of the executor thread that
     *  performs tasks one by one from the taskQueue.
     *
     * @param taskQueue List of Tasks that need to be executed
     *
     * @exception DBConnectionException - When we face a problem
     * while creating a database connection. These problems could
     * be that configured connection class does not exist,
     * the path for the database is not found, the container name
     * is incorrect, the connection to the database
     * could not be established etc.
     */
    public ExecutorThread(TaskQueue taskQueue) throws DBConnectionException {

        this._taskQueue = taskQueue;
        _dbConn = DBConnectorFactory.getSyncConnection(true);
        _noOfTasksExecuted = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Manage the execution of tasks from the task queue.
     *
     * <p> It aborts its working if it encounter an exception
     * or if the processing error flag in the taskQueue
     * is set to true. </p>
     *
     * <p> Stop execution if the taskQueue is completed
     * and all the tasks are executed
     * or if it exceeds its max wait time of 50 seconds. </p>
     *
     */
    public void run() {
        int maxWait = 100;
        while (maxWait != 0) {

            /**
             * In case of a processing error, we abort the connection
             * and stop the executino of this thread
             */
            if (_taskQueue.hasProcessingError()) {
                _abortConnection();
                return;
            }

            try {

                if (_noOfTasksExecuted < _taskQueue.size()) {
                    /**
                     * If there exists a task in the TaskQueue that
                     * as not been executed, then the task is executed,
                     * the task counter incremented and the maxWait
                     * counter is reset
                     */
                    _executeTask();
                    _noOfTasksExecuted++;
                    maxWait = 100;

                } else if ((_taskQueue.size() == _noOfTasksExecuted)
                        && (_taskQueue.areAllTasksAdded())) {
                    /**
                     * If all the tasks are executed and the taskQueue is
                     * complete, then the processing is completed.
                     * We then close the connection and mark execution
                     * as complete and stop the execution of this thread.
                     */
                    _dbConn.closeConnection();
                    _taskQueue.setExecutionCompleted();
                    return;

                } else {
                    /**
                     * If neither a new task is present in the queue nor
                     * the processing is completed,
                     * the thread decrements the wait counter
                     * and goes to sleep
                     */
                    maxWait--;
                    Thread.sleep(500);

                }

            } catch (DBConnectionException e) {
                /**
                 * In case of error, we abort the connection and set the
                 * ExecutionError and stop the execution of this thread
                 */
                _abortConnection();
                _taskQueue.setExecutionError("Database execution error - "
                        + e.getMessage());
                return;

            } catch (InterruptedException e) {
                _abortConnection();
                _taskQueue.setExecutionError("Database execution error - "
                        + e.getMessage());
                e.printStackTrace();
                return;
            } catch (DBExecutionException e) {
                _abortConnection();
                _taskQueue.setExecutionError("Database execution error - "
                        + e.getMessage());
                e.printStackTrace();
                return;
            } catch (ModelAlreadyExistException e) {
                _abortConnection();
                _taskQueue.setExecutionError("Database execution error - "
                        + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        System.out.println("DB Connection thread timed out" + this.toString());
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     * Delegate the call to the abortConnection() API of DBConnection
     */
    private void _abortConnection() {
        try {
            _dbConn.abortConnection();

        } catch (DBConnectionException e) {
            e.printStackTrace();
        }

    }

    /**
     * Delegate the call to the appropriate API of DBConnection
     * depending on the type of the task it is executing
     * @exception DBExecutionException
     * @exception ModelAlreadyExistException Thrown when the model being created is already in the database.
     */
    private void _executeTask() throws DBExecutionException, ModelAlreadyExistException {
        Task task = _taskQueue.get(_noOfTasksExecuted);

        //if the task is of type save model task, then execute the proper method from the connection
        if (task instanceof SaveModelTask) {
            _dbConn.executeSaveModelTask((SaveModelTask) task);
        }

        //if the task is of type CreateModelTask then execute the proper method from the connection
        if (task instanceof CreateModelTask) {
            _dbConn.executeCreateModelTask((CreateModelTask) task);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /** This is the actual database connection(synchronous)
     * over which the tasks are executed.
     */
    private DBConnection _dbConn;

    /** This is used to keep track of the number of tasks
     * executed by the executor thread.
     *
     */
    private int _noOfTasksExecuted;

    /**
     * This is the taskQueue into which an Asynchronous
     * connection adds the tasks for execution
     * The Executor thread reads tasks one by one from this
     * queue and executes them on the database
     * The taskQueue is also used as a communication mechanism
     * between the Executor thread and the Asynchronous connection
     * Each informs the other of an error by setting it
     * the error flag in the taskQueue
     */
    private TaskQueue _taskQueue;

}
