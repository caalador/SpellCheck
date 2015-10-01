package org.percepta.mgrankvi.spelling.client;

import com.vaadin.shared.communication.ClientRpc;

import java.util.List;
import java.util.Map;

// ClientRpc is used to pass events from server to client
// For sending information about the changes to component state, use State instead
public interface SpellCheckClientRpc extends ClientRpc {

	void corrections(List<Word> corrections);

	void setException(boolean exception);
}