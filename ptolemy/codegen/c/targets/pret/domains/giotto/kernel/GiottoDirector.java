/* Code generator helper class associated with the GiottoDirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.targets.pret.domains.giotto.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.giotto.kernel.GiottoReceiver;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;



////GiottoDirector

/**
 Code generator helper associated with the GiottoDirector class. This class
 is also associated with a code generator.

 @author Ben Lickly, Shanna-Shaye Forbes, Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.2
 @Pt.ProposedRating Red (blickly)
 @Pt.AcceptedRating Red (blickly)
 */
public class GiottoDirector extends ptolemy.codegen.c.domains.giotto.kernel.GiottoDirector {

    public String generateFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        //code.append("//fire code should be here. I'm from the openRTOS GiottoDirector "+_eol);
        System.out.println("generateFireCode from pret giotto director called here");

        return code.toString();
    }
    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        System.out.println("generateFireFunctionCode called from pret giotto director***************");
        StringBuffer code = new StringBuffer(" ");//super.generateFireFunctionCode());
        code.append("//generateFireFunctionCode called from pret giotto director");
        return code.toString();

        //content moved to _generateActorsCode() which is called in preinitialize b/c code wasn't being generated for 
        //nested actors


    }
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());
        // Declare the thread handles.
        System.out.println("generatePreinitializeCode from pret giotto director called here");
        code.append("//should driver code be here");
        /*
            code.append(_generateBlockCode("preinitBlock"));
            HashSet<Integer> frequencies = _getAllFrequencies();

            StringBuffer frequencyTCode = new StringBuffer();

            ArrayList args = new ArrayList();
            args.add("");

            int currentPriorityLevel = 1;
            if(_isTopGiottoDirector())
            {
                args.set(0, "$actorSymbol()_scheduler");
                code.append(_generateBlockCode("declareTaskHandle", args));
            }
            for(int frequencyValue : frequencies) {
                // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
    //            frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));

               args.set(0, _getThreadName(frequencyValue));
               code.append(_generateBlockCode("declareTaskHandle", args));
            }

            code.append("//driver code should be below here******************"+_eol);
           code.append(_generateDriverCode());
           code.append(_generateActorsCode());
           //code.append("am i the top most director??");
            //System.out.println("I should check to see if I'm the top most Giotto director here.. ");
         */
      //  code.append(_eol+_generateDriverCode());
        //code.append(_generateActorsCode());
        
        code.append("//driver code should be below here******************"+_eol);
        code.append(_eol+_generateInDriverCode());
        code.append(_eol+_generateOutDriverCode());
        code.append(_generateActorsCode());
        code.append("// end of generate Preinitialize code here %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        //code.append("am i the top most director??");
         //System.out.println("I should check to see if I'm the top most Giotto director here.. ");
        
        if(_isTopDirectorFSM()){
            code.append(_eol+"//################# fire code for Giotto stuff here"+_eol);
            code.append(_generateFireCode());
            code.append(_eol+"//end of generate fire code stuff for top director fsm"+_eol);
            }
        
        
        return processCode(code.toString());
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Attribute iterations = _director.getAttribute("iterations");
        if (iterations == null) {
            code.append(_eol + "while (true) {" + _eol);
        } else {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
            .intValue();
            if (iterationCount <= 0) {
                code.append(_eol + "while (true) {" + _eol);
            } else {
                // Declare iteration outside of the loop to avoid
                // mode" with gcc-3.3.3
                code.append(_eol + "int iteration;" + _eol);
                code.append("for (iteration = 0; iteration < "
                        + iterationCount + "; iteration ++) {" + _eol);
                //call the actor methods here....
            }
        }

        //Begin Shanna Comment out here

        code.append(_generateFireCode());

        // The code generated in generateModeTransitionCode() is executed
        // after one global iteration, e.g., in HDF model.
        ActorCodeGenerator modelHelper = (ActorCodeGenerator) _getHelper(_director
                .getContainer());
        modelHelper.generateModeTransitionCode(code);

        /*if (callPostfire) {
                    code.append(_INDENT2 + "if (!postfire()) {" + _eol + _INDENT3
                            + "break;" + _eol + _INDENT2 + "}" + _eol);
                }
         */

        code.append(generatePostfireCode());


        //end Shanna comment out here
        code.append("}" + _eol);

        return code.toString();
    }
    /** Generate a variable declaration for the <i>period</i> parameter,
     *  if there is one.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer variableDeclarations = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            variableDeclarations.append(helperObject.generateVariableDeclaration());
            variableDeclarations.append(_generatePortVariableDeclarations(actor));







            variableDeclarations.append("//should attempt to append port here"+_eol);
            // variableDeclarations.append(helperObject.);
        }

        return variableDeclarations.toString();
    }


    public Set getHeaderFiles() throws IllegalActionException {
        HashSet files = new HashSet();
        files.add("\"deadline.h\"");
        return files;
    }
    public String getPortVariableDeclarations()  throws IllegalActionException {
        return "getPortVariableDeclaration from Giotto Director Called";
    }
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
    throws IllegalActionException {

//        if(port.isOutput())
//        {// do own thing here}
//            // will need to take care of the case of where the output is for a composite actor
//            //return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[0]+"_PORT";
//            return "*"+CodeGeneratorHelper.generateName(port)+"_PORT";  // will need to handle multiple channels later
//            //return "//dummy for write port";
//        }
//        else
            //return "//from else";
            return super.getReference(port, channelAndOffset, forComposite, isWrite,helper);
    }
    
    public String driverGetReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
    throws IllegalActionException {
        if(port.isOutput())
        {// do own thing here}
            // will need to take care of the case of where the output is for a composite actor
            //return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[0]+"_PORT";
            return "*"+CodeGeneratorHelper.generateName(port)+"_PORT";  // will need to handle multiple channels later
            //return "//dummy for write port";
        }
        else
            //return "//from else";
            return super.getReference(port, channelAndOffset, forComposite, isWrite,helper);
    
    
    }
    
    /** Construct the code generator helper associated with the given
     *  GiottoDirector.
     *  @param giottoDirector The associated
     *  ptolemy.domains.giotto.kernel.GiottoDirector
     */
    public GiottoDirector(ptolemy.domains.giotto.kernel.GiottoDirector giottoDirector) {
        super(giottoDirector);

    }

    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        double period = _getPeriod();
        int threadID = 0;
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
            //ActorCodeGenerator helper = (ActorCodeGenerator) _getHelper(actor);

            // FIXME: generate deadline instruction w/ period

            // Note: Currently, the deadline instruction takes the number 
            // of cycle as argument. In the future, this should be changed
            // to use time unit, which is platform-independent. 
            // So for now, we will assume the clock speed to be 250 Mhz. 
            // Thus, in order to get a delay of t seconds (= period/frequency) 
            // for each actor, we will need to wait for 
            // period/frequency * 250,000,000 cycles/second.

            //int cycles = (int)(250000000 * period / _getFrequency(actor));
            int cycles = (int)(250000000 * period / _getFrequency(actor));
            // FIXME: Account for time that driver takes in a more systematic fashion.
            final int driverCycles = 1000;
            code.append("#ifdef THREAD_" + threadID + "\n");
            // for
            String index = CodeGeneratorHelper.generateName((NamedObj) actor) + "_frequency";
            code.append("for (int " + index + " = 0; " + index + " < " +
                    _getFrequency(actor) + "; ++" + index + ") {" + _eol);
            code.append("DEAD0(" + cycles  + "); // period" + _eol);
            code.append("DEAD1(" + (cycles-driverCycles)  + "); //period - wcet" + _eol);
            code.append(_getActorName(actor)+"_driver_in();//read inputs from ports determinitically"+_eol);
            
            code.append(_getActorName(actor)+"();"+_eol);
            code.append("DEAD1(0);" + _eol);
            // code.append(helper.generateFireCode());
            //code.append(helper.generatePostfireCode());
            code.append(_getActorName(actor)+"_driver_out();// output values to ports deterministically"+_eol);
            code.append("}" + _eol); // end of for loop

            code.append("#endif /* THREAD_" + threadID + "*/\n");
            threadID++;
        }
        return code.toString();
    }

    private String _generatePortVariableDeclarations(Actor actor) throws IllegalActionException {

        System.out.println("get Port Variable Declarations called");
        StringBuffer code = new StringBuffer();

        Iterator outputPorts = actor.outputPortList()
        .iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            //if (!outputPort.isOutsideConnected() || outputPort.isInsideConnected()) {
            if(true){
                //not sure how this will work for multiports yet

                code.append("/*"+_getActorName(actor)+ "'s PORT variable declarations.*/"+_eol);
                code.append("volatile " + targetType(outputPort.getType()) + " * "
                        + generateName(outputPort)+"_PORT = ("+targetType(outputPort.getType()) + " * ) 0x"+Integer.toHexString(_getThenIncrementCurrentSharedMemoryAddress()));

                if (outputPort.isMultiport()) {
                    code.append("[" + outputPort.getWidthInside() + "]");
                }

                int bufferSize = getBufferSize(outputPort);

                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
            else
            {
                System.out.println("didn't match if");
            }

            // return "should define port here from CCodeGEneratorHelper";
        }
        //System.out.println("about to return: "+code.toString());
        return code.toString();
    }

    private int _getFrequency(Actor actor) throws IllegalActionException {
        Attribute frequency = ((Entity)actor).getAttribute("frequency");
        if (frequency == null) {
            return 1;
        } else {
            return ((IntToken) ((Variable) frequency).getToken()).intValue();
        }
    }

    private double _getPeriod() throws IllegalActionException {

        /*
         * Director = _director.container.director
         * while (!director instanceof giottodirector) {
         *   if (director.container.director exists)
         *     director = director.container.director
         *   else return periodValue attribute
         * }
         * return director._getPeriod() * director.container."frequency"
         */

        Director director = ((TypedCompositeActor)
                _director.getContainer()).getExecutiveDirector();

        while (director != null &&
                !(director instanceof ptolemy.domains.giotto.kernel.GiottoDirector)) {
            director = ((TypedCompositeActor)
                    director.getContainer()).getExecutiveDirector();
        }
        if (director == null) {
            // This is the case for the outer-most Giotto director.
            Attribute period = _director.getAttribute("period");
            double periodValue;

            if (period != null) {
                periodValue = ((DoubleToken) ((Variable) period).getToken())
                .doubleValue();
            } else {
                throw new IllegalActionException(this, "There is no period" +
                "specified in the outer-most Giotto director");
            }
            return periodValue;            
        }

        // Get the frequency.
        int frequency = _getFrequency((Actor)_director.getContainer());
        return ((GiottoDirector) _getHelper(director))._getPeriod() / frequency;

    }

    private String _getActorName(Actor actor) {
        String actorFullName = actor.getFullName();
        actorFullName = actorFullName.substring(1,actorFullName.length());
        actorFullName = actorFullName.replace('.', '_');
        actorFullName = actorFullName.replace(' ', '_');
        return actorFullName;
    }

    /** Generate the content of a driver methods. Copy outputs variables to their output ports,
     *  and inputs from a port the the input variable. The PORT allows double buffering.
     *  PORT here is simply a common variable, not a PORT in the general Ptolemy II actor sense
     *  @param none
     *  @return code that copies outputs to a port, and inputs from a port in a driver method
     */ 
//    String _generateDriverCode() throws IllegalActionException {
//        StringBuffer code = new StringBuffer();
//        System.out.println("generateDriver Code has been called");
//
//        for (Actor actor : (List<Actor>) 
//                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
//            System.out.println("actor to check is "+actor.getDisplayName());
//
//            List outputPortList = actor.outputPortList();
//            Iterator outputPorts = outputPortList.iterator();
//
//            String actorDriverCode = "";
//            // this is a hack to try to copy the output of the actor to the port in the driver code
//            CodeGeneratorHelper myHelper;
//            //this is currently a hack. let's see how long it continues to work
//            /*if(outputPorts .hasNext())
//            {
//               actorDriverCode+=_getActorName(actor)+"_output_0_PORT = "+_getActorName(actor)+"_output_0;"+_eol;
//
//            }
//             */
//            while (outputPorts.hasNext()) {
//                System.out.println("the actor has output ports");
//                IOPort port = (IOPort) outputPorts.next();
//                Receiver[][] channelArray = port.getRemoteReceivers();
//
//                for (int i = 0; i < channelArray.length; i++) {
//                    Receiver[] receiverArray = channelArray[i];
//                    
//
//                    for (int j = 0; j < receiverArray.length; j++) {
//                      
//                        GiottoReceiver receiver = (GiottoReceiver) receiverArray[j];
//                        IOPort sinkPort = receiver.getContainer();
//
//                        ArrayList args = new ArrayList();
//
//                        // FIXME: figure out the channel number for the sinkPort.
//                        String channelOffset [] = {"0","0"};
//                        myHelper = (CodeGeneratorHelper) this._getHelper(sinkPort.getContainer());
//                        String sinkReference = this.getReference((TypedIOPort)sinkPort,channelOffset,false,true,myHelper);//"##ref(sinkPort)";
//
//                        channelOffset[0]= Integer.valueOf(i).toString();
//                        myHelper = (CodeGeneratorHelper)_getHelper(actor);
//                        String srcReference = this.getReference((TypedIOPort)port,channelOffset,false,false,myHelper);//"##ref(sinkPort)";
//
//
//                        args.add(sinkReference);
//                        args.add(srcReference);
//
//                        actorDriverCode += _generateBlockCode("updatePort", args);
//
//                    }
//                }
//            }
//            System.out.println("actorDriverCode is now:");
//            System.out.println(actorDriverCode);
//            // if(inputPortList.size() > 1)
//            //  if(actorDriverCode.length() >= 1) // not sure if this is the correct check
//            {// for now generate driver methods for all the actors
//                ArrayList args = new ArrayList();
//                args.add(_generateDriverName((NamedObj) actor));
//
//                CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
//
//
//                String temp = helper.generateTypeConvertFireCode();
//                // this was there originally. Will need to modify the 
//                //TypeConvertFireCode method or create method for the actor in this file 
//                //to do type conversion and append the port as well
//
//                args.add(actorDriverCode);
//                /*if(temp.length()== 0)
//                {
//                    args.add(actorDriverCode);
//                }
//                else
//                {
//                    args.add(temp);
//                    System.out.println("temp was added as the argument to generate block code");
//                }*/
//                code.append(_generateBlockCode("driverCode", args));
//            }
//        }
//        System.out.println("about to return :");
//        System.out.println(code.toString());
//        return code.toString();
//    }

    private static String _generateDriverName(NamedObj namedObj)
    {
        String name = StringUtilities.sanitizeName(namedObj.getFullName());
        // FIXME: Assume that all objects share the same top level. In this case,
        // having the top level in the generated name does not help to
        // expand the name space but merely lengthen the name string.
        //        NamedObj parent = namedObj.toplevel();
        //        if (namedObj.toplevel() == namedObj) {
        //            return "_toplevel_";
        //        }        
        //        String name = StringUtilities.sanitizeName(namedObj.getName(parent));
        if (name.startsWith("_")) {
            name = name.substring(1, name.length());
        }
        return name.replaceAll("\\$", "Dollar")+"_driver";

    }


    private String _generateActorsCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        System.out.println("generateActors Code has been called");

        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {

            /*
          Iterator actors = ((CompositeActor) _director.getContainer())
          .deepEntityList().iterator();

          while (actors.hasNext()) {
              Actor actor = (Actor) actors.next();
             */
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            //Iterators localActors =((CompositeActor)actor.()
            //to do
            // how do you access the actors in a composite actor

            String actorFullName = _getActorName(actor);    
            System.out.println("I have an actor named "+actorFullName+" going to generate fireCode for it now.");

            code.append(_eol + "void " + actorFullName+ _getFireFunctionArguments() + " {"
                    + _eol);
            //code.append(actorHelper.generateFireFunctionCode());
            code.append(actorHelper.generateFireCode());
            //code.append("$actorSymbol()");
            //code.append("//created by director "+_director.getDisplayName()+_eol);
            code.append("}" + _eol);

        }
        return code.toString();




    }
    
    private boolean _isTopDirectorFSM()
    {
        boolean returnValue = false;
        
        
        
        Director director = ((TypedCompositeActor) _director.getContainer()).getExecutiveDirector();
    
        if(director != null &&
                (director instanceof ptolemy.domains.fsm.kernel.FSMDirector)) {
           returnValue = true;
           }
            
        
        return returnValue;
        
        
    }
    /** Generate the content of a driver methods. For each actor update it's inputs to the 
     *  outputs stored in ports. The PORT allows double buffering, in this case the output
     *  variable is used as the port. PORT here is simply a common variable, not a PORT in 
     *  the general Ptolemy II actor sense
     *  
     *  NOTE: Duplicate ports connected through a fork are removed. IE. if an input is connected to a fork
     *  and the fork is connected to two other places... it removes the first place from the list of places and keeps the last place
     *  need to ask Jackie if there is a way to work around this b/c Reciever [][] recievers = getRecievers doesn't work.
     *  @param none
     *  @return code that copies outputs to a port, and inputs from a port in a driver method
     */ 
    public String _generateInDriverCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        System.out.println("generateDriver Code has been called");
        
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
    
            List inputPortList = actor.inputPortList();
            System.out.println("this actor"+actor.getDisplayName()+" has "+inputPortList.size()+" input ports.");
            Iterator inputPorts = inputPortList.iterator();
    
            String actorDriverCode = "";
            String sinkReference = "";
            String srcReference = "";
            String temp = "";
            StringBuffer transferIn= new StringBuffer();
            StringBuffer transferOut=new StringBuffer();
            String output="";
            int i = 0; //sink index counter
            int j = 0; // src index counter
            CodeGeneratorHelper myHelper;
            
            
           
            
            
            while (inputPorts.hasNext()) {
                i = 0;  // this is a test to see if this is to be done here, if so remove the i++ from the end of the loop
                j = 0;
                 IOPort port = (IOPort)inputPorts.next();
                 System.out.println("this port's name is "+port.getFullName());
                 Receiver[][] channelArray = port.getReceivers();
                // port.
                List<IOPort> cip = port.sourcePortList();
                if(cip.size()>0)
                {
                    System.out.println("sourcePortList contains: ");
                    Iterator tome2 =cip.iterator();
                    while(tome2.hasNext()){
                        IOPort tempp = (IOPort)tome2.next();
                      System.out.print(tempp.getFullName()+" ");  
            
                   }
                    System.out.println(" ");
                }
                
                 
                List<IOPort> connectedPorts = port.deepConnectedOutPortList();
                List<IOPort> connectToMe = port.sourcePortList();//port.connectedPortList(); //connectedPortList();
                System.out.println("connectToMe size is "+connectToMe.size());
                //System.out.println("before remove double connections");
          
                Iterator tome= connectToMe.iterator();
              System.out.println("currently connectToMe size is "+connectToMe.size());
                
                tome= connectToMe.iterator();
                while(tome.hasNext())
                {
                    IOPort tempp = (IOPort)tome.next();
                    System.out.println("******I'm connected to I think: "+tempp.getFullName());  
                }
                 
                // Iterator cpIterator = connectedPorts.iterator();
                Iterator cpIterator = connectToMe.iterator();
                 while(cpIterator.hasNext()){//&&(j <connectToMe.size()-1)){
                   IOPort sourcePort = (IOPort)cpIterator.next();
                // FIXME: figure out the channel number for the sourcePort.
                   // if you need to transfer inputs inside
                   if(actor instanceof CompositeActor) {
                      System.out.println("composite actor so doing stuff for that");
                       //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                       //_generateTransferInputsCode(port, transferIn);
                       transferIn.append(("//should transfer input for this actor to from the outside to inside"+_eol));
                       //generateTransferInputsCode(inputPort, code);
                       
                   }
                
                   System.out.println(" j is "+j +"and size of connect to me is "+connectToMe.size());
                   String channelOffset [] = {"0","0"};
                 
                     System.out.println("the sender port is named "+sourcePort.getFullName()+" and the reciever is "+port.getFullName());
                     myHelper = (CodeGeneratorHelper)this._getHelper(sourcePort.getContainer());
                    // temp+= _generateTypeConvertFireCode(false)+_eol;
                   channelOffset[0] = Integer.valueOf(i).toString();
                   System.out.println("channel offset is "+channelOffset[0]);
                     srcReference = this.driverGetReference((TypedIOPort)sourcePort,channelOffset,false,true,myHelper);
                     System.out.println("after first call to getReference");
                                         
                     myHelper = (CodeGeneratorHelper)_getHelper(actor);
                     channelOffset[0] = Integer.valueOf(j).toString();
                     System.out.println("channel offset is "+channelOffset[0]);
                     sinkReference = this.driverGetReference((TypedIOPort)port,channelOffset,false,true,myHelper);
                     System.out.println("after second call to getReference");
                     j++;
                     
                     temp+= _generateTypeConvertFireCode(sourcePort,port);//+_eol;                 
                     System.out.println("I think the source Reference is "+srcReference+" and it's display name is "+sourcePort.getDisplayName());
                     System.out.println("I think the sink Reference is "+sinkReference+" and it's display name is "+port.getDisplayName());
        
                     ArrayList args = new ArrayList();    
                      args.add(sinkReference);
                      args.add(srcReference);
                      
                      actorDriverCode += _generateBlockCode("updatePort", args);
                     }
                 
                 if(actor instanceof CompositeActor) {
                   // It is not necessary to generate transfer out code, 
                     //since the fanout actor drivers will read the necessary values from the ports                    
                     //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                     //directorHelper._generateTransferOutputsCode(port, transferOut);
                     
                     //generateTransferInputsCode(inputPort, code);
                     //transferOut.append("//should transfer input for this actor to from inside to outside"+_eol);
                 }
                 
             i++; // increment the ofset variable // not sure if this is correct since we're using iterators 
            } 
            
            System.out.println("actorDriverCode is now:");
            System.out.println(actorDriverCode);
            
           
            ArrayList args = new ArrayList();  
            args.add(_generateDriverName((NamedObj) actor)+"_in");
           if(temp.length() == 0)   // if no type conversion is necessary
            output=transferIn.toString()+actorDriverCode+transferOut.toString();
           else
               output=transferIn.toString()+temp+transferOut.toString();
          
           args.add(output);
            code.append(_generateBlockCode("driverCode", args));
            }
        
        System.out.println("about to return :");
        System.out.println(code.toString());
        return code.toString();
    }
 
    
    public String _generateOutDriverCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        String channelOffset [] = {"0","0"};
        
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
    
            List outputPortList = actor.outputPortList();
            System.out.println("this actor"+actor.getDisplayName()+" has "+outputPortList.size()+" output ports.");
            Iterator outPorts = outputPortList.iterator();
    
            String actorDriverCode = "";
            String sinkReference = "";
            String srcReference = "";
            String temp = "";
            StringBuffer transferIn= new StringBuffer();
            StringBuffer transferOut=new StringBuffer();
            String output="";
            int i = 0; //sink index counter
            int j = 0; // src index counter
            CodeGeneratorHelper myHelper;
            while (outPorts.hasNext()) {
                i = 0;  // this is a test to see if this is to be done here, if so remove the i++ from the end of the loop
                j = 0;
               IOPort port = (IOPort)outPorts.next();
               
               myHelper = (CodeGeneratorHelper)_getHelper(actor);
                 
               sinkReference = this.driverGetReference((TypedIOPort)port,channelOffset,false,true,myHelper);
               srcReference = this.getReference((TypedIOPort)port,channelOffset,false,true,myHelper);
               
               //temp+= _generateTypeConvertFireCode(sourcePort,port);//+_eol;                 
               //System.out.println("I think the source Reference is "+srcReference+" and it's display name is "+sourcePort.getDisplayName());
               //System.out.println("I think the sink Reference is "+sinkReference+" and it's display name is "+port.getDisplayName());
  
               ArrayList args = new ArrayList();    
                args.add(sinkReference);
                args.add(srcReference);
                
                actorDriverCode += _generateBlockCode("updatePort", args);
              
            }
          
            ArrayList args = new ArrayList();  
            args.add(_generateDriverName((NamedObj) actor)+"_out");
           
           args.add(actorDriverCode);
            code.append(_generateBlockCode("driverCode", args));
        }
        return code.toString();
    }
    

     
     /**
      * Generate the type conversion fire code. This method is called by the
      * Director to append necessary fire code to handle type conversion.
      * @param forComposite True if we are generating code for a composite.
      * @return The generated code.
      * @exception IllegalActionException Not thrown in this base class.
      */
     public String _generateTypeConvertFireCode(IOPort source,IOPort sink)
     throws IllegalActionException {
         StringBuffer code = new StringBuffer();
         System.out.println("generateTypeConvertFireCode in OpenRTOS giotto director called");
         //code.append("//generateTypeConvertFireCode in OpenRTOS giotto director called");
         //not 100% sure what this should contain yet
        
//         CodeGeneratorHelper helper = (CodeGeneratorHelper)_getHelper(source.getContainer());
//         // Type conversion code for inter-actor port conversion.
//         for(int i = 0; i< source.getWidth();i++){
//             Iterator channels = helper.getSinkChannels(sink, i).iterator();
//             while (channels.hasNext()) {
//                 Channel srcChan = (Channel) channels.next();
 //    
//                 if (source.isOutput() || source.isInput()) {
 //    
//                     Iterator sinkChannels = _getTypeConvertSinkChannels(srcChan)
//                     .iterator();
 //    
//                     while (sinkChannels.hasNext()) {
//                         Channel sinkChan = (Channel) sinkChannels.next();
//                         code.append(_generateTypeConvertStatements(srcChan, sinkChan));
//                     }
//                 }
//             }
//         }
         return code.toString();
     }
     
   

    int _getThenIncrementCurrentSharedMemoryAddress() throws IllegalActionException {
        currentSharedMemoryAddress += 32;
        if(currentSharedMemoryAddress >= 0x40000000) {
            throw new IllegalActionException("out of shared data space on PRET");
        }
        return currentSharedMemoryAddress-32;

    }

    static private int currentSharedMemoryAddress = 0x3F800000;


}
