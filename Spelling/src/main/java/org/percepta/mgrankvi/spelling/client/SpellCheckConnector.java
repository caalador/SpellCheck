package org.percepta.mgrankvi.spelling.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.VConsole;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VTextField;
import com.vaadin.shared.ui.Connect;
import org.percepta.mgrankvi.spelling.SpellCheck;

import java.util.List;

// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(SpellCheck.class)
public class SpellCheckConnector extends AbstractExtensionConnector {

    // ServerRpc is used to send events to server. Communication implementation
    // is automatically created here
    SpellCheckServerRpc rpc = RpcProxy.create(SpellCheckServerRpc.class, this);

    PopupPanel popupPanel;
    CellList<String> list;
    Word currentWord;

    ComponentConnector connection;
    boolean scheduled = false;

    //
    public SpellCheckConnector() {
        // To receive RPC events from server, we register ClientRpc implementation
        registerRpc(SpellCheckClientRpc.class, new SpellCheckClientRpc() {
            @Override
            public void corrections(List<Word> corrections) {
//                VConsole.log("Got map;" + corrections);
                if (popupPanel != null && popupPanel.isVisible()) {
                    popupPanel.hide();
                }

                if (corrections.isEmpty()) {
                    return;
                }

                final Widget extendedWidget = connection.getWidget();

                popupPanel = new PopupPanel(true);
                popupPanel.setWidth(extendedWidget.getOffsetWidth() + "px");

                final ScrollPanel panel = new ScrollPanel();
                panel.setHeight("90px");


                currentWord = corrections.iterator().next();
                List<String> values = currentWord.candidates;

                list = new CellList<String>(new Cell(), keyProvider);
                list.setPageSize(values.size());
                list.setRowCount(values.size(), true);
                list.setRowData(0, values);
                list.setVisibleRange(0, values.size());
                list.setWidth("100%");
                list.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);


                final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>(keyProvider);
                list.setSelectionModel(selectionModel);

                selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                    public void onSelectionChange(SelectionChangeEvent event) {
                        popupPanel.hide();

                        String text = ((TextBoxBase) extendedWidget).getText();
                        String replaced = text.replace(currentWord.word, selectionModel.getSelectedObject());
                        ((TextBoxBase) extendedWidget).setText(replaced);
                        ((VTextField) extendedWidget).valueChange(false);
                        rpc.checkSpelling(replaced);
                    }
                });
                ((TextBoxBase) extendedWidget).setSelectionRange(currentWord.startPosition, currentWord.length);


                panel.setWidget(list);
                popupPanel.setWidget(panel);
                popupPanel.setPopupPosition(extendedWidget.getAbsoluteLeft(), extendedWidget.getAbsoluteTop() + extendedWidget.getOffsetHeight());
                popupPanel.show();
            }

            @Override
            public void setException(boolean exception) {
                if (exception)
                    connection.getWidget().addStyleName("exception");
                else
                    connection.getWidget().removeStyleName("exception");
            }
        });

    }

    private class Cell extends AbstractCell<String> {

        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            sb.appendEscaped(value);
        }
    }

    ProvidesKey<String> keyProvider = new ProvidesKey<String>() {
        public Object getKey(String item) {
            // Always do a null check.
            return (item == null) ? null : item.hashCode();
        }
    };


    private Timer textChangeEventTrigger = new Timer() {

        @Override
        public void run() {
            if (connection.getWidget().isAttached()) {
                String text = ((TextBoxBase) connection.getWidget()).getText();
                VConsole.log(" === " + text);
                rpc.checkForErrors(text);
            }
            scheduled = false;
        }
    };


    @Override
    protected void extend(ServerConnector target) {
        connection = (ComponentConnector) target;

        connection.getWidget().addDomHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (scheduled) {
                    textChangeEventTrigger.cancel();
                    scheduled = false;
                }

                if (event.isControlKeyDown() && event.isDownArrow()) {
                    rpc.checkSpelling(((TextBoxBase) connection.getWidget()).getText());
                    return;
                } else if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE && popupPanel != null && popupPanel.isVisible()) {
                    popupPanel.hide();
                }


                if ((Character.toString((char) event.getNativeKeyCode())).isEmpty() || event.isDownArrow() || event.isLeftArrow() || event.isRightArrow() || event.isUpArrow() ||
                        event.getNativeKeyCode() == KeyCodes.KEY_ENTER || event.getNativeKeyCode() == KeyCodes.KEY_SPACE || event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
                    return;
                }
                textChangeEventTrigger.schedule(250);
                scheduled = true;
            }
        }, KeyUpEvent.getType());
    }

    @Override
    public SpellCheckState getState() {
        return (SpellCheckState) super.getState();
    }

    // Whenever the state changes in the server-side, this method is called
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
//        if (getState().corrections != null)
//            Window.alert(getState().corrections.values().toString());

//        ((ComponentConnector)connection).getWidget()

    }

}
