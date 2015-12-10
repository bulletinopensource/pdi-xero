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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

/**
 * <p>This provides the user interface that pops up when the user double-clicks on the
 * step.  It was based off a sample dialog that looked like it was machine-generated.
 * </p>
 *
 * @author Andrew Lindesay
 */
@SuppressWarnings("unused")
public class XeroGetStepDialog extends BaseStepDialog implements StepDialogInterface {

    /**
     * The PKG member is used when looking up internationalized strings.
     * The properties file with localized keys is expected to reside in
     * {the package of the class specified}/messages/messages_{locale}.properties
     */
    private static Class<?> PKG = XeroGetStepMeta.class; // for i18n purposes

    // this is the object the stores the step's settings
    // the dialog reads the settings from it when opening
    // the dialog writes the settings to it when confirmed
    private XeroGetStepMeta meta;

    private TextVar wUrl;
    private TextVar wAuthenticationConsumerKey;
    private TextVar wAuthenticationKeyFile;
    private Text wContainerElements;
    private TextVar wIfModifiedSince;
    private Text wXmlFieldName;
    private TextVar wWhere;

    public XeroGetStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        meta = (XeroGetStepMeta) in;
    }

    private Label createStandardLabel(Composite composite, String key, FormData formData) {
        Label label = new Label(composite, SWT.RIGHT);
        label.setText(BaseMessages.getString(PKG, key));
        props.setLook(label);
        label.setLayoutData(formData);
        return label;
    }

    private Text createStandardText(Composite composite, String initialValue, FormData formData, ModifyListener modifyListener) {
        Text result = new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        result.setText(initialValue);
        props.setLook(result);
        result.addModifyListener(modifyListener);
        result.setLayoutData(formData);
        return result;
    }

    private TextVar createStandardTextVar(Composite composite, String initialValue, FormData formData, ModifyListener modifyListener) {
        TextVar result = new TextVar(transMeta, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        result.setText(initialValue);
        props.setLook(result);
        result.addModifyListener(modifyListener);
        result.setLayoutData(formData);
        return result;
    }

    private FormData createStandardLabelFormData(Control lastControl) {
        FormData ds = new FormData();
        ds.left = new FormAttachment(0, 0);
        ds.right = new FormAttachment(props.getMiddlePct(), -Const.MARGIN);

        if (null == lastControl) {
            ds.top = new FormAttachment(0, Const.MARGIN);
        } else {
            ds.top = new FormAttachment(lastControl, Const.MARGIN);
        }

        return ds;
    }

    private FormData createStandardControlFormData(Control lastControl) {
        FormData ds = new FormData();
        ds.left = new FormAttachment(props.getMiddlePct(), 0);
        ds.right = new FormAttachment(100, 0);

        if (null == lastControl) {
            ds.top = new FormAttachment(0, Const.MARGIN);
        } else {
            ds.top = new FormAttachment(lastControl, Const.MARGIN);
        }

        return ds;
    }

    public String open() {

        // store some convenient SWT variables
        Shell parent = getParent();
        Display display = parent.getDisplay();

        // SWT code for preparing the dialog
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, meta);

        // Save the value of the changed flag on the meta object. If the user cancels
        // the dialog, it will be restored to this saved value.
        // The "changed" variable is inherited from BaseStepDialog
        changed = meta.hasChanged();

        // The ModifyListener used on all controls. It will update the meta object to
        // indicate that changes are being made.
        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                meta.setChanged();
            }
        };

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog        //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "XeroGetStep.Shell.Title"));

        int margin = Const.MARGIN;

        Control lastControl = null;

        {
            fdlStepname = createStandardLabelFormData(lastControl); // not sure why it keeps this?
            wlStepname = createStandardLabel(shell, "System.Label.StepName", fdlStepname);
            wStepname = createStandardText(shell, stepname, createStandardControlFormData(lastControl), lsMod);
            lastControl = wStepname;
        }

        {
            createStandardLabel(shell, "XeroGetStep.XmlFieldName.Title", createStandardLabelFormData(lastControl));
            wXmlFieldName = createStandardText(shell, "", createStandardControlFormData(lastControl), lsMod);
            lastControl = wXmlFieldName;
        }

        {
            createStandardLabel(shell, "XeroGetStep.URL.Title", createStandardLabelFormData(lastControl));
            wUrl = createStandardTextVar(shell, "", createStandardControlFormData(lastControl), lsMod);
            lastControl = wUrl;
        }

        {
            createStandardLabel(shell, "XeroGetStep.AuthenticationConsumerKey.Title", createStandardLabelFormData(lastControl));
            wAuthenticationConsumerKey = createStandardTextVar(shell, "", createStandardControlFormData(lastControl), lsMod);
            lastControl = wAuthenticationConsumerKey;
        }

        {
            createStandardLabel(shell, "XeroGetStep.AuthenticationKeyFile.Title", createStandardLabelFormData(lastControl));
            wAuthenticationKeyFile = createStandardTextVar(shell, "", createStandardControlFormData(lastControl), lsMod);
            lastControl = wAuthenticationKeyFile;
        }

        {
            createStandardLabel(shell, "XeroGetStep.ContainerElements.Title", createStandardLabelFormData(lastControl));
            wContainerElements = createStandardText(shell, "", createStandardControlFormData(lastControl), lsMod);
            lastControl = wContainerElements;
        }

        {
            createStandardLabel(shell, "XeroGetStep.IfModifiedSince.Title", createStandardLabelFormData(lastControl));
            wIfModifiedSince = createStandardTextVar(shell, "", createStandardControlFormData(lastControl), lsMod);
            lastControl = wIfModifiedSince;
        }

        {
            createStandardLabel(shell, "XeroGetStep.Where.Title", createStandardLabelFormData(lastControl));
            wWhere = createStandardTextVar(shell, "", createStandardControlFormData(lastControl), lsMod);
            lastControl = wWhere;
        }

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[]{wOK, wCancel}, margin, lastControl);

        // Add listeners for cancel and OK
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        // default listener (for hitting "enter")
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);
        wUrl.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // Set/Restore the dialog size based on last position on screen
        // The setSize() method is inherited from BaseStepDialog
        setSize();

        // populate the dialog with the values from the meta object
        populateDialog();

        // restore the changed flag to original value, as the modify listeners fire during dialog population
        meta.setChanged(changed);

        // open dialog and enter event loop
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        // at this point the dialog has closed, so either ok() or cancel() have been executed
        // The "stepname" variable is inherited from BaseStepDialog
        return stepname;
    }

    private void populateDialog() {
        wStepname.selectAll();
        wUrl.setText(StringUtils.trimToEmpty(meta.getUrl()));
        wAuthenticationConsumerKey.setText(StringUtils.trimToEmpty(meta.getAuthenticationConsumerKey()));
        wAuthenticationKeyFile.setText(StringUtils.trimToEmpty(meta.getAuthenticationKeyFile()));
        wContainerElements.setText(StringUtils.trimToEmpty(meta.getContainerElements()));
        wIfModifiedSince.setText(StringUtils.trimToEmpty(meta.getIfModifiedSince()));
        wXmlFieldName.setText(StringUtils.trimToEmpty(meta.getXmlFieldName()));
        wWhere.setText(StringUtils.trimToEmpty(meta.getWhere()));
    }

    private void cancel() {
        stepname = null;
        meta.setChanged(changed);
        dispose();
    }

    private void ok() {
        stepname = wStepname.getText();
        meta.setUrl(wUrl.getText());
        meta.setAuthenticationConsumerKey(wAuthenticationConsumerKey.getText());
        meta.setAuthenticationKeyFile(wAuthenticationKeyFile.getText());
        meta.setContainerElements(wContainerElements.getText());
        meta.setIfModifiedSince(wIfModifiedSince.getText());
        meta.setXmlFieldName(wXmlFieldName.getText());
        meta.setWhere(wWhere.getText());
        dispose();
    }
}
