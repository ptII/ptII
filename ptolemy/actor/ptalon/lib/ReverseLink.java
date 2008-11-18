/* Reverse links for a Map/Reduce demo

 Copyright (c) 2006-2007 The Regents of the University of California.
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

package ptolemy.actor.ptalon.lib;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

//////////////////////////////////////////////////////////////////////////
//// ReverseLink

/**
 Reverse a link in the Map/Reduce demo.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ReverseLink extends MapReduceAlgorithm {

    public List<KeyValuePair> map(String key, String value) {
        StringTokenizer tokenizer = new StringTokenizer(value);
        LinkedList<KeyValuePair> output = new LinkedList<KeyValuePair>();
        while (tokenizer.hasMoreTokens()) {
            output.add(new KeyValuePair(tokenizer.nextToken(), key));
        }
        return output;
    }

    public List<String> reduce(String key, BlockingQueue<String> values)
            throws InterruptedException {
        List<String> output = new LinkedList<String>();
        while (!isQueueEmpty()) {
            String value = values.take();
            if (isQueueEmpty()) {
                break;
            }
            output.add(value);
        }
        return output;
    }

}
