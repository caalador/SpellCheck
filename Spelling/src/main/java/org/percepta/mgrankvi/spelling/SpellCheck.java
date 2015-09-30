package org.percepta.mgrankvi.spelling;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractTextField;
import org.percepta.mgrankvi.spelling.client.SpellCheckClientRpc;
import org.percepta.mgrankvi.spelling.client.SpellCheckServerRpc;
import org.percepta.mgrankvi.spelling.client.SpellCheckState;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This is the server-side UI component that provides public API 
// for MyComponent
public class SpellCheck extends AbstractExtension {

    private Spelling spelling;
//    private LoadingCache<String, String> spellingCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, String>() {
//
//        @Override
//        public String load(String key) throws Exception {
//            return spelling.correct(key);
//        }
//    });

    // To process events from the client, we implement ServerRpc
    private SpellCheckServerRpc rpc = new SpellCheckServerRpc() {

        @Override
        public void checkSpelling(String string) {
            doSpellCheck(string);
        }

    };

    private void doSpellCheck(String string) {
        List<String> words = Lists.newLinkedList();

        Pattern p = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher m = p.matcher(string.toLowerCase());

        while (m.find()) {
            words.add(m.group());
        }

        HashMap<String, String> corrections = Maps.newHashMap();
        for (String word : words) {
            if (!spelling.isOk(word)) {
                System.out.println("Word not ok: " + word);
                corrections.put(word, spelling.correct(word));
//                try {
//                    corrections.put(word, spellingCache.get(word));
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
            }
        }

        if (!corrections.isEmpty()) {
            getRpcProxy(SpellCheckClientRpc.class)
                    .corrections(corrections);
        }
        System.out.println(corrections.values());
    }

    public SpellCheck() {
        this("en.dic");
    }

    public SpellCheck(String dictionary) {
        this(dictionary, false);
    }

    public SpellCheck(String dictionary, boolean scandics) {
        try {
            spelling = new Spelling(dictionary, scandics);
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
                    doSpellCheck(component.getValue());
                }
            }
        });

//        component.addTextChangeListener(new FieldEvents.TextChangeListener() {
//
//            @Override
//            public void textChange(FieldEvents.TextChangeEvent event) {
//                if (event.getText() != null && !event.getText().isEmpty()) {
//                    doSpellCheck(event.getText());
//                } else {
//                    System.out.println("All ok.");
//                }
//            }
//        });
    }
}
