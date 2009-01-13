package ptolemy.apps.apes.demo.ThreeTasks;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CPUScheduler; 
import ptolemy.apps.apes.InterruptServiceRoutine;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class IRS_A extends InterruptServiceRoutine {

    public IRS_A() {  
    }

    public IRS_A(Workspace workspace) {
        super(workspace);  
    }

    public IRS_A(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);  
    }
     
    
    
    protected void _callCMethod() {  
        System.out.println(this.getName() + ".fire() - Time: " + getDirector().getModelTime());
        try {
            accessPointCallback(-1.0, 0.0);
            cpuScheduler.ActivateTask(1);
            accessPointCallback(0.2, -1.0);
            cpuScheduler.TerminateTask();
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
