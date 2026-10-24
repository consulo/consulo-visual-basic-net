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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Conditional compilation state while parsing: which code is active, and the constants of {@code #Const}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public class VisualBasicPreprocessorState {
    private static final class Branch {
        boolean myActive;
        // a branch of this #If was taken: following #ElseIf and #Else are inactive
        boolean myTaken;

        Branch(boolean active) {
            myActive = active;
            myTaken = active;
        }
    }

    private final Deque<Branch> myBranches = new ArrayDeque<>();
    private final Map<String, Object> myConstants = new HashMap<>();

    /**
     * @param defines constants of the project: values as expressions, by name. Values may use the constants before them.
     */
    public VisualBasicPreprocessorState(Map<String, String> defines) {
        for (Map.Entry<String, String> define : defines.entrySet()) {
            myConstants.put(define.getKey().toLowerCase(Locale.ROOT), VisualBasicPreprocessorEvaluator.evaluate(define.getValue(), myConstants));
        }
    }

    public boolean isActive() {
        for (Branch branch : myBranches) {
            if (!branch.myActive) {
                return false;
            }
        }
        return true;
    }

    private boolean isParentActive() {
        Iterator<Branch> iterator = myBranches.iterator();
        if (iterator.hasNext()) {
            // the innermost branch
            iterator.next();
        }
        while (iterator.hasNext()) {
            if (!iterator.next().myActive) {
                return false;
            }
        }
        return true;
    }

    /**
     * Applies a directive.
     *
     * @return true when the directive itself is in inactive code
     */
    public boolean process(VisualBasicDirective directive) {
        switch (directive.kind()) {
            case IF -> {
                if (!isActive()) {
                    Branch branch = new Branch(false);
                    branch.myTaken = true;
                    myBranches.push(branch);
                    return true;
                }
                myBranches.push(new Branch(evaluate(directive.expression())));
                return false;
            }
            case ELSE_IF, ELSE -> {
                Branch branch = myBranches.peek();
                if (branch == null) {
                    return !isActive();
                }
                if (!isParentActive()) {
                    return true;
                }
                if (branch.myTaken) {
                    branch.myActive = false;
                }
                else {
                    branch.myActive = directive.kind() == VisualBasicDirective.Kind.ELSE || evaluate(directive.expression());
                    branch.myTaken = branch.myActive;
                }
                return false;
            }
            case END_IF -> {
                if (!myBranches.isEmpty()) {
                    myBranches.pop();
                }
                return !isActive();
            }
            case CONST -> {
                if (!isActive()) {
                    return true;
                }
                String name = directive.name();
                if (name != null) {
                    Object value = VisualBasicPreprocessorEvaluator.evaluate(directive.expression(), myConstants);
                    myConstants.put(name.toLowerCase(Locale.ROOT), value);
                }
                return false;
            }
            default -> {
                return !isActive();
            }
        }
    }

    private boolean evaluate(String expression) {
        return VisualBasicPreprocessorEvaluator.isTrue(VisualBasicPreprocessorEvaluator.evaluate(expression, myConstants));
    }

    public @Nullable Object getConstant(String name) {
        return myConstants.get(name.toLowerCase(Locale.ROOT));
    }
}
