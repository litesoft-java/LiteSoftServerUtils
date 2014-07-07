package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;

public class MutationFileMap {
    public MutationFileData get( Object pKeyPart0, Object... pKeyPartNths ) {
        return mMutationFileDatas.get( Maps.makeStringKey( pKeyPart0, pKeyPartNths ) );
    }

    public static class Builder {
        public MutationFileMap build() {
            return new MutationFileMap( this );
        }

        public KeyedBuilder addKey( Object pKeyPart0, Object... pKeyPartNths ) {
            Confirm.isFalse( "Can't add a Key after 'build' is called", mBuilt );
            return new KeyedBuilder( Maps.makeStringKey( pKeyPart0, pKeyPartNths ) );
        }

        public class KeyedBuilder {
            public Builder with( String pFileName ) {
                return addwith( new MutationFileData( pFileName ) );
            }

            public Builder with( String pFileName, String pReplaceFrom, String pReplaceTo, String... pAdditionalReplacementsFromToPairs ) {
                return addwith( new MutationFileData( pFileName, pReplaceFrom, pReplaceTo, pAdditionalReplacementsFromToPairs ) );
            }

            public Builder with( String pFileName, MutationFileData.ReplacementPair pReplacement, MutationFileData.ReplacementPair... pAdditionalReplacements ) {
                return addwith( new MutationFileData( pFileName, pReplacement, pAdditionalReplacements ) );
            }

            private Builder addwith( MutationFileData pMutationFileData ) {
                Confirm.isFalse( "Can't add an Entry after 'build' is called", mBuilt );
                mMutationFileDatas.put( mKey, pMutationFileData );
                return Builder.this;
            }

            private final String mKey;

            private KeyedBuilder( String pKey ) {
                mKey = pKey;
            }
        }

        private final Map<String, MutationFileData> mMutationFileDatas = Maps.newHashMap();
        private boolean mBuilt;
    }

    private MutationFileMap( Builder pBuilder ) {
        Confirm.isFalse( "'build' already called", pBuilder.mBuilt );
        pBuilder.mBuilt = true;
        this.mMutationFileDatas = pBuilder.mMutationFileDatas;
    }

    private final Map<String, MutationFileData> mMutationFileDatas;
}
