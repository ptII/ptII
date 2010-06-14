package ptdb.common.dto;

///////////////////////////////////////////////////////////////////
//// Task
/**
 *  Abstract class that defines a task that can be executed over the database.
 *
 *  @author Ashwini Bijwe
 *
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (abijwe)
 *  @Pt.AcceptedRating Red (abijwe)
 */
public abstract class Task {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return true if the given task is an update task or
     * false if it is a select task.
     * @return True if task is an update task, false otherwise.
     */
    public boolean isUpdateTask() {
        return _isUpdateTask;
    }

    /**
     * Set the given task as an update task or select task
     * depending on the value of isUpdateTask.
     * @param isUpdateTask Boolean that specifies if the
     *                       given task is an update task.
     */
    public void setIsUpdateTask(boolean isUpdateTask) {
        _isUpdateTask = isUpdateTask;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /**
     * There are two types of tasks - Update/Write tasks
     * that change the data in the database and Select/Read
     * that read the data from the database.
     *
     * If this is true, then the task is an Update/Write task.
     * If this is false, then the task is a Read/Select task.
     */
    protected boolean _isUpdateTask;
}
