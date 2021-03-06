/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.android.dom.structure.resources;

import com.android.resources.ResourceType;
import com.google.common.collect.Lists;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementVisitor;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.android.dom.resources.ResourceElement;
import org.jetbrains.android.dom.resources.Resources;
import org.jetbrains.android.dom.structure.StructureUtils;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Structure view builder for &lt;resources&gt; XML files
 */
public class ResourceStructureViewBuilder extends TreeBasedStructureViewBuilder {
  private final DomFileElement<Resources> myResources;

  public ResourceStructureViewBuilder(@NotNull DomFileElement<Resources> resources) {
    myResources = resources;
  }

  @NotNull
  @Override
  public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
    return new StructureViewModelBase(myResources.getFile(), new Root(myResources));
  }

  private static class Root extends PsiTreeElementBase<PsiElement> {
    private final DomFileElement<Resources> myResources;

    Root(@NotNull DomFileElement<Resources> resources) {
      super(resources.getXmlElement());
      myResources = resources;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
      final List<StructureViewTreeElement> result = Lists.newArrayList();
      final DomElementVisitor visitor = new DomElementVisitor() {
        public void visitResourceElement(ResourceElement element) {
          final ResourceType type = AndroidResourceUtil.getType(element.getXmlTag());
          final String name = element.getName().getValue();
          final XmlElement xmlElement = element.getXmlElement();
          if (name != null && type != null && xmlElement != null) {
            result.add(new Leaf(xmlElement, name, type));
          }
        }

        @Override
        public void visitDomElement(DomElement element) {
        }
      };
      StructureUtils.acceptChildrenInOrder(myResources.getRootElement(), visitor);
      return result;
    }

    @NotNull
    @Override
    public String getPresentableText() {
      return String.format("Resources file '%s'", myResources.getFile().getName());
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder(getPresentableText());
      builder.append('\n');
      for (StructureViewTreeElement child : getChildrenBase()) {
        builder.append("  ").append(child.toString()).append('\n');
      }
      return builder.toString();
    }
  }

  private static class Leaf extends PsiTreeElementBase<PsiElement> {
    private final String myName;
    private final ResourceType myResourceType;

    protected Leaf(@NotNull PsiElement psiElement, @NotNull String name, @NotNull ResourceType resourceType) {
      super(psiElement);
      myName = name;
      myResourceType = resourceType;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
      return Collections.emptyList();
    }

    @Nullable
    @Override
    public String getPresentableText() {
      return myResourceType.getDisplayName() + " - " + myName;
    }

    @Override
    public String toString() {
      return getPresentableText();
    }
  }
}
