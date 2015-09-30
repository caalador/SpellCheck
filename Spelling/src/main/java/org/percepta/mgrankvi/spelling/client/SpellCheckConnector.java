package org.percepta.mgrankvi.spelling.client;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.VConsole;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import org.percepta.mgrankvi.spelling.SpellCheck;

import java.util.Map;

// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(SpellCheck.class)
public class SpellCheckConnector extends AbstractExtensionConnector {

    // ServerRpc is used to send events to server. Communication implementation
    // is automatically created here
    SpellCheckServerRpc rpc = RpcProxy.create(SpellCheckServerRpc.class, this);
//
    public SpellCheckConnector() {
        // To receive RPC events from server, we register ClientRpc implementation
		registerRpc(SpellCheckClientRpc.class, new SpellCheckClientRpc() {
            @Override
            public void corrections(Map<String, String> corrections) {
                VConsole.log("Got map;" + corrections.values());
            }
        });

    }

    ComponentConnector connection;

    private Timer textChangeEventTrigger = new Timer() {

        @Override
        public void run() {
            if(connection.getWidget().isAttached()) {
                String text = ((TextBoxBase) connection.getWidget()).getText();
                VConsole.log(" === " + text);
                rpc.checkSpelling(text);
            }
            scheduled = false;
        }
    };

    boolean scheduled = false;
    @Override
    protected void extend(ServerConnector target) {
        connection = (ComponentConnector)target;
        connection.getWidget().addDomHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if(scheduled){
                    textChangeEventTrigger.cancel();
                }
                textChangeEventTrigger.schedule(200);
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
