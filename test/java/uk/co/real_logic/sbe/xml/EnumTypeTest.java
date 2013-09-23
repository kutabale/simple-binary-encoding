/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
/*
 * Copyright 2013 Real Logic Ltd.
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
package uk.co.real_logic.sbe.xml;

import uk.co.real_logic.sbe.Primitive;
import uk.co.real_logic.sbe.PrimitiveValue;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static java.lang.Integer.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class EnumTypeTest
{

    /**
     * Grab type nodes, parse them, and populate map for those types.
     *
     * @param xPathExpr for type nodes in XML
     * @param xml       string to parse
     * @return map of name to EnumType nodes
     */
    private static Map<String, Type> parseTestXmlWithMap(final String xPathExpr, final String xml)
        throws ParserConfigurationException, XPathExpressionException, IOException, SAXException
    {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile(xPathExpr).evaluate(document, XPathConstants.NODESET);
        Map<String, Type> map = new HashMap<String, Type>();

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Type t = new EnumType(list.item(i));
            map.put(t.getName(), t);
        }

        return map;
    }

    @Test
    public void shouldHandleBinaryEnumType()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<enum name=\"biOp\" encodingType=\"uint8\">" +
            " <validValue name=\"off\" description=\"switch is off\">0</validValue>" +
            " <validValue name=\"on\" description=\"switch is on\">1</validValue>" +
            "</enum>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        EnumType e = (EnumType)map.get("biOp");
        assertThat(e.getName(), is("biOp"));
        assertThat(e.getEncodingType(), is(Primitive.UINT8));
        assertThat(valueOf(e.getValidValueSet().size()), is(valueOf(2)));
        assertThat(e.getValidValue("on").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "1")));
        assertThat(e.getValidValue("off").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "0")));
    }

    @Test
    public void shouldHandleBooleanEnumType()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<enum name=\"boolean\" encodingType=\"uint8\" fixUsage=\"Boolean\">" +
            " <validValue name=\"false\">0</validValue>" +
            " <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        EnumType e = (EnumType)map.get("boolean");
        assertThat(e.getName(), is("boolean"));
        assertThat(e.getEncodingType(), is(Primitive.UINT8));
        assertThat(valueOf(e.getValidValueSet().size()), is(valueOf(2)));
        assertThat(e.getValidValue("true").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "1")));
        assertThat(e.getValidValue("false").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "0")));
    }

    @Test
    public void shouldHandleOptionalBooleanEnumType()
        throws Exception
    {
        final String nullValueStr = "255";
        final String testXmlString = "<types>" +
            "<enum name=\"optionalBoolean\" encodingType=\"uint8\" presence=\"optional\" nullValue=\"" + nullValueStr + "\" fixUsage=\"Boolean\">" +
            " <validValue name=\"false\">0</validValue>" +
            " <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        EnumType e = (EnumType)map.get("optionalBoolean");
        assertThat(e.getName(), is("optionalBoolean"));
        assertThat(e.getEncodingType(), is(Primitive.UINT8));
        assertThat(valueOf(e.getValidValueSet().size()), is(valueOf(2)));
        assertThat(e.getValidValue("true").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "1")));
        assertThat(e.getValidValue("false").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "0")));
        assertThat(e.getNullValue(), is(new PrimitiveValue(Primitive.UINT8, nullValueStr)));
    }

    @Test
    public void shouldHandleEnumTypeList()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<enum name=\"triOp\" encodingType=\"uint8\">" +
            " <validValue name=\"off\" description=\"switch is off\">0</validValue>" +
            " <validValue name=\"on\" description=\"switch is on\">1</validValue>" +
            " <validValue name=\"not-known\" description=\"switch is unknown\">2</validValue>" +
            "</enum>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        EnumType e = (EnumType)map.get("triOp");
        assertThat(e.getName(), is("triOp"));
        assertThat(e.getEncodingType(), is(Primitive.UINT8));

        int foundOn = 0, foundOff = 0, foundNotKnown = 0, count = 0;
        for (Map.Entry<String, EnumType.ValidValue> entry : e.getValidValueSet())
        {
            if (entry.getKey().equals("on"))
            {
                foundOn++;
            }
            else if (entry.getKey().equals("off"))
            {
                foundOff++;
            }
            else if (entry.getKey().equals("not-known"))
            {
                foundNotKnown++;
            }
            count++;
        }
        assertThat(valueOf(count), is(valueOf(3)));
        assertThat(valueOf(foundOn), is(valueOf(1)));
        assertThat(valueOf(foundOff), is(valueOf(1)));
        assertThat(valueOf(foundNotKnown), is(valueOf(1)));
    }

    @Test
    public void shouldHandleCharEnumEncodingType()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<enum name=\"mixed\" encodingType=\"char\">" +
            " <validValue name=\"Cee\">C</validValue>" +
            " <validValue name=\"One\">1</validValue>" +
            " <validValue name=\"Two\">2</validValue>" +
            " <validValue name=\"Eee\">E</validValue>" +
            "</enum>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        EnumType e = (EnumType)map.get("mixed");
        assertThat(e.getEncodingType(), is(Primitive.CHAR));
        assertThat(e.getValidValue("Cee").getPrimitiveValue(), is(new PrimitiveValue(Primitive.CHAR, "C")));
        assertThat(e.getValidValue("One").getPrimitiveValue(), is(new PrimitiveValue(Primitive.CHAR, "1")));
        assertThat(e.getValidValue("Two").getPrimitiveValue(), is(new PrimitiveValue(Primitive.CHAR, "2")));
        assertThat(e.getValidValue("Eee").getPrimitiveValue(), is(new PrimitiveValue(Primitive.CHAR, "E")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenIllegalEncodingTypeSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<enum name=\"boolean\" encodingType=\"int64\" fixUsage=\"Boolean\">" +
            " <validValue name=\"false\">0</validValue>" +
            " <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        parseTestXmlWithMap("/types/enum", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDuplicateValueSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<enum name=\"boolean\" encodingType=\"uint8\" fixUsage=\"Boolean\">" +
            " <validValue name=\"false\">0</validValue>" +
            " <validValue name=\"anotherFalse\">0</validValue>" +
            " <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        parseTestXmlWithMap("/types/enum", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDuplicateNameSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<enum name=\"boolean\" encodingType=\"uint8\" fixUsage=\"Boolean\">" +
            " <validValue name=\"false\">0</validValue>" +
            " <validValue name=\"false\">2</validValue>" +
            " <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        parseTestXmlWithMap("/types/enum", testXmlString);
    }
}