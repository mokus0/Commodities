package net.deepbondi.minecraft.market;

import java.util.ArrayList;
import java.util.List;

public final class Util {
    private Util() {}
    
    public static String capitalize(final String word) {
        if (word.length() > 0) {
            final char[] letters = word.toCharArray();
            
            if (Character.isTitleCase(letters[0])) return word;
            else letters[0] = Character.toTitleCase(letters[0]);
            
            return new String(letters);
        } else {
            return word;
        }
    }
    
    public static String camelCase(final String[] parts, final boolean upcaseFirst) {
        final StringBuilder out = new StringBuilder();
        boolean first = true;
        
        for (final String part : parts) {
            final String lcPart = part.toLowerCase();
            
            if (first && !upcaseFirst) {
                out.append(lcPart);
            } else {
                out.append(capitalize(lcPart));
            }
            
            first = false;
        }
        
        return out.toString();
    }
    
    public static String underscoredToCamelCase(final String underscored, final boolean upcaseFirst) {
        return camelCase(underscored.split("_"), upcaseFirst);
    }
    
    // shamelessly cribbed from http://stackoverflow.com/questions/2559759
    // and modified to split the output instead of inserting spaces.
    public static String[] splitCamelCase(String s) {
       return s.split(
          String.format("%s|%s|%s",
             "(?<=[A-Z])(?=[A-Z][a-z])",
             "(?<=[^A-Z])(?=[A-Z])",
             "(?<=[A-Za-z])(?=[^A-Za-z])"
          ));
    }
}

