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

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * <p>This class is responsible for providing the hook point for the step into the Kettle
 * infrastructure.</p>
 *
 * @author Andrew Lindesay
 */

@Step(
        id = "XeroGetStep",
        image = "net/bulletin/pdi/xero/step/resources/icon.png",
        i18nPackageName = "net.bulletin.pdi.xero.step.get",
        name = "XeroGetStep.Name",
        description = "XeroGetStep.TooltipDesc",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform"
)
public class XeroGetStepMeta extends BaseStepMeta implements StepMetaInterface {

    /**
     * The PKG member is used when looking up internationalized strings.
     * The properties file with localized keys is expected to reside in
     * {the package of the class specified}/messages/messages_{locale}.properties
     */
    private static Class<?> PKG = XeroGetStepMeta.class; // for i18n purposes

    // -----------------------
    // KEYS USED FOR IDENTIFYING CONFIGURATION

    private final static String KEY_URL = "url";

    private final static String KEY_AUTHENTICATIONCONSUMERKEY = "authentication_consumer_key";

    private final static String KEY_AUTHENTICATIONKEYFILE = "authentication_key_file";

    private final static String KEY_WHERE = "where";

    private final static String KEY_XMLFIELDNAME = "xml_field_name";

    private final static String KEY_CONTAINERELEMENTS = "container_elements";

    private final static String KEY_IFMODIFIEDSINCE = "if_modified_since";

    // -----------------------
    // STATE

    private String url;

    private String authenticationConsumerKey;

    private String authenticationKeyFile;

    private String where;

    private String xmlFieldName;

    private String containerElements;

    private String ifModifiedSince;

    public XeroGetStepMeta() {
        super();
    }

    // -----------------------
    // GET'ER AND SET'ER

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUrlIfPresent(String value) {
        if (StringUtils.isNotBlank(value)) {
            setUrl(value);
        }
    }

    public String getAuthenticationConsumerKey() {
        return authenticationConsumerKey;
    }

    public void setAuthenticationConsumerKey(String authenticationConsumerKey) {
        this.authenticationConsumerKey = authenticationConsumerKey;
    }

    public void setAuthenticationConsumerKeyIfPresent(String value) {
        if (StringUtils.isNotBlank(value)) {
            setAuthenticationConsumerKey(value);
        }
    }

    public String getAuthenticationKeyFile() {
        return authenticationKeyFile;
    }

    public void setAuthenticationKeyFile(String authenticationKeyFile) {
        this.authenticationKeyFile = authenticationKeyFile;
    }

    public void setAuthenticationKeyFileIfPresent(String value) {
        if (StringUtils.isNotBlank(value)) {
            setAuthenticationKeyFile(value);
        }
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public void setWhereIfPresent(String value) {
        if (StringUtils.isNotBlank(value)) {
            setWhere(value);
        }
    }

    public String getXmlFieldName() {
        return xmlFieldName;
    }

    public void setXmlFieldName(String xmlFieldName) {
        this.xmlFieldName = StringUtils.trimToNull(xmlFieldName);
    }

    public void setXmlFieldNameIfPresent(String value) {
        if (StringUtils.isNotBlank(value)) {
            setXmlFieldName(value);
        }
    }

    public String getContainerElements() {
        return containerElements;
    }

    public void setContainerElements(String containerElements) {
        this.containerElements = StringUtils.trimToNull(containerElements);
    }

    public void setContainerElementsIfPresent(String value) {
        if (StringUtils.isNotBlank(value)) {
            setContainerElements(value);
        }
    }

    public String getIfModifiedSince() {
        return ifModifiedSince;
    }

    public void setIfModifiedSince(String ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
    }

    public void setIfModifiedSinceIfPresent(String value) {
        if (StringUtils.isNotBlank(value)) {
            setIfModifiedSince(value);
        }
    }

    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
        return new XeroGetStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
    }

    @Override
    public StepDataInterface getStepData() {
        return new XeroGetStepData();
    }

    @Override
    public void setDefault() {
        setUrl("https://api.xero.com/api.xro/2.0/Contacts");
        setXmlFieldName("xeroxml");
        setContainerElements("/");
        setWhere("");
        setContainerElements("/Response/Contacts/Contact");
        setAuthenticationConsumerKey(null);
        setAuthenticationKeyFile(null);
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public String getXML() throws KettleValueException {
        @SuppressWarnings("StringBufferReplaceableByString") StringBuilder result = new StringBuilder();

        result.append(XMLHandler.addTagValue(KEY_URL, getUrl()));
        result.append(XMLHandler.addTagValue(KEY_AUTHENTICATIONCONSUMERKEY, getAuthenticationConsumerKey()));
        result.append(XMLHandler.addTagValue(KEY_AUTHENTICATIONKEYFILE, getAuthenticationKeyFile()));
        result.append(XMLHandler.addTagValue(KEY_XMLFIELDNAME, getXmlFieldName()));
        result.append(XMLHandler.addTagValue(KEY_CONTAINERELEMENTS, getContainerElements()));
        result.append(XMLHandler.addTagValue(KEY_IFMODIFIEDSINCE, getIfModifiedSince()));
        result.append(XMLHandler.addTagValue(KEY_WHERE, getWhere()));

        return result.toString();
    }

    @Override
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
        try {
            setUrlIfPresent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, KEY_URL)));
            setAuthenticationConsumerKeyIfPresent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, KEY_AUTHENTICATIONCONSUMERKEY)));
            setAuthenticationKeyFileIfPresent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, KEY_AUTHENTICATIONKEYFILE)));
            setXmlFieldNameIfPresent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, KEY_XMLFIELDNAME)));
            setContainerElementsIfPresent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, KEY_CONTAINERELEMENTS)));
            setIfModifiedSinceIfPresent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, KEY_IFMODIFIEDSINCE)));
            setWhereIfPresent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, KEY_WHERE)));
        } catch (Exception e) {
            throw new KettleXMLException("unable to read the step's configuration from xml", e);
        }
    }

    @Override
    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException {
        try {
            rep.saveStepAttribute(id_transformation, id_step, KEY_URL, getUrl());
            rep.saveStepAttribute(id_transformation, id_step, KEY_AUTHENTICATIONCONSUMERKEY, getAuthenticationConsumerKey());
            rep.saveStepAttribute(id_transformation, id_step, KEY_AUTHENTICATIONKEYFILE, getAuthenticationKeyFile());
            rep.saveStepAttribute(id_transformation, id_step, KEY_CONTAINERELEMENTS, getContainerElements());
            rep.saveStepAttribute(id_transformation, id_step, KEY_XMLFIELDNAME, getXmlFieldName());
            rep.saveStepAttribute(id_transformation, id_step, KEY_IFMODIFIEDSINCE, getIfModifiedSince());
            rep.saveStepAttribute(id_transformation, id_step, KEY_WHERE, getWhere());
        } catch (Exception e) {
            throw new KettleException("Unable to save step into repository: " + id_step, e);
        }
    }

    @Override
    public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException {
        try {
            setUrlIfPresent(rep.getStepAttributeString(id_step, KEY_URL));
            setAuthenticationConsumerKeyIfPresent(rep.getStepAttributeString(id_step, KEY_AUTHENTICATIONCONSUMERKEY));
            setAuthenticationKeyFileIfPresent(rep.getStepAttributeString(id_step, KEY_AUTHENTICATIONKEYFILE));
            setContainerElementsIfPresent(rep.getStepAttributeString(id_step, KEY_CONTAINERELEMENTS));
            setXmlFieldNameIfPresent(rep.getStepAttributeString(id_step, KEY_XMLFIELDNAME));
            setIfModifiedSinceIfPresent(rep.getStepAttributeString(id_step, KEY_IFMODIFIEDSINCE));
            setWhereIfPresent(rep.getStepAttributeString(id_step, KEY_WHERE));
        } catch (Exception e) {
            throw new KettleException("Unable to load step from repository", e);
        }
    }

    @Override
    public void getFields(
            RowMetaInterface inputRowMeta,
            String name,
            RowMetaInterface[] info,
            StepMeta nextStep,
            VariableSpace space,
            Repository repository,
            IMetaStore metaStore) throws KettleStepException {
        ValueMetaInterface v = new ValueMeta(getXmlFieldName(), ValueMeta.TYPE_STRING);
        v.setTrimType(ValueMeta.TRIM_TYPE_BOTH);
        v.setOrigin(name);
        inputRowMeta.addValueMeta(v);
    }

    @Override
    public void check(
            List<CheckResultInterface> remarks,
            TransMeta transMeta,
            StepMeta stepMeta,
            RowMetaInterface prev,
            String input[],
            String output[],
            RowMetaInterface info,
            VariableSpace space,
            Repository repository,
            IMetaStore metaStore) {

        if (StringUtils.isBlank(url)) {
            remarks.add(new CheckResult(
                    CheckResult.TYPE_RESULT_ERROR,
                    BaseMessages.getString(PKG, "XeroGetStep.CheckResult.URL.Required"),
                    stepMeta));
        }

        if (StringUtils.isBlank(authenticationConsumerKey)) {
            remarks.add(new CheckResult(
                    CheckResult.TYPE_RESULT_ERROR,
                    BaseMessages.getString(PKG, "XeroGetStep.CheckResult.AuthenticationConsumerKey.Required"),
                    stepMeta));
        }

        if (StringUtils.isBlank(authenticationKeyFile)) {
            remarks.add(new CheckResult(
                    CheckResult.TYPE_RESULT_ERROR,
                    BaseMessages.getString(PKG, "XeroGetStep.CheckResult.AuthenticationKey.Required"),
                    stepMeta));
        }

    }


}
