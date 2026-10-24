/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.visualBasic.dotnet.impl.lang.lexer;

import consulo.language.lexer.FlexAdapter;
import consulo.language.lexer.RestartableLexer;
import consulo.language.lexer.TokenIterator;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicLexer extends FlexAdapter implements RestartableLexer {
    public VisualBasicLexer() {
        super(new _VisualBasicLexer());
    }

    @Override
    public int getStartState() {
        return _VisualBasicLexer.YYINITIAL;
    }

    @Override
    public boolean isRestartableState(int state) {
        return ((_VisualBasicLexer) getFlex()).isRestartableState(state);
    }

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        ((_VisualBasicLexer) getFlex()).reset();
        super.start(buffer, startOffset, endOffset, initialState);
    }

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState, TokenIterator tokenIterator) {
        start(buffer, startOffset, endOffset, initialState);
    }
}
