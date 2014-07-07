package org.litesoft.server.util;

import org.litesoft.commonfoundation.annotations.*;
import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public abstract class MultiThreadedListProcessor {
    private static final int MAX_THREADS = 25;

    private final AtomicInteger mRemainingCountToProcess = new AtomicInteger();
    protected final AtomicReference<String> mStoppingResult = new AtomicReference<String>( null );
    protected final ProgressReporter mReporter;
    private final String mWhatProcess;

    public MultiThreadedListProcessor( @NotNull ProgressReporter pReporter, String pWhatProcess ) {
        mReporter = Confirm.isNotNull( "Reporter", pReporter );
        mWhatProcess = ConstrainTo.significantOrNull( pWhatProcess );
    }

    public boolean process( String pListDescription, List<String> pToProcess )
            throws InterruptedException {
        if ( (pToProcess = ConstrainTo.notNull( pToProcess )).isEmpty() ) {
            return true;
        }
        int zCount = pToProcess.size();
        mRemainingCountToProcess.set( zCount );
        mReporter.reportNewLine( new MsgBuilder()
                                         .addText( mWhatProcess )
                                         .addOptionalPluralText( pListDescription, zCount )
                                         .addText( ": " + zCount ) );
        Queue<String> zSourceQueue = new ConcurrentLinkedQueue<String>( pToProcess );
        for ( int zAttempts = 5; (zAttempts > 0) && shouldContinue(); zAttempts-- ) {
            Queue<String> zFailedQueue = new ConcurrentLinkedQueue<String>();
            if ( process( zSourceQueue, zFailedQueue ) && zFailedQueue.isEmpty() ) {
                return true;
            }
            zSourceQueue = zFailedQueue;
        }
        String zErrorText = mStoppingResult.get();
        if ( zErrorText != null ) {
            mReporter.reportError( "************** " + mWhatProcess + " Failure: " + zErrorText );
        }
        mReporter.reportError( "************** Failed to " + mWhatProcess + " **************" );
        for ( String zEntry : zSourceQueue ) {
            mReporter.indent();
            mReporter.reportError( zEntry );
            mReporter.outdent();
        }
        return false;
    }

    private boolean shouldContinue() {
        return (mStoppingResult.get() == null);
    }

    private boolean process( final Queue<String> pSourceQueue, final Queue<String> pFailedQueue )
            throws InterruptedException {
        Thread[] zThreads = new Thread[Math.min( MAX_THREADS, pSourceQueue.size() )];
        for ( int i = 0; i < zThreads.length; i++ ) {
            (zThreads[i] = new Thread( new Runnable() {
                @Override
                public void run() {
                    for ( String zToProcess; null != (zToProcess = pSourceQueue.poll()); ) {
                        if ( shouldContinue() ) {
                            if ( process( zToProcess ) ) {
                                mRemainingCountToProcess.decrementAndGet();
                            } else {
                                pFailedQueue.add( zToProcess );
                            }
                        }
                    }
                }
            } )).start();
        }
        waitForCompletion( zThreads );
        return shouldContinue();
    }

    private void waitForCompletion( Thread[] pThreads )
            throws InterruptedException {
        boolean zAnyAlive;
        String zStatus;
        do {
            zAnyAlive = false;
            zStatus = "    ";
            for ( Thread zThread : pThreads ) {
                if ( zThread.isAlive() ) {
                    zAnyAlive = true;
                    zStatus += 'w';
                } else {
                    zStatus += ' ';
                }
            }
            zStatus += "   ";
            if ( zAnyAlive ) {
                mReporter.reportSameLine( zStatus + mRemainingCountToProcess.get() + "     " );
                Thread.sleep( 200 );
            }
        }
        while ( zAnyAlive );
        mReporter.reportSameLine( zStatus + "     " ); // Clear current line
        mReporter.reportSameLine( "" ); // Clear Dirty Flag
        for ( Thread zThread : pThreads ) {
            zThread.join();
        }
    }

    abstract protected boolean process( String pToProcess );
}
