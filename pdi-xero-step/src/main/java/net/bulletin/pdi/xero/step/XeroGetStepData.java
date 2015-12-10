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

import net.oauth.OAuthMessage;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import java.util.Stack;

/**
 * <p>This class holds state for the step while it executes.</p>
 *
 * @author Andrew Lindesay
 */

public class XeroGetStepData extends BaseStepData implements StepDataInterface {

    private RowMetaInterface outputRowMeta;

    private OAuthMessage oAuthMessage = null;

    public XeroGetStepData() {
        super();
    }

    public void setOutputRowMeta(RowMetaInterface outputRowMeta) {
        this.outputRowMeta = outputRowMeta;
    }

    public RowMetaInterface getOutputRowMeta() {
        return outputRowMeta;
    }

    public void setOAuthMessage(OAuthMessage oAuthMessage) {
        this.oAuthMessage = oAuthMessage;
    }

    public OAuthMessage getOAuthMessage() {
        return oAuthMessage;
    }

}

