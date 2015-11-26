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

package net.bulletin.pdi.xero.step;

import net.bulletin.pdi.xero.step.support.Helpers;
import net.bulletin.pdi.xero.step.support.XMLChunker;
import net.bulletin.pdi.xero.step.support.XMLChunkerImpl;
import net.oauth.*;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;
import net.oauth.signature.RSA_SHA1;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import javax.xml.stream.*;
import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class drives the logic for the step.
 *
 * @author Andrew Lindesay
 */

public class XeroGetStep extends BaseStep implements StepInterface {

    /**
     * <p>The container elements define what elements to 'pick out' from the XML stream
     * from Xero and to copy into the Kettle stream.  The user supplies this as a string
     * and so it should comply with this format.</p>
     */

    private static final Pattern PATTERN_CONTAINERELEMENTS = Pattern.compile("^[A-Za-z0-9-]+(/[A-Za-z0-9-]+)*$");

    /**
     * <p>The Xero API is able to accept an "If-Modified-Since" header and only material
     * up to and after that date is included in the result.  This header must be in the
     * following format or it is ignored by Xero.  Note that the time-zone of the
     * timestamp is explicitly relative to GMT-0.</p>
     */

    private static final String FORMAT_TIMESTAMP_IFMODIFIEDSINCE_XERO = "MMM dd yyyy HH:mm:ss";

    /**
     * <p>This is the format of the date that should be supplied by the user for the
     * "If-Modified-Since" date.</p>
     */

    private static final String FORMAT_TIMESTAMP_INPUT = "yyyy-MM-dd HH:mm:ss";

    private XMLChunker xmlChunker;

    public XeroGetStep(
            StepMeta stepMeta,
            StepDataInterface stepDataInterface,
            int copyNr,
            TransMeta transMeta,
            Trans dis) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, dis);
    }

    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        XeroGetStepMeta meta = (XeroGetStepMeta) smi;
        XeroGetStepData data = (XeroGetStepData) sdi;
        boolean result = false;

        try {
            data.setOAuthMessage(openXero(meta));

            try {

                xmlChunker = new XMLChunkerImpl(XMLInputFactory.newInstance().createXMLStreamReader(data.getOAuthMessage().getBodyAsStream()),
                        getContainerElementsStack(meta));

                result = true;
            } catch (Exception ioe) {
                throw new KettleException("unable to process the xero input as xml", ioe);
            }

        } catch (KettleException ke) {
            logError("unable to initialize the xero step", ke);
        }

        return result && super.init(smi, sdi);
    }

    private String createIfModifiedSinceHeaderValue(XeroGetStepMeta meta) throws KettleException {
        String ifModifiedSinceAsSupplied = meta.getIfModifiedSince();

        if (StringUtils.isNotBlank(ifModifiedSinceAsSupplied)) {
            ifModifiedSinceAsSupplied = environmentSubstitute(ifModifiedSinceAsSupplied);
            SimpleDateFormat formatInput = new SimpleDateFormat(FORMAT_TIMESTAMP_INPUT);

            try {
                Date ifModifiedSince = formatInput.parse(ifModifiedSinceAsSupplied);
                SimpleDateFormat formatOutput = new SimpleDateFormat(FORMAT_TIMESTAMP_IFMODIFIEDSINCE_XERO);
                formatOutput.setTimeZone(TimeZone.getTimeZone("GMT-0"));
                return formatOutput.format(ifModifiedSince);
            } catch (ParseException pe) {
                throw new KettleException("unable to parse the supplied if-modified-since; " + ifModifiedSinceAsSupplied + " (should be '" + FORMAT_TIMESTAMP_INPUT + "')", pe);
            }
        }

        return null;
    }

    private String createXeroUrl(XeroGetStepMeta meta) throws KettleException {
        StringBuilder url = new StringBuilder(environmentSubstitute(StringUtils.trimToEmpty(meta.getUrl())));

        if (0 == url.length()) {
            throw new KettleException("the xero url must be supplied");
        }

        return Helpers.appendUrlQuery(
                url,
                "where",
                environmentSubstitute(StringUtils.trimToEmpty(meta.getWhere()))).toString();
    }

    /**
     * <p>The Xero cert to use is stored in a file which is pointed at from the meta-data for
     * the step.  This method will pull in that file and turn it into a string so that it can
     * be used.</p>
     */

    private String readXeroKey(XeroGetStepMeta meta) throws KettleException {
        String keyFilePath = environmentSubstitute(meta.getAuthenticationKeyFile());

        logBasic("will load xero key from; " + keyFilePath);

        File file = new File(keyFilePath);

        if (!file.exists()) {
            throw new KettleException("the xero key file cannot be found; " + file.getAbsolutePath());
        }

        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(file);
            String key = StringUtils.trimToEmpty(IOUtils.toString(inputStream, CharEncoding.UTF_8));

            if (StringUtils.isBlank(key)) {
                throw new KettleException("the xero key file appears to be blank; " + file.getAbsolutePath());
            }

            return key;
        } catch (IOException ioe) {
            throw new KettleException("unable to process the xero key file; " + file.getAbsolutePath());
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private OAuthMessage openXero(XeroGetStepMeta meta) throws KettleException {

        String consumerKey = environmentSubstitute(meta.getAuthenticationConsumerKey());

        if (StringUtils.isBlank(consumerKey)) {
            throw new KettleException("the xero consumer key must be supplied");
        }

        logBasic("will use xero consumer key; " + Helpers.obfuscateAuthenticationDetailForLog(consumerKey));

        String key = readXeroKey(meta);
        logBasic("will use xero key; " + Helpers.obfuscateAuthenticationDetailForLog(key));

        String url = createXeroUrl(meta);
        String ifModifiedSinceHeaderValue = createIfModifiedSinceHeaderValue(meta);

        OAuthServiceProvider serviceProvider = new OAuthServiceProvider(null, null, null);
        OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, null, serviceProvider);
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, key);

        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.accessToken = consumerKey;

        try {
            OAuthMessage request = accessor.newRequestMessage(
                    "GET",
                    createXeroUrl(meta),
                    new ArrayList<Map.Entry>(),
                    null);

            OAuthClient client = new OAuthClient(new URLConnectionClient());
            request.getHeaders().add(new OAuth.Parameter("Accept", "text/xml"));

            if (StringUtils.isNotBlank(ifModifiedSinceHeaderValue)) {
                request.getHeaders().add(new OAuth.Parameter("If-Modified-Since", ifModifiedSinceHeaderValue));
                logBasic("will use 'If-Modified-Since' header of; " + ifModifiedSinceHeaderValue);
            }

            Object ps = accessor.consumer.getProperty(OAuthClient.PARAMETER_STYLE);
            ParameterStyle style = (ps == null) ? ParameterStyle.BODY
                    : Enum.valueOf(ParameterStyle.class, ps.toString());

            OAuthMessage result = client.invoke(request, style);
            logBasic("did open xero connection to; " + url);
            return result;

        } catch (OAuthProblemException e) {
            // http://developer.xero.com/documentation/getting-started/http-response-codes/
            throw new KettleException("error has arisen communicating with xero api at; " + meta.getUrl() + " (" + e.getHttpStatusCode() + ")", e);
        } catch (IOException e) {
            throw new KettleException("error has arisen communicating with xero api at; " + meta.getUrl(), e);
        } catch (OAuthException e) {
            throw new KettleException("error has arisen communicating with xero api at; " + meta.getUrl(), e);
        } catch (URISyntaxException use) {
            throw new KettleException("the supplied URI syntax is malformed; " + meta.getUrl(), use);
        }

    }

    private Stack<String> getContainerElementsStack(XeroGetStepMeta meta) throws KettleException {
        Stack<String> result = new Stack<String>();
        String ce = StringUtils.trimToEmpty(meta.getContainerElements());

        while (ce.startsWith("/")) {
            ce = ce.substring(1);
        }

        if (StringUtils.isNotBlank(ce)) {
            if (!PATTERN_CONTAINERELEMENTS.matcher(ce).matches()) {
                throw new KettleException("malformed container elements; " + ce);
            }

            Collections.addAll(result, ce.split("/"));
        }

        return result;
    }

    /**
     * <p>Pulls in some more of the stream from Xero and pushes hunks of XML into the
     * stream.</p>
     */

    @Override
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        XeroGetStepMeta meta = (XeroGetStepMeta) smi;
        XeroGetStepData data = (XeroGetStepData) sdi;

        if (first) {
            first = false;
            RowMetaInterface outputRowMeta = new RowMeta();
            meta.getFields(outputRowMeta, getStepname(), null, null, this, null, null);
            data.setOutputRowMeta(outputRowMeta);
        }

        String xml = xmlChunker.pullNextXmlChunk();

        if (null != xml) {
            Object[] outputRow = RowDataUtil.allocateRowData(1);
            outputRow[0] = xml;
            putRow(data.getOutputRowMeta(), outputRow);
            incrementLinesOutput();
            return true;
        }

        setOutputDone();
        return false;
    }

    @Override
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        XeroGetStepMeta meta = (XeroGetStepMeta) smi;
        XeroGetStepData data = (XeroGetStepData) sdi;

        try {
            xmlChunker.close();
        } catch (KettleException e) {
            logError("unable to close the xml stream from xero", e);
        }

        OAuthMessage oAuthMessage = data.getOAuthMessage();

        if (null != oAuthMessage) {
            try {
                InputStream inputStream = oAuthMessage.getBodyAsStream();

                if (null != inputStream) {
                    inputStream.close();
                    logBasic("did close xero connection");
                }
            } catch (IOException ioe) {
                logError("unable to close the oauth stream from xero", ioe);
            }
        }

        super.dispose(meta, data);
    }

}
