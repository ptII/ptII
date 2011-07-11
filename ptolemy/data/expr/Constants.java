/* A table of named constants that are recognized by the expression parser.

 Copyright (c) 2001-2011 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.UnsizedFixToken;
import ptolemy.data.XMLToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.Complex;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// Constants

/**
 A table of named constants that are recognized by the expression parser.
 <p>
 A named constant in an expression is substituted by its associated value
 when the expression is evaluated. The constants are stored in a hash table,
 using their names as key. The value of each constant is wrapped in a data
 token.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 @see ptolemy.data.expr.PtParser
 */
public class Constants {
    // There is no need to create an instance of this class.
    private Constants() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a constant with the given name and value to the table.
     *  If a constant with this same name had been previously added,
     *  then that value is replaced with the new one.
     *  Neither the name nor the value can be null, otherwise a
     *  NullPointerException will be thrown.
     *  @param name The name of the constant.
     *  @param value The value of the constant, wrapped in a data token.
     */
    public static void add(String name, ptolemy.data.Token value) {
        _table.put(name, value);
    }

    /** Return a record representation of the constants.
     *  @return A record with the constants and their values.
     *  @since Ptolemy II 2.1
     */
    public static RecordToken constants() {
        // This should be called toString(), but we cannot have a static
        // toString() because Object.toString() is not static.
        // NOTE: Construct these arrays rather than using keySet()
        // and values() because we have no assurance that those will
        // return the contents in the same order.
        int size = _table.size();
        String[] names = new String[size];
        ptolemy.data.Token[] values = new ptolemy.data.Token[size];
        Iterator keys = _table.keySet().iterator();
        int i = 0;

        while (keys.hasNext()) {
            String key = (String) keys.next();
            names[i] = key;
            values[i] = (ptolemy.data.Token) _table.get(key);
            i++;
        }

        try {
            return new RecordToken(names, values);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Cannot construct a record!");
        }
    }

    /** Look up the value of the constant with the given name.
     *  @param name The name of the constant.
     *  @return The value of the constant, wrapped in a data token, or null
     *   if there is no constant with the given name in the table.
     */
    public static ptolemy.data.Token get(String name) {
        return (ptolemy.data.Token) _table.get(name);
    }

    /** Remove the constant with the given name from the table.
     *  If there is no constant with the given name in the table,
     *  the table is not changed.
     *  @param name The name of the constant to be removed from the table.
     */
    public static void remove(String name) {
        _table.remove(name);
    }

    /** Return a copy of the types.
     *  @return a copy of the hash table tha name types.
     */
    public static TreeMap types() {
        // We use a separate map of types so that we can list the
        // types in the port configurer dialog
        // We return a TreeMap here because we do not need the
        // synchronization in a Hashtable, but we do want it sorted.
        return new TreeMap(_types);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The hash table containing the named constants.
    private static Hashtable _table = new Hashtable();

    /** The treemap containing the named constants.
     *  We use a treemap so that we get sorting in natural order.
     */
    private static TreeMap _types = new TreeMap();

    ///////////////////////////////////////////////////////////////////
    ////                         static initializer                ////
    // Add the default set of named constants to the table.
    static {
        DoubleToken token = new DoubleToken(java.lang.Math.PI);
        _table.put("PI", token);
        _table.put("pi", token);
        token = new DoubleToken(java.lang.Math.E);
        _table.put("E", token);
        _table.put("e", token);

        ComplexToken i = new ComplexToken(new Complex(0.0, 1.0));
        _table.put("i", i);
        _table.put("j", i);
        _table.put("true", BooleanToken.TRUE);
        _table.put("false", BooleanToken.FALSE);

        // This variable is used as a tag by the FileParameter
        // class to represent a search in the classpath.  This is a hack,
        // but it deals with the fact that Java is not symmetric in how it
        // deals with getting files from the classpath (using getResource)
        // and getting files from the file system.
        _table.put("CLASSPATH", new StringToken("xxxxxxCLASSPATHxxxxxx"));

        try {
            // StringToken.getProperty() specially handles user.dir.
            _putProperty("CWD", "user.dir");

            _putProperty("HOME", "user.home");

            // When Vergil is started up, java is called with
            // -Dptolemy.ptII.dir=${PTII} and
            // StringUtilities.getProperty() does some special munging
            // for ptolemy.ptII.dir
            _putProperty("PTII", "ptolemy.ptII.dir");

            // See also the ptolemy.ptII.dirAsURL property in StringUtilities.
            // Note that TMPDIR almost always ends with a \ or /
            // so usually we refer to it as ${TMPDIR}.
            _putProperty("TMPDIR", "java.io.tmpdir");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Infinities and NaN
        _table.put("NaN", new DoubleToken(Double.NaN));
        _table.put("Infinity", new DoubleToken(Double.POSITIVE_INFINITY));

        // numerical bounds
        _table.put("MaxUnsignedByte", new UnsignedByteToken(255));
        _table.put("MinUnsignedByte", new UnsignedByteToken(0));
        _table.put("MaxShort", new ShortToken(Short.MAX_VALUE));
        _table.put("MinShort", new ShortToken(Short.MIN_VALUE));
        _table.put("MaxInt", new IntToken(Integer.MAX_VALUE));
        _table.put("MinInt", new IntToken(Integer.MIN_VALUE));
        _table.put("MaxLong", new LongToken(Long.MAX_VALUE));
        _table.put("MinLong", new LongToken(Long.MIN_VALUE));
        _table.put("MaxFloat", new DoubleToken(Float.MAX_VALUE));
        _table.put("MinFloat", new DoubleToken(Float.MIN_VALUE));
        _table.put("MaxDouble", new DoubleToken(Double.MAX_VALUE));
        _table.put("MinDouble", new DoubleToken(Double.MIN_VALUE));
        _table.put("PositiveInfinity",
                new DoubleToken(Double.POSITIVE_INFINITY));
        _table.put("NegativeInfinity",
                new DoubleToken(Double.NEGATIVE_INFINITY));

        // Type constants.
        // We use a separate map of types so that we can list the
        // types in the port configurer dialog
        _types.put("boolean", BooleanToken.FALSE);
        _types.put("complex", new ComplexToken(new Complex(0.0, 0.0)));
        _types.put("float", new FloatToken(0.0f));
        _types.put("double", new DoubleToken(0.0));
        _types.put("fixedpoint", new UnsizedFixToken());
        _types.put("general", new GeneralToken());
        _types.put("short", new ShortToken(0));
        _types.put("int", new IntToken(0));
        _types.put("long", new LongToken(0));
        _types.put("matrix", new ConcreteMatrixToken());
        _types.put("object", ObjectToken.NULL);
        _types.put("xmltoken", new XMLToken());
        _types.put("scalar", new ConcreteScalarToken());
        _types.put("string", new StringToken(""));
        _types.put("unknown", new UnknownToken());
        _types.put("unsignedByte", new UnsignedByteToken(0));

        _table.putAll(_types);

        ptolemy.data.Token nil = ptolemy.data.Token.NIL;
        _table.put("nil", nil);

        _table.put("null", ObjectToken.NULL);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Look up a property and add it to the _table.
    // If the property is not available because of a securityException,
    // then ignore the securityException and do nothing.
    private static void _putProperty(String variableName, String property) {
        try {
            _table.put(variableName,
                    new StringToken(StringUtilities.getProperty(property)));
        } catch (SecurityException ex) {
            System.out.println("Warning: While trying to set '" + variableName
                    + "', failed to read '" + property + "' property "
                    + "(-sandbox always causes this)");
        }
    }
}
