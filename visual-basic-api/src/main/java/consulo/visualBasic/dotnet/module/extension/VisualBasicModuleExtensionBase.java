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

package consulo.visualBasic.dotnet.module.extension;

import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionBase;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Settings of {@link VisualBasicModuleExtension}, kept in the module file.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public abstract class VisualBasicModuleExtensionBase<T extends VisualBasicModuleExtensionBase<T>> extends ModuleExtensionBase<T>
    implements VisualBasicModuleExtension<T> {
    protected String myRootNamespace = "";
    protected final List<String> myImports = new ArrayList<>();
    protected final Map<String, String> myConstants = new LinkedHashMap<>();
    protected boolean myOptionStrict;
    protected boolean myOptionExplicit = true;
    protected boolean myOptionInfer = true;
    protected VisualBasicCompareMode myOptionCompare = VisualBasicCompareMode.BINARY;

    public VisualBasicModuleExtensionBase(String id, ModuleRootLayer moduleRootLayer) {
        super(id, moduleRootLayer);
    }

    @Override
    public String getRootNamespace() {
        return myRootNamespace;
    }

    public void setRootNamespace(String rootNamespace) {
        myRootNamespace = rootNamespace;
    }

    @Override
    public List<String> getImports() {
        return myImports;
    }

    @Override
    public Map<String, String> getConstants() {
        return myConstants;
    }

    @Override
    public boolean isOptionStrict() {
        return myOptionStrict;
    }

    public void setOptionStrict(boolean optionStrict) {
        myOptionStrict = optionStrict;
    }

    @Override
    public boolean isOptionExplicit() {
        return myOptionExplicit;
    }

    public void setOptionExplicit(boolean optionExplicit) {
        myOptionExplicit = optionExplicit;
    }

    @Override
    public boolean isOptionInfer() {
        return myOptionInfer;
    }

    public void setOptionInfer(boolean optionInfer) {
        myOptionInfer = optionInfer;
    }

    @Override
    public VisualBasicCompareMode getOptionCompare() {
        return myOptionCompare;
    }

    public void setOptionCompare(VisualBasicCompareMode optionCompare) {
        myOptionCompare = optionCompare;
    }

    public boolean isModifiedImpl(T extension) {
        return myIsEnabled != extension.isEnabled()
            || !myRootNamespace.equals(extension.myRootNamespace)
            || !myImports.equals(extension.myImports)
            || !myConstants.equals(extension.myConstants)
            || myOptionStrict != extension.myOptionStrict
            || myOptionExplicit != extension.myOptionExplicit
            || myOptionInfer != extension.myOptionInfer
            || myOptionCompare != extension.myOptionCompare;
    }

    @Override
    public void commit(T extension) {
        super.commit(extension);
        myRootNamespace = extension.myRootNamespace;
        myImports.clear();
        myImports.addAll(extension.myImports);
        myConstants.clear();
        myConstants.putAll(extension.myConstants);
        myOptionStrict = extension.myOptionStrict;
        myOptionExplicit = extension.myOptionExplicit;
        myOptionInfer = extension.myOptionInfer;
        myOptionCompare = extension.myOptionCompare;
    }

    @Override
    protected void loadStateImpl(Element element) {
        super.loadStateImpl(element);
        myRootNamespace = element.getAttributeValue("root-namespace", "");
        myOptionStrict = Boolean.parseBoolean(element.getAttributeValue("option-strict", "false"));
        myOptionExplicit = Boolean.parseBoolean(element.getAttributeValue("option-explicit", "true"));
        myOptionInfer = Boolean.parseBoolean(element.getAttributeValue("option-infer", "true"));
        myOptionCompare = VisualBasicCompareMode.valueOf(element.getAttributeValue("option-compare", VisualBasicCompareMode.BINARY.name()));
        myImports.clear();
        for (Element importElement : element.getChildren("import")) {
            myImports.add(Objects.requireNonNullElse(importElement.getAttributeValue("value"), ""));
        }
        myConstants.clear();
        for (Element constant : element.getChildren("constant")) {
            String name = constant.getAttributeValue("name");
            if (name != null) {
                myConstants.put(name, constant.getAttributeValue("value", "True"));
            }
        }
    }

    @Override
    protected void getStateImpl(Element element) {
        super.getStateImpl(element);
        if (!myRootNamespace.isEmpty()) {
            element.setAttribute("root-namespace", myRootNamespace);
        }
        element.setAttribute("option-strict", Boolean.toString(myOptionStrict));
        element.setAttribute("option-explicit", Boolean.toString(myOptionExplicit));
        element.setAttribute("option-infer", Boolean.toString(myOptionInfer));
        element.setAttribute("option-compare", myOptionCompare.name());
        for (String imported : myImports) {
            element.addContent(new Element("import").setAttribute("value", imported));
        }
        for (Map.Entry<String, String> constant : myConstants.entrySet()) {
            element.addContent(new Element("constant").setAttribute("name", constant.getKey()).setAttribute("value", constant.getValue()));
        }
    }
}
