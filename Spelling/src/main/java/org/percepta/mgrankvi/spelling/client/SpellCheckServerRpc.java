package org.percepta.mgrankvi.spelling.client;

import com.vaadin.shared.communication.ServerRpc;

// ServerRpc is used to pass events from client to server
public interface SpellCheckServerRpc extends ServerRpc {

    void checkSpelling(String string);

    void checkForErrors(String string);
}
