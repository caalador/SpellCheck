package org.percepta.mgrankvi.spelling;

import junit.framework.Assert;
import org.junit.Test;

import java.util.LinkedList;

// JUnit tests here
public class MyComponentTest {

    @Test
    public void testSpelling() throws Exception {
        Spelling spelling = new Spelling("en.dic");
        long start = System.currentTimeMillis();
        String corrected = spelling.correct("zuchini");
        System.out.println("Spelling Time: " + (System.currentTimeMillis() - start));
        Assert.assertEquals("zucchini", corrected);
    }

    @Test
    public void testSpellingEdits() throws Exception {
        Spelling spelling = new Spelling("en.dic");
        String correction = spelling.correct("wallerst");
        long start = System.currentTimeMillis();
        LinkedList<String> candidates = spelling.getCandidates("wallerst");
        System.out.println("Spelling Edits Time: " + (System.currentTimeMillis() - start));
        Assert.assertEquals("Got wrong candidates.", "[wallet, waller, callers, tallest, wailers, walkers, walters, wallets]", candidates.toString());
        Assert.assertEquals("Candidates have the wrong sort order", correction, candidates.getFirst());
    }

    @Test
    public void testSpellingInSwedish() throws Exception {
        Spelling spelling = new Spelling("sv.dic", true);

        long start = System.currentTimeMillis();

        LinkedList<String> candidates = spelling.getCandidates("över");
        System.out.println("Spelling Time Swedish: " + (System.currentTimeMillis() - start));
        String corrected;
        System.out.println("  " + (corrected = spelling.correct("över")));
        System.out.println(candidates.toString());
        Assert.assertEquals("över", corrected);


    }

//    @Test
//    public void testJazzy() throws IOException {
//        SpellChecker checker = new SpellChecker(new SpellDictionaryHashMap(new InputStreamReader(Spelling.class.getClassLoader().getResourceAsStream("en.dic")), new InputStreamReader(Spelling.class.getClassLoader().getResourceAsStream("en_phonet.dat"))));
//
//        long start = System.currentTimeMillis();
//        System.out.println(checker.getSuggestions("wallerst", 150000000));
//        System.out.println("Spelling jazzy Time: " + (System.currentTimeMillis()-start));
//    }
}
