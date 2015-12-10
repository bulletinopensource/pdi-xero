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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/**
 * @author Andrew Lindesay
 */

public class XMLChunkerTest {

    private byte[] readSampleXml() throws IOException {
        InputStream inputStream = null;

        try {
            inputStream = XMLChunkerTest.class.getResourceAsStream("/sample_xml_a.xml");

            if (null == inputStream) {
                throw new IllegalStateException("the sample xml file was unable to be accessed");
            }

            return IOUtils.toByteArray(inputStream);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    // ------------------------------------------------------
    // CHUNKING INTO THE DOCUMENT
    // In this test, the document contains a number of artists and the chunking is across the artists.

    private void checkChunkForArtistDetails(String xml, String artistName, String[] genres) throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        XPath xPath = XPathFactory.newInstance().newXPath();

        org.w3c.dom.Document doc = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes(CharEncoding.UTF_8)));

        Assert.assertEquals("the artist name does not match", artistName, xPath.evaluate("/Artist/Name", doc));

        for (int i = 0; i < genres.length; i++) {
            Assert.assertEquals(genres[i], xPath.evaluate("/Artist/Genres/Genre[" + (i + 1) + "]", doc));
        }
    }

    private Stack<String> createExpectedContainerElementsStack() {
        Stack<String> result = new Stack<String>();
        result.push("Response");
        result.push("Artists");
        result.push("Artist");
        return result;
    }

    /**
     * <p>The input document contains a number of artists in a structure.  This test will break the
     * data into individual artist such that each artist is in their own document.</p>
     */

    @Test
    public void testPullNextXmlChunk_withContainerElements() throws Exception {
        byte[] sampleXml = readSampleXml();

        XMLChunker chunker = new XMLChunkerImpl(
                XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(sampleXml)), // all in-memory
                createExpectedContainerElementsStack());

        // ---------------------------------
        String actuals[] = new String[]{
                chunker.pullNextXmlChunk(),
                chunker.pullNextXmlChunk(),
                chunker.pullNextXmlChunk(),
                chunker.pullNextXmlChunk()
        };
        // ---------------------------------

        // This will work through the chunks and check specific information it knows in the sample.
        checkChunkForArtistDetails(actuals[0], "Len Lye", new String[]{"Kinetic Sculpture", "Poetry", "Film"});
        checkChunkForArtistDetails(actuals[1], "Alexander Caulder", new String[]{"Sculpture"});
        checkChunkForArtistDetails(actuals[2], "Marc Chagall", new String[]{"Painting"});
        Assert.assertNull("expected the last chunk to be null", actuals[3]);

    }

    /**
     * <p>This test is checking to see that, without any container elements, the chunking will produce one
     * document and that single document should be the whole of the input.</p>
     */

    @Test
    public void testPullNextXmlChunk_withoutContainerElements() throws Exception {
        byte[] sampleXml = readSampleXml();

        XMLChunker chunker = new XMLChunkerImpl(
                XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(sampleXml)), // all in-memory
                new Stack<String>());

        // ---------------------------------
        String actuals[] = new String[]{
                chunker.pullNextXmlChunk(),
                chunker.pullNextXmlChunk()
        };
        // ---------------------------------

        // This will work through the chunks and check specific information it knows in the sample.

        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPath xPath = XPathFactory.newInstance().newXPath();

            org.w3c.dom.Document doc = documentBuilder.parse(new ByteArrayInputStream(actuals[0].getBytes(CharEncoding.UTF_8)));
            NodeList artistNodeList = (NodeList) xPath.evaluate("/Response/Artists/Artist", doc, XPathConstants.NODESET);

            Assert.assertEquals(3, artistNodeList.getLength());
        }

        Assert.assertNull("expected the last chunk to be null", actuals[1]);

    }

}
