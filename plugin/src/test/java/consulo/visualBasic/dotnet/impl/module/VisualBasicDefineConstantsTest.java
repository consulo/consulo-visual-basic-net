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

package consulo.visualBasic.dotnet.impl.module;

import consulo.visualBasic.dotnet.module.VisualBasicDefineConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-25
 */
public class VisualBasicDefineConstantsTest {
    @Test
    public void projectSystemConstants() {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("CONFIG", "\"Debug\"");
        expected.put("DEBUG", "-1");
        expected.put("TRACE", "-1");
        expected.put("_MyType", "\"Console\"");
        expected.put("PLATFORM", "\"Any, CPU\"");
        expected.put("FEATURE", "True");
        Assertions.assertEquals(expected,
            VisualBasicDefineConstants.parse("CONFIG=\"Debug\",DEBUG=-1,TRACE=-1,_MyType=\"Console\",PLATFORM=\"Any, CPU\";FEATURE"));
    }

    @Test
    public void empty() {
        Assertions.assertEquals(Map.of(), VisualBasicDefineConstants.parse(" , ;"));
    }
}
