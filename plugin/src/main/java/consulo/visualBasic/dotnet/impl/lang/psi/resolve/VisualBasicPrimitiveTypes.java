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

package consulo.visualBasic.dotnet.impl.lang.psi.resolve;

import consulo.dotnet.DotNetTypes;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Numeric types and the widening of Visual Basic arithmetic: {@code Integer + Long} is {@code Long},
 * {@code Integer + UInteger} is {@code Long}, {@code Long + ULong} is {@code Decimal}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicPrimitiveTypes {
    // by widening rank; signed and unsigned of the same size share a rank
    private static final List<String> SIGNED = List.of(
        DotNetTypes.System.SByte,
        DotNetTypes.System.Int16,
        DotNetTypes.System.Int32,
        DotNetTypes.System.Int64,
        DotNetTypes.System.Decimal,
        DotNetTypes.System.Single,
        DotNetTypes.System.Double
    );

    private static final List<String> UNSIGNED = List.of(
        DotNetTypes.System.Byte,
        DotNetTypes.System.UInt16,
        DotNetTypes.System.UInt32,
        DotNetTypes.System.UInt64
    );

    private VisualBasicPrimitiveTypes() {
    }

    public static boolean isNumeric(String vmQName) {
        return SIGNED.contains(vmQName) || UNSIGNED.contains(vmQName);
    }

    public static boolean isIntegral(String vmQName) {
        return UNSIGNED.contains(vmQName) || SIGNED.indexOf(vmQName) >= 0 && SIGNED.indexOf(vmQName) <= 3;
    }

    private static int rank(String vmQName) {
        int signed = SIGNED.indexOf(vmQName);
        return signed >= 0 ? signed : UNSIGNED.indexOf(vmQName);
    }

    /**
     * @return the type both numeric types widen to, null when one is not numeric
     */
    public static @Nullable String widen(String left, String right) {
        if (!isNumeric(left) || !isNumeric(right)) {
            return null;
        }
        if (left.equals(right)) {
            return left;
        }
        boolean leftUnsigned = UNSIGNED.contains(left);
        boolean rightUnsigned = UNSIGNED.contains(right);
        int rank = Math.max(rank(left), rank(right));
        if (leftUnsigned && rightUnsigned) {
            return UNSIGNED.get(rank);
        }
        if (leftUnsigned != rightUnsigned) {
            String unsigned = leftUnsigned ? left : right;
            String signed = leftUnsigned ? right : left;
            // a signed type wide enough for the unsigned one
            if (rank(signed) > rank(unsigned)) {
                return signed;
            }
            return SIGNED.get(Math.min(rank(unsigned) + 1, SIGNED.size() - 1));
        }
        return SIGNED.get(rank);
    }
}
