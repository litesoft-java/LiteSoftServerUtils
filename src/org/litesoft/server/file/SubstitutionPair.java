package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.charstreams.*;
import org.litesoft.commonfoundation.typeutils.*;

import java8.util.function.*;

import java.util.*;

public class SubstitutionPair implements NamedStringValueSource,
                                         UnaryOperator<String> {
    public static final char SINGLE_QUOTE = '\'';
    public static final char DOUBLE_QUOTE = '"';
    public static final char COMMENT_LINES_STARTWITH = '#';
    public static final char FROM_TO_SEP = '=';

    private final String mFrom, mTo;

    public SubstitutionPair( String pFrom, String pTo ) {
        mFrom = pFrom;
        mTo = pTo;
    }

    public String getFrom() {
        return mFrom;
    }

    public String getTo() {
        return mTo;
    }

    @Override
    public String getName() {
        return mFrom;
    }

    @Override
    public String getValue() {
        return mTo;
    }

    @Override
    public String apply( String pText ) {
        return Strings.replace( pText, getFrom(), getTo() );
    }

    @Override
    public String toString() {
        return quote( mFrom ) + " " + FROM_TO_SEP + " " + quote( mTo );
    }

    public static List<SubstitutionPair> createListFrom( String pSource, String... pSubstitutionLines ) {
        List<SubstitutionPair> zPairs = Lists.newArrayList();
        if ( pSubstitutionLines != null ) {
            for ( int zLineOffset = 0; zLineOffset < pSubstitutionLines.length; zLineOffset++ ) {
                SubstitutionPair zPair = new LineParser( pSource, zLineOffset, pSubstitutionLines[zLineOffset] ).parse();
                if ( zPair != null ) {
                    zPairs.add( zPair );
                }
            }
        }
        return zPairs;
    }

    /**
     * @param pSubstitutions The Entry Order wil be the List Order!  Note: nulls are treated as empty strings, and Empty Keys are NOT allowed!
     */
    public static List<SubstitutionPair> createListFrom( Map<String, String> pSubstitutions ) {
        List<SubstitutionPair> zPairs = Lists.newArrayList();
        if ( pSubstitutions != null ) {
            for ( Map.Entry<String, String> zEntry : pSubstitutions.entrySet() ) {
                String zKey = ConstrainTo.notNull( zEntry.getKey() );
                String zValue = ConstrainTo.notNull( zEntry.getValue() );
                if ( zKey.isEmpty() ) {
                    throw new IllegalArgumentException( "Empty Key" );
                }
                zPairs.add( new SubstitutionPair( zKey, zValue ) );
            }
        }
        return zPairs;
    }

    private static String quote( String pString ) {
        if ( Strings.hasNoSurroundingWhiteSpace( pString ) && !pString.isEmpty() && !startsWithQuote( pString ) ) {
            return pString; // No Quoting needed!
        }
        if ( -1 == pString.indexOf( SINGLE_QUOTE ) ) {
            return SINGLE_QUOTE + pString + SINGLE_QUOTE;
        }
        return DOUBLE_QUOTE + pString + DOUBLE_QUOTE;
    }

    private static boolean startsWithQuote( String pString ) {
        return (pString.length() > 0) && isQuote( pString.charAt( 0 ) );
    }

    private static boolean isQuote( int pChar ) {
        return (pChar == SINGLE_QUOTE) || (pChar == DOUBLE_QUOTE);
    }

    private static class LineParser {
        private final String mSource;
        private final int mLineOffset;
        private final String mLine;
        private final CharSource mChars;

        private LineParser( String pSource, int pLineOffset, String pLine ) {
            mSource = pSource;
            mLineOffset = pLineOffset;
            mChars = new CharSourceFromSequence( mLine = pLine );
        }

        public SubstitutionPair parse() {
            if ( isCommentOrEmpty() ) {
                return null;
            }
            String zFrom = extractFrom();
            validateFromToSep();
            String zTo = extractTo();
            return new SubstitutionPair( zFrom, zTo );
        }

        private boolean isCommentOrEmpty() {
            return (COMMENT_LINES_STARTWITH == mChars.peek()) // Comment
                   || !mChars.consumeSpaces(); // Just white space
        }

        private void validateFromToSep() {
            if ( !mChars.consumeSpaces() || (FROM_TO_SEP != mChars.get()) ) {
                throw malformedLine( "No '" + FROM_TO_SEP + "' separator." );
            }
        }

        private String extractFrom() {
            Character zQuote = checkAndConsumeQuote();
            String zFrom = "";
            if ( zQuote == null ) {
                zFrom = mChars.getUpTo( FROM_TO_SEP ).trim();
            } else if ( zQuote != mChars.peek() ) {
                if ( (zFrom = mChars.getUpTo( zQuote )).isEmpty() ) {
                    throw malformedLine( "No closing quote (" + zQuote + ") in 'from' section." );
                }
                mChars.get(); // Consume closing quote
            }
            if ( zFrom.isEmpty() ) {
                throw malformedLine( "'from' not allowed to be empty." );
            }
            return zFrom;
        }

        private String extractTo() {
            Character zQuote = checkAndConsumeQuote();
            if ( zQuote == null ) {
                return mChars.toString().trim(); // the rest!
            }
            String zTo = (zQuote == mChars.peek()) ? "" : mChars.getUpTo( zQuote );
            if ( zQuote != mChars.get() ) { // Consume closing quote
                throw malformedLine( "No closing quote (" + zQuote + ") in 'to' section." );
            }
            if ( mChars.consumeSpaces() ) {
                throw malformedLine( "Junk after closing quote (" + zQuote + ") in 'to' section." );
            }
            return zTo;
        }

        private Character checkAndConsumeQuote() {
            return (mChars.consumeSpaces() && isQuote( mChars.peek() )) ? mChars.getRequired() : null;
        }

        private IllegalArgumentException malformedLine( String pWhy ) {
            return new IllegalArgumentException( "Malformed Line: " + pWhy
                                                 + "\n    @ (Offset: " + mLineOffset + " from '" + mSource + "') of: " + mLine );
        }
    }
}
