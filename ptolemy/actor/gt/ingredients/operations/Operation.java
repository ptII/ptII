/*

 Copyright (c) 1997-2009 The Regents of the University of California.
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

package ptolemy.actor.gt.ingredients.operations;

import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.Replacement;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeWriter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParserTreeConstants;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class Operation extends GTIngredient {

    /**
     * @param owner
     * @param elementCount
     */
    public Operation(GTIngredientList owner, int elementCount) {
        super(owner, elementCount);
    }

    public abstract ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            NamedObj patternObject, NamedObj replacementObject,
            NamedObj hostObject) throws IllegalActionException;

    protected ASTPtLeafNode _evaluate(ASTPtRootNode node,
            ParseTreeEvaluator evaluator, ParserScope scope)
            throws IllegalActionException {
        Token token = evaluator.evaluateParseTree(node, scope);
        ASTPtLeafNode newNode = new ASTPtLeafNode(
                PtParserTreeConstants.JJTPTLEAFNODE);
        newNode.setToken(token);
        newNode.setType(token.getType());
        newNode.setConstant(true);
        return newNode;
    }

    protected ParseTreeWriter _parseTreeWriter = new ParseTreeWriter() {

        public void visitLeafNode(ASTPtLeafNode node)
        throws IllegalActionException {
            if (node.isConstant() && node.isEvaluated()) {
                Token token = node.getToken();
                if (token instanceof StringToken) {
                    // For StringToken, call stringValue instead of toString to
                    // avoid having an extra pair of quotes.
                    _writer.write(((StringToken) token).stringValue());
                    return;
                }
            }
            super.visitLeafNode(node);
        }
    };

}
