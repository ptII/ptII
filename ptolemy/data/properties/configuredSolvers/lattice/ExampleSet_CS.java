package ptolemy.data.properties.configuredSolvers.lattice;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public class ExampleSet_CS extends PropertyConstraintSolver {

    public ExampleSet_CS(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        propertyLattice.setExpression("exampleSetLattice");
        propertyLattice.setVisibility(Settable.NOT_EDITABLE);

        solvingFixedPoint.setExpression("least");
        solvingFixedPoint.setVisibility(Settable.NOT_EDITABLE);

        actorConstraintType.setExpression("out >= in");
        actorConstraintType.setVisibility(Settable.NOT_EDITABLE);

        connectionConstraintType.setExpression("sink >= src");
        connectionConstraintType.setVisibility(Settable.NOT_EDITABLE);

        compositeConnectionConstraintType.setExpression("sink >= src");
        compositeConnectionConstraintType.setVisibility(Settable.NOT_EDITABLE);

        expressionASTNodeConstraintType.setExpression("parent >= child");
        expressionASTNodeConstraintType.setVisibility(Settable.NOT_EDITABLE);

        fsmConstraintType.setExpression("sink >= src");
        fsmConstraintType.setVisibility(Settable.NOT_EDITABLE);

        // Add default highlight colors
//        StringAttribute highlightUnknownProperty = new StringAttribute(_highlighter, "unknown");
//        highlightUnknownProperty.setExpression("Unknown");
//        ColorAttribute highlightUnknownColor = new ColorAttribute(_highlighter, "unknownHighlightColor");
//        highlightUnknownColor.setExpression("{0.0,0.0,0.0,1.0}");
//
//        StringAttribute highlightTrueProperty = new StringAttribute(_highlighter, "true");
//        highlightTrueProperty.setExpression("True");
//        ColorAttribute highlightTrueColor = new ColorAttribute(_highlighter, "trueHighlightColor");
//        highlightTrueColor.setExpression("{0.0,0.8,0.2,1.0}");
//
//        StringAttribute highlightFalseProperty = new StringAttribute(_highlighter, "false");
//        highlightFalseProperty.setExpression("False");
//        ColorAttribute highlightFalseColor = new ColorAttribute(_highlighter, "falseHighlightColor");
//        highlightFalseColor.setExpression("{0.0,0.2,1.0,1.0}");
    }

}
