/*
 * Copyright (c) 2004-2007 by Michael Connor. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of FormLayoutBuilder or Michael Connor nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mlc.swing.layout;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * This is the ComponentBuilder used to build JLists.
 *
 * @author Kevin Routley
@version $Id$
@since Ptolemy II 8.0
 */
public class JListComponentBuilder implements ComponentBuilder {
    List<BeanProperty> properties = new ArrayList<BeanProperty>();

    public List<BeanProperty> getProperties() {
        return properties;
    }

    public String getDeclaration(String name, Map<String, Object> properties) {
        return "javax.swing.JList "
                + name
                + "Control = new javax.swing.JList();\njavax.swing.JScrollPane "
                + name + " = new javax.swing.JScrollPane(" + name
                + "Control);\n";
    }

    public Component getInstance(Map<String, Object> properties)
            throws Exception {
        JList tree = new JList();
        JScrollPane scrollPane = new JScrollPane(tree);
        return scrollPane;
    }

    public boolean isComponentALayoutContainer() {
        return false;
    }

    public String toString() {
        return "javax.swing.JList";
    }

    public ComponentDef getComponentDef(String name,
            Map<String, Object> beanProperties) {
        String imp = "import javax.swing.JList;\n"
                + "import javax.swing.JScrollPane;";
        String decl = "JList ${name}Control = new JList();\n"
                + "JScrollPane ${name} = new JScrollPane(${name}Control);";
        String add = "${container}.add(${name}, \"${name}\");";
        ComponentDef cd = new ComponentDef(name, imp, decl, add);
        return cd;
    }

}
