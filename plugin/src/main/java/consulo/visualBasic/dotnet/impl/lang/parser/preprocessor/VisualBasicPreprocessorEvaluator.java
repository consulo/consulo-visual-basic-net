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

package consulo.visualBasic.dotnet.impl.lang.parser.preprocessor;

import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Evaluates conditional compilation expressions: literals, constants, {@code Not}, {@code And}, {@code Or}, {@code Xor},
 * {@code AndAlso}, {@code OrElse}, comparisons, arithmetic and {@code &}. An unknown constant is {@code Nothing}.
 * A malformed expression is false.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicPreprocessorEvaluator {
    private final String myText;
    private final Map<String, Object> myConstants;
    private int myPosition;

    private VisualBasicPreprocessorEvaluator(String text, Map<String, Object> constants) {
        myText = text;
        myConstants = constants;
    }

    /**
     * @param constants by lower case name
     */
    public static @Nullable Object evaluate(String text, Map<String, Object> constants) {
        VisualBasicPreprocessorEvaluator evaluator = new VisualBasicPreprocessorEvaluator(text, constants);
        try {
            Object value = evaluator.parseOr();
            evaluator.skipWhitespace();
            return evaluator.myPosition == text.length() ? value : null;
        }
        catch (IllegalArgumentException | ArithmeticException e) {
            return null;
        }
    }

    public static boolean isTrue(@Nullable Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0;
        }
        return false;
    }

    // Or, OrElse, Xor
    private @Nullable Object parseOr() {
        Object left = parseAnd();
        while (true) {
            if (word("OrElse")) {
                boolean value = isTrue(left);
                Object right = parseAnd();
                left = value || isTrue(right);
            }
            else if (word("Or")) {
                left = isTrue(left) | isTrue(parseAnd());
            }
            else if (word("Xor")) {
                left = isTrue(left) ^ isTrue(parseAnd());
            }
            else {
                return left;
            }
        }
    }

    // And, AndAlso
    private @Nullable Object parseAnd() {
        Object left = parseNot();
        while (true) {
            if (word("AndAlso")) {
                boolean value = isTrue(left);
                Object right = parseNot();
                left = value && isTrue(right);
            }
            else if (word("And")) {
                left = isTrue(left) & isTrue(parseNot());
            }
            else {
                return left;
            }
        }
    }

    private @Nullable Object parseNot() {
        if (word("Not")) {
            return !isTrue(parseNot());
        }
        return parseComparison();
    }

    private @Nullable Object parseComparison() {
        Object left = parseConcatenation();
        while (true) {
            String operator = symbol("<=", ">=", "<>", "=", "<", ">");
            if (operator == null) {
                return left;
            }
            int compare = compare(left, parseConcatenation());
            left = switch (operator) {
                case "=" -> compare == 0;
                case "<>" -> compare != 0;
                case "<" -> compare < 0;
                case ">" -> compare > 0;
                case "<=" -> compare <= 0;
                default -> compare >= 0;
            };
        }
    }

    private @Nullable Object parseConcatenation() {
        Object left = parseAdditive();
        while (symbol("&") != null) {
            left = text(left) + text(parseAdditive());
        }
        return left;
    }

    private @Nullable Object parseAdditive() {
        Object left = parseMultiplicative();
        while (true) {
            String operator = symbol("+", "-");
            if (operator == null) {
                return left;
            }
            Object right = parseMultiplicative();
            if (operator.equals("+") && (left instanceof String || right instanceof String)) {
                left = text(left) + text(right);
            }
            else {
                left = operator.equals("+") ? number(left) + number(right) : number(left) - number(right);
            }
        }
    }

    private @Nullable Object parseMultiplicative() {
        Object left = parseUnary();
        while (true) {
            String operator = symbol("*", "/", "\\");
            if (operator == null && word("Mod")) {
                operator = "Mod";
            }
            if (operator == null) {
                return left;
            }
            double right = number(parseUnary());
            left = switch (operator) {
                case "*" -> number(left) * right;
                case "/" -> number(left) / right;
                case "Mod" -> number(left) % right;
                default -> (double) ((long) number(left) / (long) right);
            };
        }
    }

    private @Nullable Object parseUnary() {
        if (symbol("-") != null) {
            return -number(parseUnary());
        }
        if (symbol("+") != null) {
            return number(parseUnary());
        }
        return parsePrimary();
    }

    private @Nullable Object parsePrimary() {
        skipWhitespace();
        if (myPosition >= myText.length()) {
            throw new IllegalArgumentException("expression expected");
        }
        char c = myText.charAt(myPosition);
        if (c == '(') {
            myPosition++;
            Object value = parseOr();
            if (symbol(")") == null) {
                throw new IllegalArgumentException("')' expected");
            }
            return value;
        }
        if (c == '"') {
            int end = myText.indexOf('"', myPosition + 1);
            if (end < 0) {
                throw new IllegalArgumentException("unterminated string");
            }
            String value = myText.substring(myPosition + 1, end);
            myPosition = end + 1;
            return value;
        }
        if (Character.isDigit(c)) {
            int start = myPosition;
            while (myPosition < myText.length() && (Character.isLetterOrDigit(myText.charAt(myPosition)) || myText.charAt(myPosition) == '.')) {
                myPosition++;
            }
            String literal = myText.substring(start, myPosition).replaceAll("[a-zA-Z%&@!#]+$", "");
            return Double.parseDouble(literal);
        }
        if (Character.isLetter(c) || c == '_' || c == '[') {
            String name = identifier();
            return switch (name.toLowerCase(Locale.ROOT)) {
                case "true" -> Boolean.TRUE;
                case "false" -> Boolean.FALSE;
                case "nothing" -> null;
                default -> myConstants.get(name.toLowerCase(Locale.ROOT));
            };
        }
        throw new IllegalArgumentException("unexpected " + c);
    }

    private String identifier() {
        boolean bracketed = myText.charAt(myPosition) == '[';
        if (bracketed) {
            myPosition++;
        }
        int start = myPosition;
        while (myPosition < myText.length() && (Character.isLetterOrDigit(myText.charAt(myPosition)) || myText.charAt(myPosition) == '_')) {
            myPosition++;
        }
        String name = myText.substring(start, myPosition);
        if (bracketed && myPosition < myText.length() && myText.charAt(myPosition) == ']') {
            myPosition++;
        }
        return name;
    }

    private boolean word(String word) {
        skipWhitespace();
        int end = myPosition + word.length();
        if (end > myText.length() || !myText.regionMatches(true, myPosition, word, 0, word.length())) {
            return false;
        }
        if (end < myText.length() && (Character.isLetterOrDigit(myText.charAt(end)) || myText.charAt(end) == '_')) {
            return false;
        }
        myPosition = end;
        return true;
    }

    private @Nullable String symbol(String... symbols) {
        skipWhitespace();
        for (String symbol : symbols) {
            if (myText.startsWith(symbol, myPosition)) {
                myPosition += symbol.length();
                return symbol;
            }
        }
        return null;
    }

    private void skipWhitespace() {
        while (myPosition < myText.length() && Character.isWhitespace(myText.charAt(myPosition))) {
            myPosition++;
        }
    }

    private static int compare(@Nullable Object left, @Nullable Object right) {
        if (left instanceof String || right instanceof String) {
            return text(left).compareTo(text(right));
        }
        return Double.compare(number(left), number(right));
    }

    /**
     * True is -1, as in Visual Basic.
     */
    private static double number(@Nullable Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? -1 : 0;
        }
        if (value == null) {
            return 0;
        }
        throw new IllegalArgumentException("not a number: " + value);
    }

    private static String text(@Nullable Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Double number && number == Math.rint(number)) {
            return Long.toString(number.longValue());
        }
        return value.toString();
    }
}
