package org.percepta.mgrankvi.spelling.client;

import java.io.Serializable;
import java.util.List;

/**
 * @author Mikael Grankvist - Vaadin }>
 */
public class Word implements Serializable {

    public String word;
    public int startPosition;
    public int length;
    public List<String> candidates;
}
