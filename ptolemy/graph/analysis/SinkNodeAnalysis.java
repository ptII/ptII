/* Computation of sink nodes in a graph.

 Copyright (c) 2002-2010 The University of Maryland. All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 */
package ptolemy.graph.analysis;

import java.util.List;

import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.SinkNodeAnalyzer;
import ptolemy.graph.analysis.strategy.SinkNodeStrategy;

///////////////////////////////////////////////////////////////////
//// SinkNodeAnalysis

/**
 Computation of sink nodes in a graph.
 A sink node in a graph is a node without output edges.
 <p>
 The returned collection cannot be modified when the client uses the default
 strategy.
 <p>
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class SinkNodeAnalysis extends Analysis {
    /** Construct an instance of this class for a given graph.
     *
     *  @param graph The given graph.
     */
    public SinkNodeAnalysis(Graph graph) {
        super(new SinkNodeStrategy(graph));
    }

    /** Construct an instance of this class using a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public SinkNodeAnalysis(SinkNodeAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the sink nodes in the graph under analysis.
     *  Each element of the list is an {@link ptolemy.graph.Node}.
     *
     *  @return Return the sink nodes.
     */
    public List nodes() {
        return ((SinkNodeAnalyzer) analyzer()).nodes();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    public String toString() {
        return "Sink node analysis using the following analyzer:\n"
                + analyzer().toString();
    }

    /** Check if a given analyzer is compatible with this analysis.
     *  In other words if it is possible to use it to compute the computation
     *  associated with this analysis.
     *
     *  @param analyzer The given analyzer.
     *  @return True if the given analyzer is valid for this analysis.
     */
    public boolean validAnalyzerInterface(Analyzer analyzer) {
        return analyzer instanceof SinkNodeAnalyzer;
    }
}
