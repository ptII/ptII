/* An utility function for traversing the system and generate files for model checking.

 Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.verification.kernel.maude;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Clock;
import ptolemy.data.expr.Variable;
import ptolemy.domains.de.lib.TimedDelay;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.fsm.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.verification.lib.BoundedBufferNondeterministicDelay;

// ////////////////////////////////////////////////////////////////////////
// //RTMaudeUtility

/**
 * This is an utility function for Ptolemy II models. It performs a systematic
 * traversal of the system and generate Realtime Maude model file
 *
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (kbae4)
 * @Pt.AcceptedRating Red (kbae4)
 */
public class RTMaudeUtility {

    /**
     * Return a StringBuffer that contains the converted .maude format of the
     * system.
     *
     * @param model The system under analysis.
     * @param formula

     * @return The converted .maude format of the system.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public static StringBuffer generateRTMDescription(CompositeActor model,
            String formula, boolean inlineFilesIfPossible)
            throws IllegalActionException, NameDuplicationException {

        // RTM initial state for current Ptolemy model (DE)
        RTMList topconf = _translateCompositeEntity(model, null);

        StringBuffer returnRTMFormat = new StringBuffer();

        _loadSemanticFiles(returnRTMFormat, inlineFilesIfPossible);

        returnRTMFormat.append("\n");
        _generateModelBody(returnRTMFormat, model.getName(), topconf);
        returnRTMFormat.append("\n");
        _generateFormula(returnRTMFormat, formula);
        returnRTMFormat.append("\n");

        return returnRTMFormat;
    }

    /**
     * Return a StringBuffer that contains the converted .maude format of the
     * system.
     *
     * @param model The system under analysis.
     * @param formula

     * @return The converted .maude format of the system.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public static StringBuffer generateRTMDescription(BufferedReader template,
            CompositeActor model, String formula) throws IllegalActionException,
            NameDuplicationException {

        // RTM initial state for current Ptolemy model (DE)
        RTMList topconf = _translateCompositeEntity(model, null);

        StringBuffer returnRTMFormat = new StringBuffer();

        String line;
        boolean beginModel = false;
        boolean beginFormula = false;
        try {
            do {
                line = template.readLine();
                if (line != null) {
                    String lineTrimed = line.trim();
                    if (beginModel || beginFormula) {
                        if (lineTrimed.replace("-", "").trim().equals("")) {
                            // A ----- line
                            returnRTMFormat.append(line);
                            returnRTMFormat.append("\n");

                            do {
                                line = template.readLine();
                                if (line == null || line.replace("-", "").trim()
                                        .equals("")) {
                                    break;
                                }
                            } while (true);
                        }
                        if (beginModel) {
                            _generateModelBody(returnRTMFormat,
                                    model.getName(), topconf);
                            beginModel = false;
                        } else {
                            _generateFormula(returnRTMFormat, formula);
                            beginFormula = false;
                        }
                    } else if (lineTrimed.equals("")) {
                        if (beginModel) {
                            _generateModelBody(returnRTMFormat, model.getName(),
                                    topconf);
                            beginModel = false;
                            continue;
                        } else if (beginFormula) {
                            _generateFormula(returnRTMFormat, formula);
                            beginFormula = false;
                            continue;
                        }
                    } else if (lineTrimed.startsWith("---") &&
                            lineTrimed.endsWith("---") &&
                            lineTrimed.substring(3, lineTrimed.length() - 3)
                                    .trim().equalsIgnoreCase("Model Begin")) {
                        beginModel = true;
                        beginFormula = false;
                    } else if (lineTrimed.startsWith("---") &&
                            lineTrimed.endsWith("---") &&
                            lineTrimed.substring(3, lineTrimed.length() - 3)
                                    .trim().equalsIgnoreCase("Formula Begin")) {
                        beginModel = false;
                        beginFormula = true;
                    }
                    returnRTMFormat.append(line);
                    returnRTMFormat.append("\n");
                }
                if (line == null) {
                    break;
                }
            } while (true);
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Unable to read template");
        }

        return returnRTMFormat;
    }

    private static void _generateFormula(StringBuffer returnRTMFormat,
            String formula) {
        if (formula != null && formula.trim().length() > 0)    // model check command
            returnRTMFormat.append("(mc {init} |=u "  + formula + " .)\n");
    }

    private static void _generateModelBody(StringBuffer returnRTMFormat,
            String modelName, RTMList topconf) {
        returnRTMFormat.append("(tomod " + modelName + "-RTM-INIT is\n");
        returnRTMFormat.append("  inc INIT + PTOLEMY-MODELCHECK .\n");
        returnRTMFormat.append("  eq #model = \n");
        returnRTMFormat.append(topconf.print(0, false) + " .\n");
        returnRTMFormat.append("endtom)\n");
    }

    private static void _loadSemanticFiles(StringBuffer buffer,
            boolean inline) throws IllegalActionException {
        if (inline) {
            ClassLoader loader = RTMaudeUtility.class.getClassLoader();
            InputStream stream = loader.getResourceAsStream(SEMANTIC_FILE_PATH +
                    "/ptolemy-modelcheck.maude");
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream));
            Stack<BufferedReader> readerStack = new Stack<BufferedReader>();
            readerStack.push(null);
            while (!readerStack.isEmpty()) {
                try {
                    String line = reader.readLine();
                    boolean skip = false;
                    while (line != null) {
                        String trim = line.trim();
                        if (trim.startsWith("load ")) {
                            trim = trim.substring(5).trim();
                            stream = loader.getResourceAsStream(
                                    SEMANTIC_FILE_PATH + "/" + trim + ".maude");
                            if (stream != null) {
                                readerStack.push(reader);
                                reader = new BufferedReader(
                                        new InputStreamReader(stream));
                                skip = true;
                            }
                        }
                        if (skip) {
                            skip = false;
                        } else {
                            buffer.append(line);
                            buffer.append("\n");
                        }
                        line = reader.readLine();
                    }
                } catch (IOException e) {
                    throw new IllegalActionException("Unable to read file.");
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // Ignore.
                    }
                }
                reader = readerStack.pop();
            }
        } else {
            buffer.append("load ptolemy-modelcheck\n");
        }
    }

    // taking an actor, and return translation of the actor
    @SuppressWarnings("unchecked")
    private static RTMObject _translateActor(Actor act) throws IllegalActionException{
        RTMObject ret = new RTMObject(act.getName(), "Actor");

        HashSet<Actor> processedActs = new HashSet<Actor>();    // inner actor collector
        RTMList rrefines = new RTMList(",", "noRefinement");

        if (act instanceof Clock) {
            ret.setClass("Clock");
            ret.addStrAttr("period", (new RTMPtExp(((Clock)act).period.getExpression(), true)).getValue());
        }
        else if (act instanceof TimedDelay) {
            ret.setClass("Delay");
            // BoundedBufferNondeterministicDelay did not PROPERLY override "delay"
            if (act instanceof BoundedBufferNondeterministicDelay)
                ret.addStrAttr("delay", (new RTMPtExp(((BoundedBufferNondeterministicDelay)act).delay.getExpression(), true)).getValue());
            else
                ret.addStrAttr("delay", (new RTMPtExp(((TimedDelay)act).delay.getExpression(), true)).getValue());
        }
        else if (act instanceof FSMActor || act instanceof ModalModel) {
            ret.setClass("FSM-Actor");
            FSMActor target = act instanceof FSMActor ? (FSMActor) act : ((ModalModel)act).getController();
            if (act instanceof ModalModel )
                processedActs.add(((ModalModel)act).getController());

            ret.addStrAttr("currState", RTMTerm.transId(target.getInitialState().getName()));
            ret.addStrAttr("initState", RTMTerm.transId(target.getInitialState().getName()));

            // Transition & Refinement
            RTMList rtrans = new RTMList(";", "emptyTransitionSet");
            for (State s : (List<State>)target.entityList(State.class)) {
                // State refinement
                if (s.getRefinement() != null)
                    rrefines.add(procRefinements(s.getName(), s.getRefinement(),"State",processedActs));
                // Transition and its refinement
                for (Transition t :(List<Transition>)s.outgoingPort.linkedRelationList()) {
                    rtrans.add(_translateTransition(t));
                    if (t.getRefinement() != null)
                        rrefines.add(procRefinements(t.getName(), t.getRefinement(),"Transition",processedActs));
                }
            }
            ret.addAttr("transitions", rtrans);
        }
        else if (act instanceof CompositeEntity) {
            ret.setClass("CompositeActor");
        }

        // Inner variable
        //FIXME : should translate parameter, not attribute (For general actors)
        if (act instanceof FSMActor) {
            RTMList ri = new RTMList(";", "emptyMap");
            RTMOpTermGenerator ragn = new RTMOpTermGenerator("(", " |-> ", ")");
            for (Variable v : (List<Variable>)(((NamedObj)act).attributeList(Variable.class)))
                ri.add(ragn.get(
                        new RTMFragment(RTMTerm.transId(v.getName())),
                        new RTMPtExp(v.getExpression())));
            ret.addStrAttr("store","emptyMap");
            ret.addAttr("innerVariables", ri);
        }
        else {
            ret.addStrAttr("store","emptyMap");
            ret.addStrAttr("innerVariables", "emptyMap");
        }

        // Inner Actor (except for refinement actors), add refinement actor
        if (act instanceof CompositeEntity) {
            RTMList inner = _translateCompositeEntity((CompositeEntity)act, processedActs);
            ret.addAttr("innerActors", inner);
            ret.addAttr("refinements", rrefines);
        }

        // Ports
        if (act instanceof Entity) {
            RTMList portconf = new RTMList("", "none");
            for (Port p : (List<Port>)((Entity)act).portList())
                portconf.add(_translatePort(p));
            ret.addAttr("ports", portconf);
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static RTMList _translateCompositeEntity(CompositeEntity cent, HashSet<Actor> exc)
            throws IllegalActionException {

        RTMOpTermGenerator rtran = new RTMOpTermGenerator("(", " ==> ", ")");
        RTMList rent = new RTMList("", "none");
        RTMList rcons = new RTMList("", "none");

        for (Actor act : (List<Actor>)cent.entityList(Actor.class)) {
            if (exc == null || ! exc.contains(act)) {
                rent.add(_translateActor(act));

                // translate connections : Same level
                for (IOPort p : (List<IOPort>)act.outputPortList()) {
                    RTMList rports = new RTMList(";", "noPort");
                    for (Port op : (List<Port>)p.sinkPortList())
                        if (op.getContainer() != p.getContainer().getContainer())
                            rports.add(portName(null,op));
                    if (! rports.isEmpty() )
                        rcons.add( rtran.get(portName(null,p),rports) );
                }
            }
        }
        // translate connections : From outside
        for (IOPort ip : (List<IOPort>)((Actor)cent).inputPortList()) {
            RTMList rports = new RTMList(";", "noPort");
            for (Port op : (List<IOPort>)ip.insideSinkPortList())
                if (exc == null || ! exc.contains(op.getContainer()))
                    rports.add(portName(ip.getContainer(), op));
            if (! rports.isEmpty() )
                rcons.add( rtran.get( portName(null, ip), rports) );
        }
        // translate connections : To outside
        //FIXME : there would be the case --  1_inside : N_outside
        for (IOPort op : (List<IOPort>)((Actor)cent).outputPortList())
            for (Port ip : (List<IOPort>)op.insideSourcePortList())
                if (exc == null || ! exc.contains(ip.getContainer()))
                    rcons.add( rtran.get( portName(op.getContainer(), ip), portName(null, op)) );

        if (! rcons.isEmpty()) rent.add(rcons);

        return rent;
    }


    private static RTMObject _translatePort(Port port) {
        String flag = "";
        if (port instanceof IOPort) {
            IOPort ip = (IOPort) port;
            if (ip.isOutput() && ip.isInput() ) flag = "InOut";
            else if (ip.isOutput()) flag = "Out";
            else if (ip.isInput()) flag = "In";
        }
        RTMObject retPort = new RTMObject(port.getName(), flag + "Port");
        retPort.addStrAttr("status", "absent");
        retPort.addStrAttr("value", "#r(0)");
        return retPort;
    }

    @SuppressWarnings("unchecked")
    private static RTMTerm _translateTransition(Transition tr) throws IllegalActionException {
        RTMOpTermGenerator ra = new RTMOpTermGenerator("("," |-> ", ")");

        RTMOpTermGenerator retTr = new RTMOpTermGenerator("("," --> ", " {guard: ", " output: ", " set: ", "})");
        RTMList os = new RTMList(";", "emptyMap");
        RTMList ss = new RTMList(";", "emptyMap");

        for (String pt : (List<String>)tr.setActions.getDestinationNameList())
            ss.add(ra.get(
                    new RTMFragment(RTMTerm.transId(pt)),
                    new RTMPtExp(tr.setActions.getParseTree(pt))) );

        for (String ot : (List<String>)tr.outputActions.getDestinationNameList())
            os.add(ra.get(
                    new RTMFragment(RTMTerm.transId(ot)),
                    new RTMPtExp(tr.outputActions.getParseTree(ot))) );

        return retTr.get(
                new RTMFragment(RTMTerm.transId(tr.sourceState().getName())),
                new RTMFragment(RTMTerm.transId(tr.destinationState().getName())),
                new RTMPtExp(tr.getGuardExpression(), false), os, ss);
    }

    private static RTMTerm portName(NamedObj upper, Port p) {
        RTMOpTermGenerator rport = new RTMOpTermGenerator("(", " . ", ")");
        String ipname = RTMTerm.transId(p.getContainer().getName());
        if (upper != null)
            ipname = RTMTerm.transId(upper.getName()) + " . " + ipname;
        return rport.get(new RTMFragment(ipname), new RTMFragment(RTMTerm.transId(p.getName())) );
    }

    private static RTMTerm procRefinements(String name, Actor[] rfs, String identifier, HashSet<Actor> inact)
            throws IllegalActionException {
        RTMOpTermGenerator refineAct = new RTMOpTermGenerator("(", "[" + identifier + ",false]: (", "))");
        RTMList rrf = new RTMList("", "none");
        for (Actor ra : rfs) {
            inact.add(ra);
            rrf.add(_translateActor(ra));
        }

        return refineAct.get(new RTMFragment(RTMTerm.transId(name)), rrf);
    }

    private static final String SEMANTIC_FILE_PATH = "ptolemy/verification/kernel/maude/ext";

}
