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

package consulo.visualBasic.dotnet.impl.lang.moduleAware;

import consulo.index.io.data.DataExternalizer;
import consulo.index.io.data.DataInputOutputUtil;
import consulo.index.io.data.IOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-24
 */
public class VisualBasicCompileOptionsExternalizer implements DataExternalizer<VisualBasicCompileOptions> {
    public static final VisualBasicCompileOptionsExternalizer INSTANCE = new VisualBasicCompileOptionsExternalizer();

    @Override
    public void save(DataOutput out, VisualBasicCompileOptions value) throws IOException {
        IOUtil.writeUTF(out, value.rootNamespace());
        DataInputOutputUtil.writeINT(out, value.constants().size());
        for (Map.Entry<String, String> entry : value.constants().entrySet()) {
            IOUtil.writeUTF(out, entry.getKey());
            IOUtil.writeUTF(out, entry.getValue());
        }
    }

    @Override
    public VisualBasicCompileOptions read(DataInput in) throws IOException {
        String rootNamespace = IOUtil.readUTF(in);
        int count = DataInputOutputUtil.readINT(in);
        Map<String, String> constants = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            String name = IOUtil.readUTF(in);
            constants.put(name, IOUtil.readUTF(in));
        }
        return new VisualBasicCompileOptions(rootNamespace, Collections.unmodifiableMap(constants));
    }
}
