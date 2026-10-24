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


// Token rules follow Roslyn src/Compilers/VisualBasic/Portable/Scanner (MIT)

package consulo.visualBasic.dotnet.impl.lang.lexer;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.lexer.FlexLexer;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jspecify.annotations.NullUnmarked;

import static consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens.*;

// JFlex output is not null-checked
@NullUnmarked
%%

%{
  public _VisualBasicLexer() {
    this((java.io.Reader)null);
  }

  /**
   * A line break after these tokens is an implicit line continuation: the next line is not a statement start.
   */
  private static final TokenSet CONTINUES_LINE = TokenSet.create(
    COMMA, LPAR, LBRACE, EQ, COLONEQ, DOT,
    PLUS, MINUS, ASTERISK, SLASH, BACKSLASH, CARET, AMPERSAND, LTLT, GTGT,
    PLUSEQ, MINUSEQ, ASTERISKEQ, SLASHEQ, BACKSLASHEQ, CARETEQ, AMPERSANDEQ, LTLTEQ, GTGTEQ,
    AND_KEYWORD, OR_KEYWORD, ANDALSO_KEYWORD, ORELSE_KEYWORD, XOR_KEYWORD, MOD_KEYWORD,
    IS_KEYWORD, ISNOT_KEYWORD, LIKE_KEYWORD, NOT_KEYWORD
  );

  /**
   * '<' after these tokens starts an XML literal: an expression is expected here.
   * '<' and '>' are left out, so attribute lines do not start XML on the next line.
   */
  private static final TokenSet XML_STARTS_AFTER = TokenSet.orSet(TokenSet.andNot(CONTINUES_LINE, TokenSet.create(DOT)), TokenSet.create(
    RETURN_KEYWORD, THROW_KEYWORD, THEN_KEYWORD, ELSE_KEYWORD, TO_KEYWORD, STEP_KEYWORD, IN_KEYWORD, SELECT_KEYWORD,
    CALL_KEYWORD, CASE_KEYWORD, WHILE_KEYWORD, AWAIT_KEYWORD, INTERPOLATION_START, XML_EMBED_START
  ));

  /**
   * In a line with these keywords '<' after '(' or ',' starts a parameter attribute, not XML.
   */
  private static final TokenSet DECLARATION_LINE_KEYWORDS = TokenSet.create(
    SUB_KEYWORD, FUNCTION_KEYWORD, PROPERTY_KEYWORD, EVENT_KEYWORD, OPERATOR_KEYWORD, DELEGATE_KEYWORD, DECLARE_KEYWORD
  );

  /**
   * Saved context of the enclosing code: entered an interpolated string, an XML literal or an embedded expression.
   */
  private record Frame(int returnState, int parenDepth, int xmlDepth, boolean xmlDocument, IElementType last, boolean declarationLine) {
  }

  private final Deque<Frame> myFrames = new ArrayDeque<>();

  private int myParenDepth;
  // open elements of the current XML literal
  private int myXmlDepth;
  // the current XML literal started with <?xml ... ?>
  private boolean myXmlDocument;
  // last significant code token of the logical line, null at a statement start
  private IElementType myLast;
  private boolean myDeclarationLine;

  public void reset() {
    myFrames.clear();
    myParenDepth = 0;
    myXmlDepth = 0;
    myXmlDocument = false;
    myLast = null;
    myDeclarationLine = false;
  }

  /**
   * Only statement starts: elsewhere the lexer depends on the fields above.
   */
  public boolean isRestartableState(int state) {
    return state == YYINITIAL;
  }

  private IElementType code(IElementType type) {
    if (yystate() == YYINITIAL || yystate() == CONTINUATION) {
      yybegin(CODE);
    }
    myLast = type;
    if (DECLARATION_LINE_KEYWORDS.contains(type)) {
      myDeclarationLine = true;
    }
    return type;
  }

  private IElementType newLine() {
    if (myLast != null && CONTINUES_LINE.contains(myLast)) {
      // implicit line continuation
      yybegin(CODE);
    }
    else {
      yybegin(YYINITIAL);
      myLast = null;
      myDeclarationLine = false;
      myParenDepth = 0;
    }
    return NEW_LINE;
  }

  private IElementType identifierOrKeyword() {
    int length = yylength();
    char last = yycharat(length - 1);
    if (isTypeCharacter(last)) {
      // '!' followed by an identifier is the dictionary access operator, not a type character
      if (last == '!' && zzMarkedPos < zzEndRead && isBangOperand(zzBuffer.charAt(zzMarkedPos))) {
        yypushback(1);
      }
      else {
        return code(IDENTIFIER);
      }
    }
    IElementType keyword = VisualBasicKeywords.findReserved(yytext());
    if (keyword != null) {
      return code(keyword);
    }
    code(IDENTIFIER);
    if (isExpressionStartWord(yytext())) {
      // contextual keywords followed by an expression
      myLast = AWAIT_KEYWORD;
    }
    return IDENTIFIER;
  }

  private static boolean isExpressionStartWord(CharSequence text) {
    String word = text.toString();
    return word.equalsIgnoreCase("Await") || word.equalsIgnoreCase("Yield") || word.equalsIgnoreCase("Where") || word.equalsIgnoreCase("Let");
  }

  private static boolean isTypeCharacter(char c) {
    return c == '%' || c == '&' || c == '@' || c == '!' || c == '#' || c == '$';
  }

  private static boolean isBangOperand(char c) {
    return Character.isLetter(c) || c == '_' || c == '[' || c == ']';
  }

  private void push(int returnState) {
    myFrames.push(new Frame(returnState, myParenDepth, myXmlDepth, myXmlDocument, myLast, myDeclarationLine));
  }

  private Frame pop() {
    Frame frame = myFrames.pop();
    myParenDepth = frame.parenDepth();
    myXmlDepth = frame.xmlDepth();
    myXmlDocument = frame.xmlDocument();
    myLast = frame.last();
    myDeclarationLine = frame.declarationLine();
    yybegin(frame.returnState());
    return frame;
  }

  private int codeState() {
    return yystate() == YYINITIAL || yystate() == CONTINUATION ? CODE : yystate();
  }

  // interpolated strings

  private IElementType startInterpolatedString() {
    push(codeState());
    myParenDepth = 0;
    yybegin(INTERP_TEXT);
    return INTERPOLATED_STRING_START;
  }

  private IElementType endInterpolatedString() {
    pop();
    myLast = STRING_LITERAL;
    return INTERPOLATED_STRING_END;
  }

  private IElementType stringLiteral() {
    int directiveLine = findDirectiveLine();
    if (directiveLine >= 0) {
      // an unterminated string ends before a preprocessor directive line
      yypushback(yylength() - directiveLine);
      return code(STRING_LITERAL);
    }
    char last = yycharat(yylength() - 1);
    return code(last == 'c' || last == 'C' ? CHARACTER_LITERAL : STRING_LITERAL);
  }

  /**
   * Strings may span lines, but not a line starting with '#' and a name: a directive, maybe ending inactive code.
   *
   * @return offset in the matched text of the line break before such a line, or -1
   */
  private int findDirectiveLine() {
    int length = yylength();
    for (int i = 0; i < length; i++) {
      char c = yycharat(i);
      if (c != '\n' && c != '\r') {
        continue;
      }
      int j = i + 1;
      if (c == '\r' && j < length && yycharat(j) == '\n') {
        j++;
      }
      while (j < length && (yycharat(j) == ' ' || yycharat(j) == '\t')) {
        j++;
      }
      if (j < length && yycharat(j) == '#') {
        int k = j + 1;
        while (k < length && (yycharat(k) == ' ' || yycharat(k) == '\t')) {
          k++;
        }
        if (k < length && Character.isLetter(yycharat(k))) {
          return i;
        }
      }
    }
    return -1;
  }

  // XML literals

  private boolean isXmlStart() {
    if (myLast == null || !XML_STARTS_AFTER.contains(myLast)) {
      return false;
    }
    if (myDeclarationLine && (myLast == LPAR || myLast == COMMA)) {
      return false;
    }
    if (zzMarkedPos >= zzEndRead) {
      return false;
    }
    char next = zzBuffer.charAt(zzMarkedPos);
    return Character.isLetter(next) || next == '_' || next == '!' || next == '?';
  }

  /**
   * Enters the XML literal starting at the matched '<', which is lexed again in XML_MISC.
   */
  private void startXml() {
    push(codeState());
    myXmlDepth = 0;
    myXmlDocument = false;
    yypushback(1);
    yybegin(XML_MISC);
  }

  private void endXml() {
    pop();
    // the literal is an operand
    myLast = XML_TAG_END;
  }

  private void afterElement() {
    myXmlDepth--;
    if (myXmlDepth <= 0) {
      endXml();
    }
    else {
      yybegin(XML_CONTENT);
    }
  }

  private IElementType startEmbedded(int returnState) {
    push(returnState);
    myParenDepth = 0;
    myLast = XML_EMBED_START;
    yybegin(XML_EMBED);
    return XML_EMBED_START;
  }

  /**
   * Extends the matched text up to and including {@code end}, or to the end of the text.
   */
  private void consumeUntil(String end) {
    int index = indexOf(end, zzMarkedPos);
    zzMarkedPos = index < 0 ? zzEndRead : index + end.length();
  }

  private int indexOf(String text, int from) {
    for (int i = from; i + text.length() <= zzEndRead; i++) {
      boolean match = true;
      for (int j = 0; j < text.length(); j++) {
        if (zzBuffer.charAt(i + j) != text.charAt(j)) {
          match = false;
          break;
        }
      }
      if (match) {
        return i;
      }
    }
    return -1;
  }

  private IElementType processingInstruction() {
    boolean declaration = yystate() == XML_MISC && myXmlDepth == 0 && !myXmlDocument && startsWithXmlDeclaration();
    consumeUntil("?>");
    if (declaration) {
      myXmlDocument = true;
    }
    else if (yystate() == XML_MISC && !myXmlDocument) {
      endXml();
    }
    return XML_PI;
  }

  private boolean startsWithXmlDeclaration() {
    // "<?" is matched, "xml" and a whitespace follow
    int position = zzMarkedPos;
    return position + 3 < zzEndRead
      && zzBuffer.subSequence(position, position + 3).toString().equals("xml")
      && Character.isWhitespace(zzBuffer.charAt(position + 3));
  }

  private IElementType outsideOfElement(IElementType type, String end) {
    consumeUntil(end);
    if (yystate() == XML_MISC && !myXmlDocument) {
      endXml();
    }
    return type;
  }
%}

%public
%class _VisualBasicLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

// YYINITIAL: a statement start, only whitespace before. Preprocessor directives are recognized here only
%state CODE
// after a line continuation '_': the next line break is not a statement terminator
%state CONTINUATION
%state INTERP_TEXT
%state INTERP_CODE
%state INTERP_FORMAT
// XML literal outside of elements: the prolog of a document, or a literal of a comment, CDATA or PI only
%state XML_MISC
%state XML_TAG_OPEN
%state XML_TAG
%state XML_CONTENT
%state XML_END_TAG
// <%= expression %>
%state XML_EMBED

NEW_LINE=\r\n|\r|\n|\u2028|\u2029|\u0085
NOT_NEW_LINE=[^\r\n\u2028\u2029\u0085]
WHITE_SPACE=([ \t\f\u000B]|\p{Zs})+

SINGLE_QUOTE=['\u2018\u2019]
NOT_SINGLE_QUOTE=[^'\u2018\u2019\r\n\u2028\u2029\u0085]
DOUBLE_QUOTE=[\"\u201C\u201D]
NOT_DOUBLE_QUOTE=[^\"\u201C\u201D]

COMMENT={SINGLE_QUOTE}{NOT_NEW_LINE}*
DOC_COMMENT={SINGLE_QUOTE}{SINGLE_QUOTE}{SINGLE_QUOTE}({NOT_SINGLE_QUOTE}{NOT_NEW_LINE}*)?
REM_COMMENT=[rR][eE][mM]([^\p{L}\p{Nl}\p{Nd}\p{Pc}\p{Mn}\p{Mc}\p{Cf}\r\n\u2028\u2029\u0085]{NOT_NEW_LINE}*)?

IDENTIFIER_START=[\p{L}\p{Nl}]
IDENTIFIER_PART=[\p{L}\p{Nl}\p{Nd}\p{Pc}\p{Mn}\p{Mc}\p{Cf}]
IDENTIFIER={IDENTIFIER_START}{IDENTIFIER_PART}*|_{IDENTIFIER_PART}+
TYPE_CHARACTER=[%&@!#$]

DIGITS=[0-9]([0-9_]*[0-9])?
EXPONENT=[eE][+-]?{DIGITS}
INTEGER_SUFFIX=[sS]|[uU][sS]|[iI]|[uU][iI]|[lL]|[uU][lL]|"%"|"&"
INTEGER_LITERAL=({DIGITS}|&[hH][0-9a-fA-F_]+|&[oO][0-7_]+|&[bB][01_]+){INTEGER_SUFFIX}?
FLOATING_LITERAL=({DIGITS}"."{DIGITS}{EXPONENT}?|"."{DIGITS}{EXPONENT}?|{DIGITS}{EXPONENT}|{DIGITS})[fFrR!#]|{DIGITS}"."{DIGITS}{EXPONENT}?|"."{DIGITS}{EXPONENT}?|{DIGITS}{EXPONENT}
DECIMAL_LITERAL=({DIGITS}"."{DIGITS}{EXPONENT}?|"."{DIGITS}{EXPONENT}?|{DIGITS}{EXPONENT}?)[dD@]
DATE_LITERAL="#"[ \t]*[0-9][^#\r\n\u2028\u2029\u0085]*"#"

STRING_LITERAL={DOUBLE_QUOTE}({NOT_DOUBLE_QUOTE}|{DOUBLE_QUOTE}{DOUBLE_QUOTE})*({DOUBLE_QUOTE}[cC]?)?

XML_NAME_START=[\p{L}_:]
XML_NAME={XML_NAME_START}[\p{L}\p{Nd}\p{Mn}\p{Mc}_:.\-]*
XML_WHITE_SPACE=([ \t\f]|{NEW_LINE})+

%%

<YYINITIAL> {
  "#"{WHITE_SPACE}?[a-zA-Z]{NOT_NEW_LINE}* { return PREPROCESSOR_DIRECTIVE; }
}

<CONTINUATION> {
  {WHITE_SPACE}                          { return WHITE_SPACE; }
  {DOC_COMMENT}                          { return DOC_COMMENT; }
  {COMMENT}                              { return COMMENT; }
  {REM_COMMENT}                          { return COMMENT; }
  {NEW_LINE}                             { yybegin(CODE); return LINE_CONTINUATION; }
}

<YYINITIAL, CODE> {
  {NEW_LINE}                             { return newLine(); }
  "_" / {WHITE_SPACE}? ({COMMENT}|{REM_COMMENT})? {NEW_LINE}
                                         { yybegin(CONTINUATION); return LINE_CONTINUATION; }
}

<INTERP_CODE> {
  {NEW_LINE}                             { return NEW_LINE; }
  "}"                                    {
                                           if (myParenDepth > 0) {
                                             myParenDepth--;
                                             return code(RBRACE);
                                           }
                                           yybegin(INTERP_TEXT);
                                           return INTERPOLATION_END;
                                         }
  ":"                                    {
                                           if (myParenDepth == 0) {
                                             yybegin(INTERP_FORMAT);
                                           }
                                           return code(COLON);
                                         }
}

<INTERP_TEXT> {
  ([^\"\u201C\u201D\{\}]|{DOUBLE_QUOTE}{DOUBLE_QUOTE}|"{{"|"}}")+
                                         { return INTERPOLATED_STRING_TEXT; }
  {DOUBLE_QUOTE}                         { return endInterpolatedString(); }
  "{"                                    { myParenDepth = 0; myLast = INTERPOLATION_START; yybegin(INTERP_CODE); return INTERPOLATION_START; }
  "}"                                    { return BAD_CHARACTER; }
}

<INTERP_FORMAT> {
  [^\"\u201C\u201D}\r\n\u2028\u2029\u0085]+ { return INTERPOLATION_FORMAT; }
  "}"                                    { yybegin(INTERP_TEXT); return INTERPOLATION_END; }
  {DOUBLE_QUOTE}                         { return endInterpolatedString(); }
  {NEW_LINE}                             { yybegin(INTERP_TEXT); return NEW_LINE; }
}

<XML_EMBED> {
  "%>"                                   { pop(); return XML_EMBED_END; }
  {XML_WHITE_SPACE}                      { return WHITE_SPACE; }
}

<YYINITIAL, CODE, INTERP_CODE, XML_EMBED> {
  {WHITE_SPACE}                          { return WHITE_SPACE; }

  {DOC_COMMENT}                          { return DOC_COMMENT; }
  {COMMENT}                              { return COMMENT; }
  {REM_COMMENT}                          { return COMMENT; }

  "$"{DOUBLE_QUOTE}                      { return startInterpolatedString(); }
  {STRING_LITERAL}                       { return stringLiteral(); }

  {DATE_LITERAL}                         { return code(DATE_LITERAL); }
  {DECIMAL_LITERAL}                      { return code(DECIMAL_LITERAL); }
  {FLOATING_LITERAL}                     { return code(FLOATING_LITERAL); }
  {INTEGER_LITERAL}                      { return code(INTEGER_LITERAL); }

  "[" {IDENTIFIER} "]"                   { return code(IDENTIFIER); }
  {IDENTIFIER}{TYPE_CHARACTER}?          { return identifierOrKeyword(); }

  "("                                    { myParenDepth++; return code(LPAR); }
  ")"                                    { if (myParenDepth > 0) { myParenDepth--; } return code(RPAR); }
  "{"                                    { myParenDepth++; return code(LBRACE); }
  "}"                                    { if (myParenDepth > 0) { myParenDepth--; } return code(RBRACE); }
  ","                                    { return code(COMMA); }
  "."                                    { return code(DOT); }
  ":="                                   { return code(COLONEQ); }
  ":"                                    { return code(COLON); }
  "!"                                    { return code(EXCLAMATION); }
  "?"                                    { return code(QUESTION); }
  "#"                                    { return code(HASH); }
  "@"                                    { return code(AT); }

  "+="                                   { return code(PLUSEQ); }
  "-="                                   { return code(MINUSEQ); }
  "*="                                   { return code(ASTERISKEQ); }
  "/="                                   { return code(SLASHEQ); }
  "\\="                                  { return code(BACKSLASHEQ); }
  "^="                                   { return code(CARETEQ); }
  "&="                                   { return code(AMPERSANDEQ); }
  // '<' and '>' are single tokens: the parser composes '<=', '>=', '<>', '<<', '>>', '<<=' and '>>=',
  // which may have whitespace between their characters
  "<"                                    {
                                           if (isXmlStart()) {
                                             startXml();
                                           }
                                           else {
                                             return code(LT);
                                           }
                                         }
  ">"                                    { return code(GT); }
  "="                                    { return code(EQ); }
  "+"                                    { return code(PLUS); }
  "-"                                    { return code(MINUS); }
  "*"                                    { return code(ASTERISK); }
  "/"                                    { return code(SLASH); }
  "\\"                                   { return code(BACKSLASH); }
  "^"                                    { return code(CARET); }
  "&"                                    { return code(AMPERSAND); }
}

<XML_MISC> {
  "<?"                                   { return processingInstruction(); }
  "<!--"                                 { return outsideOfElement(XML_COMMENT, "-->"); }
  "<![CDATA["                            { return outsideOfElement(XML_CDATA, "]]>"); }
  "<"{XML_NAME_START}                    { yypushback(1); myXmlDepth++; yybegin(XML_TAG_OPEN); return XML_TAG_START; }
  {XML_WHITE_SPACE}                      { if (myXmlDocument) { return WHITE_SPACE; } yypushback(yylength()); endXml(); }
  [^]                                    { yypushback(1); endXml(); }
}

<XML_TAG_OPEN> {
  {XML_NAME}                             { yybegin(XML_TAG); return XML_TAG_NAME; }
  [^]                                    { yypushback(1); yybegin(XML_TAG); }
}

<XML_TAG> {
  {XML_WHITE_SPACE}                      { return WHITE_SPACE; }
  {XML_NAME}                             { return XML_ATTRIBUTE_NAME; }
  "="                                    { return XML_EQ; }
  \"[^\"<]*\"?                           { return XML_ATTRIBUTE_VALUE; }
  '[^'<]*'?                              { return XML_ATTRIBUTE_VALUE; }
  "<%="                                  { return startEmbedded(XML_TAG); }
  ">"                                    { yybegin(XML_CONTENT); return XML_TAG_END; }
  "/>"                                   { afterElement(); return XML_EMPTY_TAG_END; }
  "<"                                    { yypushback(1); yybegin(XML_CONTENT); }
  [^]                                    { return BAD_CHARACTER; }
}

<XML_CONTENT> {
  "</"                                   { yybegin(XML_END_TAG); return XML_END_TAG_START; }
  "<%="                                  { return startEmbedded(XML_CONTENT); }
  "<!--"                                 { consumeUntil("-->"); return XML_COMMENT; }
  "<![CDATA["                            { consumeUntil("]]>"); return XML_CDATA; }
  "<?"                                   { return processingInstruction(); }
  "<"{XML_NAME_START}                    { yypushback(1); myXmlDepth++; yybegin(XML_TAG_OPEN); return XML_TAG_START; }
  [^<]+                                  { return XML_TEXT; }
  "<"                                    { return XML_TEXT; }
}

<XML_END_TAG> {
  {XML_NAME}                             { return XML_TAG_NAME; }
  {XML_WHITE_SPACE}                      { return WHITE_SPACE; }
  ">"                                    { afterElement(); return XML_TAG_END; }
  [^]                                    { yypushback(1); afterElement(); }
}

[^]                                      { return code(BAD_CHARACTER); }
