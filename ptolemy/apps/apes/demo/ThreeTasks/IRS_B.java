package ptolemy.apps.apes.demo.ThreeTasks;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.InterruptServiceRoutine;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class IRS_B extends InterruptServiceRoutine {

    public IRS_B() {  
    }

    public IRS_B(Workspace workspace) {
        super(workspace);  
    }

    public IRS_B(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);  
    }
     
    
    
    protected void _callCMethod() { 
        long period;
        System.out.println(this.getName() + ".fire() - Time: " + getDirector().getModelTime());
        try {
            accessPointCallback(-1.0, 0.0);
            cpuScheduler.activateTask(2);
            accessPointCallback(0.2, 0.0);
            cpuScheduler.terminateTask();
        } catch (NoRoomException e) { 
            e.printStackTrace();
        } catch (IllegalActionException e) { 
            e.printStackTrace();
        }          
    }

    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime);
    }
    

 
}
