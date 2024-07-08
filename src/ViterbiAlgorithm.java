public class ViterbiAlgorithm {

    private double[][] prob;
    private int[][] prev;

    public int[] decode(int[] encodedData) {
        int T = encodedData.length / 3; // Koder 1/3 (3 bity na 1 input bit)
        int S = 8; // 2^3 możliwych stanów dla 3-bitowego rejestru

        prob = new double[T][S];    // Inicjalizacja tablicy prawdopodobieństw;
        // Zawiera największe znalezione logarytmiczne prawdopodobieństwo dotarcia do stanu s w czasie t.
        prev = new int[T][S];       // Inicjalizacja tablicy poprzednich stanów

        // Inicjalizacja na podstawie danych wejściowych
        for (int s = 0; s < S; s++) {
            prob[0][s] = emissionProb(s, encodedData, 0);
        }

        // Główna pętla Viterbiego: iteracja przez wszystkie kroki czasowe i stany
        for (int t = 1; t < T; t++) {
            for (int s = 0; s < S; s++) {
                double maxProb = Double.NEGATIVE_INFINITY;
                int maxState = -1;
                for (int r = 0; r < S; r++) {
                    // Obliczenie nowego prawdopodobieństwa dla przejścia ze stanu r do s
                    double newProb = prob[t - 1][r] + transitionProb(r, s) + emissionProb(s, encodedData, t);
                    if (newProb > maxProb) {
                        // Aktualizacja danych
                        maxProb = newProb;
                        maxState = r;
                    }
                }
                prob[t][s] = maxProb; // Przypisanie maksymalnego prawdopodobieństwa do tablicy
                prev[t][s] = maxState; // Przypisanie najlepszego stanu do tablicy
            }
        }

        // Śledzenie wstecz w celu znalezienia najbardziej prawdopodobnej ścieżki stanów
        int[] path = new int[T];
        double maxFinalProb = Double.NEGATIVE_INFINITY;
        for (int s = 0; s < S; s++) {
            if (prob[T - 1][s] > maxFinalProb) {
                maxFinalProb = prob[T - 1][s]; // Aktualizacja maksymalnego prawdopodobieństwa końcowego
                path[T - 1] = s; // Przypisanie najlepszego stanu końcowego
            }
        }

        // Śledzenie wstecz w celu odbudowania ścieżki stanów
        for (int t = T - 2; t >= 0; t--) {
            path[t] = prev[t + 1][path[t + 1]];
        }

        // Konwersja stanów do bitów wejściowych
        int[] decodedBits = new int[T];
        for (int t = 0; t < T; t++) {
            decodedBits[t] = (path[t] >> 2) & 1; // przesunięcie bitów stanu o 2 miejsca w prawo
            // i pozostawienia najmłodszego bitu
        }

        return decodedBits;
    }

    // Obliczanie logarytmicznego prawdopodobieństwa przejścia między stanami
    private double transitionProb(int fromState, int toState) {
        return 0; // log(1) = 0, przejścia są równoprawdopodobne
    }

    // Obliczanie logarytmicznego prawdopodobieństwa, że dany stan mógł wygenerować zakodowane dane w danym kroku czasowym
    private double emissionProb(int state, int[] encodedData, int t) {
        int y1 = (state >> 2) & 1; //  Przesuwa stan o 2 bity w prawo i wykonuje operację AND z 1 - bit wejściowy
        int y2 = (state >> 1) & 1; // Przesuwa stan o 1 bit w prawo i wykonuje operację AND - bit z rejestru 1
        int y3 = state & 1; //Wykonuje operację AND z 1 - bit z rejestru 2

        // Oczekiwane bity zakodowane dla kroku czasowego t
        int expectedBit1 = encodedData[3 * t];
        int expectedBit2 = encodedData[3 * t + 1];
        int expectedBit3 = encodedData[3 * t + 2];

        int matches = 0; // Liczba zgodnych bitów
        if (y1 == expectedBit1) matches++;
        if (y2 == expectedBit2) matches++;
        if (y3 == expectedBit3) matches++;

        return Math.log(matches + 1); // Dodajemy 1 do logarytmu zgodnych bitów, aby uniknąć log(0)
    }
}
