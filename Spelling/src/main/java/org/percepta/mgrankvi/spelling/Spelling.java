package org.percepta.mgrankvi.spelling;

import com.google.gwt.thirdparty.guava.common.cache.CacheBuilder;
import com.google.gwt.thirdparty.guava.common.cache.CacheLoader;
import com.google.gwt.thirdparty.guava.common.cache.LoadingCache;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spelling {

    private HashMap<String, Integer> nWords;
    private List<Character> characterMap = Lists.newLinkedList();

    final DamerauLevenshteinAlgorithm damerauLevenshtein = new DamerauLevenshteinAlgorithm(2, 2, -1, 2);

    private static LoadingCache<String, HashMap<String, Integer>> wordMaps = CacheBuilder.newBuilder().maximumSize(10).expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, HashMap<String, Integer>>() {
        @Override
        public HashMap<String, Integer> load(String key) throws Exception {
            HashMap<String, Integer> nWords = Maps.newHashMap();
System.out.println(key);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(key + ".dic"), "UTF-8"));
            Pattern p = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);
            for (String temp = ""; temp != null; temp = in.readLine()) {
                Matcher m = p.matcher(temp.toLowerCase());
                while (m.find()) {
                    nWords.put((temp = m.group()), nWords.containsKey(temp) ? nWords.get(temp) + 1 : 1);
                }
            }
            in.close();
            return nWords;
        }
    });

    private static LoadingCache<String, List<Character>> specials = CacheBuilder.newBuilder().maximumSize(10).expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, List<Character>>() {
        @Override
        public List<Character> load(String key) throws Exception {
            List<Character> characters = Lists.newLinkedList();

            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(key + ".char"), "UTF-8"));
            Pattern p = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);
            for (String temp = ""; temp != null; temp = in.readLine()) {
                Matcher m = p.matcher(temp.toLowerCase());
                while (m.find()) {
                    temp = m.group();
                    for (char c : temp.toCharArray()) {
                        characters.add(c);
                    }
                }
            }
            in.close();
            return characters;
        }
    });

    public Spelling(String file) throws IOException {
        try {
            nWords = wordMaps.get(file);
            characterMap = specials.get(file);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private final LinkedList<String> edits(String word) {
        LinkedList<String> result = Lists.newLinkedList();
        for (int i = 0; i < word.length(); ++i) {
            result.add(word.substring(0, i) + word.substring(i + 1));

            for (char c : characterMap) {
                result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i + 1));
            }
        }
        for (int i = 0; i < word.length() - 1; ++i) {
            result.add(word.substring(0, i) + word.substring(i + 1, i + 2) + word.substring(i, i + 1) + word.substring(i + 2));
        }
        for (int i = 0; i <= word.length(); ++i) {
            for (char c : characterMap) {
                result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
            }
        }
        return result;
    }

    public boolean isOk(String word) {
        return nWords.containsKey(word);
    }

    public String correct(String word) {
        if (nWords.containsKey(word)) return word;
        LinkedList<String> list = edits(word);
        HashMap<Integer, String> candidates = new HashMap<Integer, String>();
        for (String s : list) {
            if (nWords.containsKey(s)) {
                candidates.put(nWords.get(s), s);
            }
        }
        if (candidates.size() > 0) {
            return candidates.get(Collections.max(candidates.keySet()));
        }
        for (String s : list) {
            for (String w : edits(s)) {
                if (nWords.containsKey(w)) {
                    candidates.put(nWords.get(w), w);
                }
            }
        }
        return candidates.size() > 0 ? candidates.get(Collections.max(candidates.keySet())) : word;
    }

    public LinkedList<String> getCandidates(final String word) {
        LinkedList<String> result = Lists.newLinkedList();

        if (nWords.containsKey(word)) {
            result.add(word);
            return result;
        }

        LinkedList<String> list = edits(word);
        for (String s : list) {
            if (nWords.containsKey(s) && !result.contains(s)) {
                result.add(s);
            }
        }
        if (!result.isEmpty()) {
            sortResult(word, result);

            return result;
        }
        for (String s : list) {
            for (String w : edits(s)) {
                if (nWords.containsKey(w) && !result.contains(w)) {
                    result.add(w);
                }
            }
        }

        if (result.isEmpty()) result.add(word);
        else {
            sortResult(word, result);
        }

        return result;
    }

    private void sortResult(final String word, LinkedList<String> result) {
        Collections.sort(result, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int one = damerauLevenshtein.execute(word, o1);
                int two = damerauLevenshtein.execute(word, o2);

                return one > two ? -1 : one == two ? 0 : 1;
            }
        });
    }
}