package algorithms.searchPattern;

public class BoyerMoore {
    private final int[] badChar = new int[256];
    private final String pattern;

    public BoyerMoore(String pattern) {
        this.pattern = pattern;
        buildBadCharTable();
    }

    private void buildBadCharTable() {
        for (int i = 0; i < 256; i++) {
            badChar[i] = -1;
        }
        for (int i = 0; i < pattern.length(); i++) {
            badChar[pattern.charAt(i)] = i;
        }
    }

    public int search(String text) {
        int m = pattern.length();
        int n = text.length();

        int shift = 0;
        while (shift <= (n - m)) {
            int j = m - 1;

            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }

            if (j < 0) {
                return shift; // padrão encontrado
            } else {
                shift += Math.max(1, j - badChar[text.charAt(shift + j)]);
            }
        }

        return -1; // não encontrado
    }
}
