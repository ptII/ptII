package ptolemy.actor.lib.mail;

import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JFrame;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/** Upon firing, send email to the specified recipient.
 *  This actor uses the SMTP protocol and will prompt for a password
 *  upon being first invoked. The password is sent encrypted.
 *  <p>
 *  By default, this actor will not actually send email, but
 *  will rather produce the formatted email on its output port.
 *  The reason for this is that sending email should be done
 *  with some hesitation, since it is easy with such an actor
 *  to create a flood of email that will have undesirable effects.
 *  To get the actor to actually send email, change the value
 *  of the <i>reallySendMail</i> parameter to true. Upon completion
 *  of an execution, <i>reallySendMail</i> will be set back to false
 *  (in the wrapup() method) to help prevent accidental duplicate
 *  mailings.
 *  <p>
 *  This actor requires that the javamail package be present, and that
 *  at least the mailapi.jar and smtp.jar files be in the CLASSPATH.
 *  Install javamail in $PTII/vendors/oracle, then run configure in
 *  $PTII. In Eclipse, you will then need to refresh the project.
 *  If for some reason the smtp.jar is missing, then you will get
 *  a cryptic NoSuchProviderException.
 * 
 *  @author Edward A. Lee
 */
public class SendMail extends TypedAtomicActor {

    public SendMail(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        to = new PortParameter(this, "to");
        to.setStringMode(true);
        to.setExpression("nobody1@nowhere.com, nobody2@nowhere.com");
        new SingletonParameter(to.getPort(), "_showName").setToken(BooleanToken.TRUE);

        from = new PortParameter(this, "from");
        from.setStringMode(true);
        from.setExpression("noreply@noreply.com");
        new SingletonParameter(from.getPort(), "_showName").setToken(BooleanToken.TRUE);

        subject = new PortParameter(this, "subject");
        subject.setStringMode(true);
        new SingletonParameter(subject.getPort(), "_showName").setToken(BooleanToken.TRUE);

        message = new PortParameter(this, "message");
        message.setStringMode(true);
        new SingletonParameter(message.getPort(), "_showName").setToken(BooleanToken.TRUE);
        TextStyle style = new TextStyle(message, "style");
        style.height.setExpression("30");

        SMTPHostName = new StringParameter(this, "SMTPHostName");
        SMTPHostName.setExpression("smtp.myserver.com");
        
        SMTPUserName = new StringParameter(this, "SMTPUserName");
        SMTPUserName.setExpression("myusername");
        
        reallySendMail = new Parameter(this, "reallySendMail");
        reallySendMail.setTypeEquals(BaseType.BOOLEAN);
        reallySendMail.setExpression("false");
        reallySendMail.setPersistent(false);
        
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }
        
    /** Email address from which this is sent. */
    public PortParameter from;

    /** The message to send. This defaults to an empty string. */
    public PortParameter message;
    
    /** Output to which the formatted message is sent.
     *  The type of this output is string.
     */
    public TypedIOPort output;

    /** If true, then actually send the email.
     *  This is a boolean that defaults to false,
     *  meaning that the message is only sent to the output port.
     *  This parameter will be set back to false in the
     *  wrapup() method, to help prevent duplicate mailings.
     */
    public Parameter reallySendMail;

    /** Host name for the send mail server. */
    public StringParameter SMTPHostName;
    
    /** User name for the send mail server. */
    public StringParameter SMTPUserName;
    
    /** The subject line. This defaults to an empty string. */
    public PortParameter subject;

    /** Email address(es) to which this is sent.
     *  This is a comma-separated list that defaults to
     *  "nobody1@nowhere.com, nobody2@nowhere.com".
     */
    public PortParameter to;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this class,
     *  if the SMTP host or user name is changed, this method forgets the password.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == SMTPHostName || attribute == SMTPUserName) {
            _password = null;
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Set up the properties for the SMTP protocol.
     *  @throws IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _props = new Properties();
        _props.put("mail.transport.protocol", "smtp");
        _props.put("mail.smtp.host", SMTPHostName.stringValue());
        _props.put("mail.smtp.auth", "true");
        // The following is needed to prevent the message
        // Relaying denied. Proper authentication required.
        _props.put("mail.smtp.starttls.enable","true");
    }

    /** Update the parameters based on any available inputs
     *  and then send one email message.
     *  @throws IllegalActionException If any of several errors
     *   occur while attempting to send the message. 
     */
    public boolean postfire() throws IllegalActionException {
        from.update();
        to.update();
        message.update();
        subject.update();
        
        // Make sure the user name is valid, since parse errors will
        // be ignored in the authenticator below.
        SMTPUserName.stringValue();
        
        // First construct the string to send to the output.
        StringBuffer result = new StringBuffer();
        
        String toValue = ((StringToken)to.getToken()).stringValue();
        String fromValue = ((StringToken)from.getToken()).stringValue();
        String subjectValue = ((StringToken)subject.getToken()).stringValue();
        String messageValue = ((StringToken)message.getToken()).stringValue();
        
        StringTokenizer tokenizer = new StringTokenizer(toValue, ",");
        while (tokenizer.hasMoreTokens()) {
            result.append("To: " + tokenizer.nextToken().trim() + "\n");
        }

        result.append("From: " + fromValue + "\n");
        result.append("Subject: " + subjectValue + "\n");
        result.append("----\n");
        result.append(messageValue);
        result.append("\n----\n");
        
        output.send(0, new StringToken(result.toString()));

        if (!((BooleanToken)reallySendMail.getToken()).booleanValue()) {
            // Don't want to actually send email, so we just return now.
            return true;
        }

        Authenticator auth = new SMTPAuthenticator();
        Session mailSession = Session.getDefaultInstance(_props, auth);
        // Uncomment for debugging info to stdout.
        // mailSession.setDebug(true);
        try {
            Transport transport = mailSession.getTransport();

            MimeMessage mimeMessage = new MimeMessage(mailSession);
            mimeMessage.setContent(messageValue, "text/plain");
            mimeMessage.setFrom(new InternetAddress(fromValue));
            mimeMessage.setSubject(subjectValue);
            
            tokenizer = new StringTokenizer(toValue, ",");
            while (tokenizer.hasMoreTokens()) {
                mimeMessage.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(tokenizer.nextToken().trim()));
            }

            transport.connect();
            transport.sendMessage(mimeMessage,
                    mimeMessage.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (MessagingException e) {
            throw new IllegalActionException(this, e, "Send mail failed.");
        }

        return true;
    }
    
    /** Override the base class to set <i>reallySendMail</i> back
     *  to false if it is true.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (((BooleanToken)reallySendMail.getToken()).booleanValue()) {
            reallySendMail.setToken(BooleanToken.FALSE);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The password last entered. */
    private char[] _password;
    
    /** Reference to a persistent set of properties used to configure
     *  the SMTP parameters.
     */
    private Properties _props;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////


    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username;
           try {
               username = SMTPUserName.stringValue();
           } catch (IllegalActionException e) {
               // This should not occur, since bwfore constructing this
               // class, the user name was checked.
               throw new InternalErrorException(e);
           }
           if (_password == null) {
               // Open a dialog to get the password.
               // First find a frame to "own" the dialog.
               // Note that if we run in an applet, there may
               // not be an effigy.
               Effigy effigy = Configuration.findEffigy(toplevel());
               JFrame frame = null;
               if (effigy != null) {
                   Tableau tableau = effigy.showTableaux();
                   if (tableau != null) {
                       frame = tableau.getFrame();
                   }
               }

               // Next construct a query for user name and password.
               Query query = new Query();
               query.setTextWidth(60);
               query.addLine("SMTPuser", "SMTP user name", username);
               query.addPassword("password", "Password", "");
               ComponentDialog dialog = new ComponentDialog(frame,
                       "Send Mail Password", query);

               if (dialog.buttonPressed().equals("OK")) {
                   // Update the parameter values.
                   String newUserName = query.getStringValue("SMTPuser");
                   if (!(username.equals(newUserName))) {
                       SMTPUserName.setExpression(newUserName);
                   }
                   // The password is not stored as a parameter.
                   _password = query.getCharArrayValue("password");
               } else {
                   return null;
               }
           }
           return new PasswordAuthentication(username, new String(_password));
        }
    }
}