package net.deepbondi.minecraft.market;

public final class Util {
    private Util() {
    }

    private static String capitalize(final String word) {
        if (!word.isEmpty()) {
            final char[] letters = word.toCharArray();

            if (Character.isTitleCase(letters[0])) return word;
            else letters[0] = Character.toTitleCase(letters[0]);

            return new String(letters);
        } else {
            return word;
        }
    }

    private static String camelCase(final String[] parts, final boolean uppercaseFirst) {
        final StringBuilder out = new StringBuilder();
        boolean first = true;

        for (final String part : parts) {
            final String lcPart = part.toLowerCase();

            if (first && !uppercaseFirst) {
                out.append(lcPart);
            } else {
                out.append(capitalize(lcPart));
            }

            first = false;
        }

        return out.toString();
    }

    // shamelessly cribbed from http://stackoverflow.com/questions/2559759
    // and modified to split the output instead of inserting spaces.
    public static String[] splitCamelCase(final String s) {
        return s.split(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ));
    }
}

