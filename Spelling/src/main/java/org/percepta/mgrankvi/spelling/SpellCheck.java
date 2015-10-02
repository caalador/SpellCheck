package org.percepta.mgrankvi.spelling;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractTextField;
import org.apache.commons.lang.WordUtils;
import org.percepta.mgrankvi.spelling.client.SpellCheckClientRpc;
import org.percepta.mgrankvi.spelling.client.SpellCheckServerRpc;
import org.percepta.mgrankvi.spelling.client.SpellCheckState;
import org.percepta.mgrankvi.spelling.client.Word;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This is the server-side UI component that provides public API 
// for MyComponent
public class SpellCheck extends AbstractExtension {

    private Spelling spelling;

    // To process events from the client, we implement ServerRpc
    private SpellCheckServerRpc rpc = new SpellCheckServerRpc() {

        @Override
        public void checkSpelling(String string) {
            List<Word> words = doSpellCheck(string);

            getRpcProxy(SpellCheckClientRpc.class).corrections(words);
            getRpcProxy(SpellCheckClientRpc.class).setException(!words.isEmpty());
        }

        @Override
        public void checkForErrors(String string) {

            List<String> words = splitWords(string);
            boolean errors = false;
            for (String word : words) {
                if (!spelling.isOk(word)) {
                    errors = true;
                    break;
                }
            }

            getRpcProxy(SpellCheckClientRpc.class).setException(errors);
        }

    };

    private List<Word> doSpellCheck(String string) {
        List<String> words = splitWords(string);

        List<Word> corrections = Lists.newLinkedList();
        for (String word : words) {
            if (!spelling.isOk(word)) {
                Word thing = new Word();
                thing.word = word;
                thing.length = word.length();
                thing.startPosition = string.toLowerCase().indexOf(word);

                LinkedList<String> candidates = spelling.getCandidates(word);
                if (candidates.size() > 0 && !candidates.getFirst().equals(word)) {
                    if(Character.isUpperCase(string.charAt(thing.startPosition))){
                        thing.word = WordUtils.capitalize(thing.word);
                        for(String candidate : candidates) {
                            thing.candidates.add(WordUtils.capitalize(candidate));
                        }
                    } else {
                        thing.candidates = candidates;
                    }

                    corrections.add(thing);
                }
            }
        }

        return corrections;
    }

    private List<String> splitWords(String string) {
        List<String> words = Lists.newLinkedList();

        Pattern p = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher m = p.matcher(string.toLowerCase());

        while (m.find()) {
            words.add(m.group());
        }
        return words;
    }

    /**
     * Use default english dictionary.
     */
    public SpellCheck() {
        this("en");
    }

    public SpellCheck(String dictionary) {
        try {
            spelling = new Spelling(dictionary);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // To receive events from the client, we register ServerRpc
        registerRpc(rpc);
    }

    // We must override getState() to cast the state to MyComponentState
    @Override
    public SpellCheckState getState() {
        return (SpellCheckState) super.getState();
    }

    public void extend(final AbstractTextField component) {
        super.extend(component);

        component.addAttachListener(new AttachListener() {

            @Override
            public void attach(AttachEvent event) {
                if (component.getValue() != null && !component.getValue().isEmpty()) {
                    List<Word> words = doSpellCheck(component.getValue());

                    getRpcProxy(SpellCheckClientRpc.class).setException(!words.isEmpty());
                }
            }
        });

    }

    @Override
    public void remove() {
        super.remove();
        getRpcProxy(SpellCheckClientRpc.class).remove();
    }
}
