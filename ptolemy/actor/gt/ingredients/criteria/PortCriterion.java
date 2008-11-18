/*

 Copyright (c) 1997-2008 The Regents of the University of California.
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
package ptolemy.actor.gt.ingredients.criteria;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.util.PtolemyExpressionString;
import ptolemy.actor.gt.util.RegularExpressionString;
import ptolemy.data.expr.Constants;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class PortCriterion extends Criterion {

    public PortCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    public PortCriterion(GTIngredientList owner, String values) {
        this(owner, null, null, false, false, false, "");
        setValues(values);
    }

    public PortCriterion(GTIngredientList owner, String portName,
            String portType, boolean input, boolean output, boolean multiport,
            String matcherName) {
        super(owner, 6);

        NamedObj container = owner.getOwner().getContainer();
        _portName = new RegularExpressionString(portName);
        _portType = new PtolemyExpressionString(container, portType);
        _input = input;
        _output = output;
        _multiport = multiport;
        _matcherName = matcherName;
    }

    public boolean canCheck(NamedObj object) {
        return super.canCheck(object) && object instanceof Port;
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    public String getMatcherName() {
        return _matcherName;
    }

    public String getPortID(GTIngredientList list) {
        if (!isMatcherNameEnabled() || _matcherName.equals("")) {
            int position = list.indexOf(this);
            return _getUniqueName(list, "criterion" + (position + 1));
        } else {
            return _getUniqueName(list, _matcherName);
        }
    }

    public String getPortName() {
        return _portName.get();
    }

    public String getPortType() {
        return _portType.get();
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _portName;
        case 1:
            return _portType;
        case 2:
            return _input;
        case 3:
            return _output;
        case 4:
            return _multiport;
        case 5:
            return _matcherName;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _portName.get());
        _encodeStringField(buffer, 1, _portType.get());
        _encodeBooleanField(buffer, 2, _input);
        _encodeBooleanField(buffer, 3, _output);
        _encodeBooleanField(buffer, 4, _multiport);
        _encodeStringField(buffer, 5, _matcherName);
        return buffer.toString();
    }

    public boolean isApplicable(NamedObj object) {
        return super.isApplicable(object) && object instanceof ComponentEntity
                && !(object instanceof State);
    }

    public boolean isInput() {
        return _input;
    }

    public boolean isInputEnabled() {
        return isEnabled(2);
    }

    public boolean isMatcherNameEnabled() {
        return isEnabled(5);
    }

    public boolean isMultiport() {
        return _multiport;
    }

    public boolean isMultiportEnabled() {
        return isEnabled(4);
    }

    public boolean isOutput() {
        return _output;
    }

    public boolean isOutputEnabled() {
        return isEnabled(3);
    }

    public boolean isPortNameEnabled() {
        return isEnabled(0);
    }

    public boolean isPortTypeEnabled() {
        return isEnabled(1);
    }

    public boolean match(NamedObj object) {
        // Check the input/output/multiport type
        if (object instanceof IOPort) {
            IOPort port = (IOPort) object;
            if (isInputEnabled() && _input != port.isInput()) {
                return false;
            } else if (isOutputEnabled() && _output != port.isOutput()) {
                return false;
            } else if (isMultiportEnabled() && _multiport != port.isMultiport()) {
                return false;
            }
        } else if (object instanceof Port) {
            if (isInputEnabled() || isOutputEnabled() || isMultiportEnabled()) {
                return false;
            }
        } else {
            return false;
        }

        // Check port name
        if (isPortNameEnabled()) {
            Pattern pattern = _portName.getPattern();
            Matcher matcher = pattern.matcher(object.getName());
            if (!matcher.matches()) {
                return false;
            }
        }

        // Check port type
        if (isPortTypeEnabled()) {
            if (object instanceof TypedIOPort) {
                TypedIOPort typedIOPort = (TypedIOPort) object;
                try {
                    Type lhsType = _portType.getToken().getType();
                    Type hostType = typedIOPort.getType();
                    boolean isTypeCompatible = true;
                    if (isInputEnabled() && _input) {
                        isTypeCompatible = isTypeCompatible
                                && hostType.isCompatible(lhsType);
                    }
                    if (isOutputEnabled() && _output) {
                        isTypeCompatible = isTypeCompatible
                                && lhsType.isCompatible(hostType);
                    }
                    if (!isTypeCompatible) {
                        return false;
                    }
                } catch (IllegalActionException e) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setInputEnabled(boolean enabled) {
        setEnabled(2, enabled);
    }

    public void setMatcherNameEnabled(boolean enabled) {
        setEnabled(5, enabled);
    }

    public void setMultiportEnabled(boolean enabled) {
        setEnabled(4, enabled);
    }

    public void setOutputEnabled(boolean enabled) {
        setEnabled(3, enabled);
    }

    public void setPortNameEnabled(boolean enabled) {
        setEnabled(0, enabled);
    }

    public void setPortTypeEnabled(boolean enabled) {
        setEnabled(1, enabled);
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _portName.set((String) value);
            break;
        case 1:
            _portType.set((String) value);
            break;
        case 2:
            _input = ((Boolean) value).booleanValue();
            break;
        case 3:
            _output = ((Boolean) value).booleanValue();
            break;
        case 4:
            _multiport = ((Boolean) value).booleanValue();
            break;
        case 5:
            _matcherName = (String) value;
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _portName.set(_decodeStringField(0, fieldIterator));
        _portType.set(_decodeStringField(1, fieldIterator));
        _input = _decodeBooleanField(2, fieldIterator);
        _output = _decodeBooleanField(3, fieldIterator);
        _multiport = _decodeBooleanField(4, fieldIterator);
        _matcherName = _decodeStringField(5, fieldIterator);
    }

    public void validate() throws ValidationException {
        if (isPortNameEnabled()) {
            if (_portName.get().equals("")) {
                throw new ValidationException("Port name must not be empty.");
            }

            try {
                _portName.getPattern();
            } catch (PatternSyntaxException e) {
                throw new ValidationException("Regular expression \""
                        + _portName + "\" cannot be compiled.", e);
            }
        }

        if (isPortTypeEnabled()) {
            if (_portType.get().equals("")) {
                throw new ValidationException("Port type must not be empty.");
            }

            try {
                _portType.getToken().getType();
            } catch (IllegalActionException e) {
                throw new ValidationException("Type expression \"" + _portType
                        + "\" cannot be parsed.", e);
            }
        }
    }

    private String _getUniqueName(GTIngredientList list, String name) {
        int pos = 1;
        String id = null;
        boolean success = false;
        while (!success) {
            success = true;
            id = pos >= 2 ? name + pos : name;
            Iterator<GTIngredient> iterator = list.iterator();
            while (iterator.hasNext()) {
                GTIngredient ingredient = iterator.next();
                if (ingredient == this) {
                    break;
                } else if (ingredient instanceof PortCriterion) {
                    PortCriterion portCriterion = (PortCriterion) ingredient;
                    String portId = portCriterion.getPortID(list);
                    if (portId.equals(id)) {
                        success = false;
                        break;
                    }
                }
            }
        }
        return id;
    }

    private static final CriterionElement[] _ELEMENTS = {
            new StringCriterionElement("name", true, true, false),
            new ChoiceCriterionElement("type", true, false, true, true),
            new BooleanCriterionElement("input", true),
            new BooleanCriterionElement("output", true),
            new BooleanCriterionElement("multi", true),
            new StringCriterionElement("matcherName", true, false, false) };

    private boolean _input;

    private String _matcherName;

    private boolean _multiport;

    private boolean _output;

    private RegularExpressionString _portName;

    private PtolemyExpressionString _portType;

    static {
        ChoiceCriterionElement portTypes = (ChoiceCriterionElement) _ELEMENTS[1];
        portTypes.addChoices(Constants.types().keySet());
        portTypes.addChoice("arrayType(int)");
        portTypes.addChoice("arrayType(int,5)");
        portTypes.addChoice("[double]");
        portTypes.addChoice("{x=double, y=double}");
    }
}
