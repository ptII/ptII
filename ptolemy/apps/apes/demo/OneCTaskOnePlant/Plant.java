package ptolemy.apps.apes.demo.OneCTaskOnePlant;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class Plant extends CTask {

    public Plant() { 
    }

    public Plant(Workspace workspace) {
        super(workspace); 
    }

    public Plant(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    public int activateTask(int taskId) throws NoRoomException, IllegalActionException {
        return cpuScheduler.activateTask(taskId);
    }
    

    @Override
    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime);
    }
    
    public void terminateTask() throws NoRoomException, IllegalActionException {
        cpuScheduler.terminateTask();
    }

    
    private native void CMethod(); 
    
    protected void _callCMethod() {  
            
        System.out.println(this.getName() + "._callCMethod()");
        CMethod();   
    }
    
    
    
}
