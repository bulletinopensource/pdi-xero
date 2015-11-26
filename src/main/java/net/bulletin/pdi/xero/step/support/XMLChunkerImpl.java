/**
 *    Copyright 2015 Bulletin.Net (NZ) Limited : www.bulletin.net
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

package net.bulletin.pdi.xero.step.support;

import org.apache.commons.lang.CharEncoding;
import org.apache.xmlbeans.impl.common.XmlReaderToWriter;
import org.pentaho.di.core.exception.KettleException;

import javax.xml.stream.*;
import java.io.StringWriter;
import java.util.Stack;

/**
 * This class is able to process an XML stream to pull chunks out of it based on a series of elements
 * that demarcate the chunks boundary.
 *
 * @author Andrew Lindesay
 */

public class XMLChunkerImpl implements XMLChunker {

    private XMLChunkerState xmlChunkerState;

    public XMLChunkerImpl(XMLStreamReader xmlStreamReader, Stack<String> expectedContainerElementsStack) {
        xmlChunkerState = new XMLChunkerState(xmlStreamReader, expectedContainerElementsStack);
    }

    /**
     * <p>Checks to see if the expected elements are present at the start of the actual current elements.</p>
     *
     * @return true if the expected elements can be seen at the start of the actual elements.
     */

    private boolean actualElementStackHasExpectedElements(XMLChunkerState data) {
        Stack<String> actual = data.getElementStack();
        Stack<String> expected = data.getExpectedContainerElementsStack();

        if (actual.size() < expected.size()) {
            return false;
        }

        for (int i = 0; i < expected.size(); i++) {
            if (!actual.get(i).equals(expected.get(i))) {
                return false;
            }
        }

        return true;
    }

    private String pullNextXmlChunkFromTopElementOnStack(XMLChunkerState data) throws KettleException {
        Stack<String> elementStack = data.getElementStack();
        XMLStreamReader xmlStreamReader = data.getXmlStreamReader();

        int elementStackDepthOnEntry = elementStack.size();
        StringWriter stringWriter = new StringWriter();

        try {
            XMLStreamWriter xmlStreamWriter = data.getXmlOutputFactory().createXMLStreamWriter(stringWriter);

            xmlStreamWriter.writeStartDocument(CharEncoding.UTF_8, "1.0");

            // put the current element on because presumably it's the open element for the one
            // that is being looked for.

            XmlReaderToWriter.write(xmlStreamReader, xmlStreamWriter);

            while (xmlStreamReader.hasNext() & elementStack.size() >= elementStackDepthOnEntry) {

                switch (xmlStreamReader.next()) {

                    case XMLStreamConstants.END_DOCUMENT:
                        break; // handled below explicitly.

                    case XMLStreamConstants.END_ELEMENT:
                        elementStack.pop();
                        XmlReaderToWriter.write(xmlStreamReader, xmlStreamWriter);
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        elementStack.push(xmlStreamReader.getLocalName());
                        XmlReaderToWriter.write(xmlStreamReader, xmlStreamWriter);
                        break;

                    default:
                        XmlReaderToWriter.write(xmlStreamReader, xmlStreamWriter);
                        break;

                }

            }

            xmlStreamWriter.writeEndDocument();
            xmlStreamWriter.close();
        } catch (Exception e) {
            throw new KettleException("unable to process a chunk of the xero xml stream", e);
        }

        return stringWriter.toString();
    }

    @Override
    public String pullNextXmlChunk() throws KettleException {
        Stack<String> elementStack = xmlChunkerState.getElementStack();
        XMLStreamReader xmlStreamReader = xmlChunkerState.getXmlStreamReader();

        try {

            while (xmlStreamReader.hasNext()) {

                switch (xmlStreamReader.next()) {

                    case XMLStreamConstants.END_DOCUMENT:
                        return null;

                    case XMLStreamConstants.END_ELEMENT:
                        elementStack.pop();
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        elementStack.push(xmlStreamReader.getLocalName());

                        if (actualElementStackHasExpectedElements(xmlChunkerState)) {
                            return pullNextXmlChunkFromTopElementOnStack(xmlChunkerState);
                        }

                        break;

                }

            }
        } catch (Exception e) {
            throw new KettleException("a problem has arisen reading the xero xml stream", e);
        }

        return null;
    }

    @Override
    public void close() throws KettleException {
        if (null != xmlChunkerState && null != xmlChunkerState.xmlStreamReader) {
            try {
                xmlChunkerState.xmlStreamReader.close();
            } catch (XMLStreamException xse) {
                 throw new KettleException(xse);
            }
        }
    }


    private class XMLChunkerState {

        private XMLStreamReader xmlStreamReader = null;

        private Stack<String> elementStack = new Stack<String>();

        private Stack<String> expectedContainerElementsStack;

        private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        public XMLChunkerState(XMLStreamReader xmlStreamReader, Stack<String> expectedContainerElementsStack) {

            if (null == xmlStreamReader) {
                throw new IllegalArgumentException("an xml stream reader is required");
            }

            if (null == expectedContainerElementsStack) {
                throw new IllegalArgumentException("an expected container element stack is required");
            }

            this.expectedContainerElementsStack = expectedContainerElementsStack;
            this.xmlStreamReader = xmlStreamReader;
        }

        public XMLStreamReader getXmlStreamReader() {
            return xmlStreamReader;
        }

        public Stack<String> getElementStack() {
            return elementStack;
        }

        public Stack<String> getExpectedContainerElementsStack() {
            return expectedContainerElementsStack;
        }

        public XMLOutputFactory getXmlOutputFactory() {
            return xmlOutputFactory;
        }
    }

}
