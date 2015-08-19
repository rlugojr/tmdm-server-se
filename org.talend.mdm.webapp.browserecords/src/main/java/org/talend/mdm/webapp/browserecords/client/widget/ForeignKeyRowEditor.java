package org.talend.mdm.webapp.browserecords.client.widget;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.ServiceFactory;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.Locale;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.google.gwt.user.client.Timer;

public class ForeignKeyRowEditor extends RowEditor<ItemNodeModel> {

    TypeModel fkTypeModel;

    boolean staging;

    ItemNodeModel currentItemNodeModel;

    public ForeignKeyRowEditor(TypeModel fkTypeModel, boolean staging) {
        this.fkTypeModel = fkTypeModel;
        this.staging = staging;
    }

    // cancel click Editor
    @Override
    protected void onRowClick(GridEvent<ItemNodeModel> e) {
    }

    @Override
    public void startEditing(int rowIndex, boolean doFocus) {
        super.startEditing(rowIndex, doFocus);
        currentItemNodeModel = grid.getSelectionModel().getSelectedItem().clone(true);
        grid.getSelectionModel().setLocked(true);

    }

    @Override
    public void stopEditing(boolean saveChanges) {
        super.stopEditing(saveChanges);
        grid.getSelectionModel().setLocked(false);
        if (saveChanges) {
            ForeignKeyBean currentForeignKeyBean = (ForeignKeyBean) currentItemNodeModel.getObjectValue();
            ForeignKeyBean selectedForeignKeyBean = (ForeignKeyBean) grid.getSelectionModel().getSelectedItem().getObjectValue();
            if (!currentForeignKeyBean.getId().equals(selectedForeignKeyBean.getId())
                    || !currentForeignKeyBean.toString().equals(selectedForeignKeyBean.toString())) {
                final ItemNodeModel node = grid.getSelectionModel().getSelectedItem();
                ForeignKeyBean fkBean = (ForeignKeyBean) node.getObjectValue();
                if (fkBean == null) {
                    MessageBox.alert(MessagesFactory.getMessages().message_fail(), MessagesFactory.getMessages()
                            .fk_edit_failure(), null);
                    return;
                }
                String ids = fkBean.getId().replaceAll("\\[", "").replaceAll("\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
                ServiceFactory
                        .getInstance()
                        .getService(staging)
                        .updateItem(
                                fkTypeModel.getForeignkey().split("/")[0], ids, fkBean.getForeignKeyInfo(), null, null, Locale.getLanguage(), //$NON-NLS-1$
                                new SessionAwareAsyncCallback<ItemResult>() {

                                    @Override
                                    protected void doOnFailure(Throwable caught) {
                                        Record record;
                                        Store<ItemNodeModel> store = grid.getStore();
                                        if (store != null) {
                                            record = store.getRecord(node);
                                        } else {
                                            record = null;
                                        }

                                        if (record != null) {
                                            record.reject(false);
                                        }

                                        String err = caught.getLocalizedMessage();
                                        if (err != null) {
                                            MessageBox.alert(MessagesFactory.getMessages().error_title(),
                                                    MultilanguageMessageParser.pickOutISOMessage(err), null);
                                        } else {
                                            super.doOnFailure(caught);
                                        }
                                    }

                                    @Override
                                    public void onSuccess(ItemResult result) {
                                        Record record;
                                        Store<ItemNodeModel> store = grid.getStore();
                                        if (store != null) {
                                            record = store.getRecord(node);
                                        } else {
                                            record = null;
                                        }

                                        if (record != null) {
                                            record.commit(false);
                                        }

                                        final MessageBox msgBox = new MessageBox();
                                        String msg = MultilanguageMessageParser.pickOutISOMessage(result.getDescription());
                                        if (result.getStatus() == ItemResult.FAILURE) {
                                            msgBox.setTitle(MessagesFactory.getMessages().error_title());
                                            msgBox.setButtons(MessageBox.OK);
                                            msgBox.setIcon(MessageBox.ERROR);
                                            msgBox.setMessage(msg == null || msg.isEmpty() ? MessagesFactory.getMessages()
                                                    .output_report_null() : msg);
                                            msgBox.show();
                                        } else {
                                            msgBox.setTitle(MessagesFactory.getMessages().info_title());
                                            msgBox.setButtons(""); //$NON-NLS-1$
                                            msgBox.setIcon(MessageBox.INFO);
                                            msgBox.setMessage(msg == null || msg.isEmpty() ? MessagesFactory.getMessages()
                                                    .save_success() : msg);
                                            msgBox.show();
                                            Timer timer = new Timer() {

                                                @Override
                                                public void run() {
                                                    msgBox.close();
                                                }
                                            };
                                            timer.schedule(1000);
                                        }
                                    }
                                });
            } else {
                Record record;
                Store<ItemNodeModel> store = grid.getStore();
                if (store != null) {
                    record = store.getRecord(grid.getSelectionModel().getSelectedItem());
                } else {
                    record = null;
                }

                if (record != null) {
                    record.reject(false);
                }
                MessageBox.info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages().no_change_info(), null);
            }
        }
    }

}
