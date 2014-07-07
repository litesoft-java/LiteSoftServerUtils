package org.litesoft.server.util;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.io.*;

public class ProgressReporter {
    public static final String INDENT_BY = "    "; // 4 spaces

    protected final String mCallerSimpleClassName;
    protected final String mVersion;
    private boolean mCurLineDirty = false;
    private int mIndent = 0;

    public ProgressReporter( Class pCaller, String pVersion ) {
        mVersion = pVersion;
        mCallerSimpleClassName = pCaller.getSimpleName();
    }

    public void announce( MsgBuilder pMsg ) {
        reportNewLine( mCallerSimpleClassName + " " + pMsg.addText( "vs" ).addText( mVersion + "." ) );
    }

    public void indent() {
        mIndent++;
    }

    public void outdent() {
        mIndent = Math.max( 0, mIndent - 1 );
    }

    public void reportNewLine( MsgBuilder pMsg ) {
        reportNewLine( pMsg.toString() );
    }

    public void reportSameLine( MsgBuilder pMsg ) {
        reportSameLine( pMsg.toString() );
    }

    public void reportSameLine( String pLine ) {
        mCurLineDirty = (pLine == null) || (pLine.length() != 0);
        showSameLine( false, pLine );
    }

    public void reportNewLine( String pLine ) {
        clearCurLineDirty();
        showNewLine( false, pLine );
    }

    public void reportError( String pLine ) {
        clearCurLineDirty();
        showNewLine( true, pLine );
    }

    public void reportException( Exception pException ) {
        clearCurLineDirty();
        showNewLine( true, "----------------------------------------" );
        pException.printStackTrace();
    }

    public boolean promptToProceed( String pSuffix ) {
        String zMsg = "Press enter to proceed";
        if ( null != (pSuffix = ConstrainTo.significantOrNull( pSuffix )) ) {
            zMsg += " " + pSuffix;
        }
        System.out.print( "\n\n" + zMsg + ": " );
        int zRead;
        try {
            zRead = System.in.read();
        }
        catch ( IOException e ) {
            return false;
        }
        return (zRead != 3); // Ctrl-C ?
    }

    protected void showSameLine( boolean pError, String pLine ) {
        getPrintStream( pError ).print( "\r" + pLine );
    }

    protected void showNewLine( boolean pError, String pLine ) {
        if ( (pLine != null) && (pLine.length() != 0) && (mIndent != 0) ) {
            pLine = indent( mIndent, pLine );
        }
        getPrintStream( pError ).println( pLine );
    }

    private PrintStream getPrintStream( boolean pError ) {
        return (pError ? System.err : System.out);
    }

    private void clearCurLineDirty() {
        if ( mCurLineDirty ) {
            mCurLineDirty = false;
            showNewLine( false, "" );
        }
    }

    protected String indent( int pIndentNonZero, String pLineNotEmpty ) {
        if ( pIndentNonZero == 1 ) {
            return INDENT_BY + pLineNotEmpty;
        }
        StringBuilder sb = new StringBuilder( (pIndentNonZero * INDENT_BY.length()) + pLineNotEmpty.length() );
        while ( 0 <= --pIndentNonZero ) {
            sb.append( INDENT_BY );
        }
        return sb.append( pLineNotEmpty ).toString();
    }
}
