package org.percepta.mgrankvi.spelling;

import junit.framework.Assert;
import org.apache.commons.lang.WordUtils;
import org.junit.Test;

import java.util.LinkedList;

// JUnit tests here
public class MyComponentTest {

    @Test
    public void testSpelling() throws Exception {
        Spelling spelling = new Spelling("en");
        long start = System.currentTimeMillis();
        String corrected = spelling.correct("zuchini");
        System.out.println("Spelling Time zuchini: " + (System.currentTimeMillis() - start));
        Assert.assertEquals("zucchini", corrected);
    }

    @Test
    public void testSpellingEdits() throws Exception {
        Spelling spelling = new Spelling("en");
        String correction = spelling.correct("wallerst");
        long start = System.currentTimeMillis();
        LinkedList<String> candidates = spelling.getCandidates("wallerst");
        System.out.println("Spelling Edits Time: " + (System.currentTimeMillis() - start));
        Assert.assertEquals("Got wrong candidates.", "[wallet, tallest, waller, callers, walkers, walters, wallets, wailers]", candidates.toString());
        Assert.assertEquals("Candidates have the wrong sort order", correction, candidates.getFirst());
    }

    @Test
    public void testSpellingInSwedish() throws Exception {
        Spelling spelling = new Spelling("sv");

        long start = System.currentTimeMillis();

        LinkedList<String> candidates = spelling.getCandidates("over");
        System.out.println("Spelling Time Swedish: " + (System.currentTimeMillis() - start));
        String corrected = spelling.correct("öevr");
        System.out.println(candidates.toString());
        Assert.assertEquals("över", corrected);

    }
//

    @Test
    public void testSpellingInSwedishWithCapitals() throws Exception {
        Spelling spelling = new Spelling("sv");

        long start = System.currentTimeMillis();

        LinkedList<String> candidates = spelling.getCandidates("Over");
        System.out.println("Spelling Time Swedish: " + (System.currentTimeMillis() - start));
        String corrected = spelling.correct("Öevr");
        System.out.println(candidates.toString());
        Assert.assertEquals("Över", corrected);


    }

}
