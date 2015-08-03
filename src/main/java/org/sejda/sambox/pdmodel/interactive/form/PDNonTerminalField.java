/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sejda.sambox.pdmodel.interactive.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sejda.sambox.cos.COSArray;
import org.sejda.sambox.cos.COSArrayList;
import org.sejda.sambox.cos.COSBase;
import org.sejda.sambox.cos.COSDictionary;
import org.sejda.sambox.cos.COSInteger;
import org.sejda.sambox.cos.COSName;

/**
 * A non terminal field in an interactive form.
 * 
 * A non terminal field is a node in the fields tree node whose descendants
 * are fields. 
 * 
 * The attributes such as FT (field type) or V (field value) do not logically
 * belong to the non terminal field but are inheritable attributes
 * for descendant terminal fields.
 */
public class PDNonTerminalField extends PDField
{
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     */
    public PDNonTerminalField(PDAcroForm acroForm)
    {
        super(acroForm);
    }

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node to be created
     */
    PDNonTerminalField(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }
    
    @Override
    public int getFieldFlags()
    {
        int retval = 0;
        COSInteger ff = (COSInteger) dictionary.getDictionaryObject(COSName.FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        // There is no need to look up the parent hierarchy within a non terminal field
        return retval;
    }

    /**
     * Returns this field's children. These may be either terminal or non-terminal fields.
     * 
     * @return he list of child fields.
     */
    public List<PDField> getChildren()
    {
        List<PDField> children = new ArrayList<PDField>();
        COSArray kids = (COSArray)dictionary.getDictionaryObject(COSName.KIDS);
        for (int i = 0; i < kids.size(); i++)
        {
            PDField field = PDField.fromDictionary(acroForm, (COSDictionary)kids.getObject(i), this);
            children.add(field);
        }
        return children;
    }
    
    /**
     * Sets the child fields.
     *
     * @param children The list of child fields.
     */
    public void setChildren(List<PDField> children)
    {
        COSArray kidsArray = COSArrayList.converterToCOSArray(children);
        dictionary.setItem(COSName.KIDS, kidsArray);
    }

    /**
     * @inheritDoc
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    @Override
    public String getFieldType()
    {
        return dictionary.getNameAsString(COSName.FT);
    }

    /**
     * @inheritDoc
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    public COSBase getValue()
    {
        return dictionary.getDictionaryObject(COSName.V);
    }

    /**
     * @inheritDoc
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    @Override
    public String getValueAsString()
    {
        return dictionary.getDictionaryObject(COSName.V).toString();
    }

    /**
     * Sets the value of this field. This may be of any kind which is valid for this field's
     * children.
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    public void setValue(COSBase object) throws IOException
    {
        dictionary.setItem(COSName.V, object);
        // todo: propagate change event to children?
        // todo: construct appearances of children?
    }

    /**
     * Returns the default value of this field. This may be of any kind which is valid for this field's
     * children.
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    public COSBase getDefaultValue()
    {
        return dictionary.getDictionaryObject(COSName.DV);
    }

    /**
     * Sets the default of this field. This may be of any kind which is valid for this field's
     * children.
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    public void setDefaultValue(COSBase value)
    {
        dictionary.setItem(COSName.V, value);
    }
}