package de.peeeq.wurstscript.jassoptimizer;

import java.io.FileNotFoundException;

/**
 * This will be used to generate unique Strings which aren't named like the ones
 * in the restricted list.
 * Main use for compressing names.
 * 
 * @author Frotty
 */
public class NameGenerator {
    /** The given charmap */
    private String charmap = "wurstqeiopadfghjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM";
    private String TEcharmap = "wurstqeiopadfghjklyxcvbnm";
    /** A counter */
    private int currentId = 0;
    private int TEId = 0;
    /** length of charmap */
    private int length;
    private int TElength = 25;
    
    
    /** 
     * Creates a new NameGenerator
     * @param restricted Forbidden names
     * @param charmap The Charmap to use to generate names
     * @throws FileNotFoundException 
     */
    public NameGenerator(){
        length          = charmap.length();     
    }
    
    /**
     * Get a token
     * @return A (for this Namegenrator) unique token
     */
    public String getToken() {
        int id = currentId++;
        StringBuilder b = new StringBuilder();
        do {
            b.append(charmap.charAt(id % length));
        } while((id /=length) != 0);

        return b.toString();
    }
    
    /**
     * Get a token that is cross checked with the Restricted names
     * @return A checked, unique token
     */
    public String getUniqueToken() {
        String s = getToken();
        if (RestrictedCompressedNames.contains(s)){
        	System.out.println("is restricted");
            // Wishful thinking, but normally this should work
            // there are only a handful of restricted names anyway.
            s = getToken();
        }

        return s;
    }
    
    /**
     * Get a token that can be used for TE and TRVE since
     * its only lowercase.
     * To be unique these start with "z"
     * @return A checked, unique token, lowercase, starting with z
     */
    public String getTEToken() {
        int id = TEId++;
        StringBuilder b = new StringBuilder("z");
        do {
            b.append(TEcharmap.charAt(id % length));
        } while((id /=length) != 0);

        return b.toString();
    }
    

}
