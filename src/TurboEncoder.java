import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class TurboEncoder {

    public static int[] register1 = new int[3];
    public static int[] register2 = new int[3];
    public static int[] interleaver = new int[2];

    public static void main(String[] args) {
        try {
            // Czytanie danych wejściowych z pliku
            int[] input = readInputData("inputData.txt");

            // Kodowanie danych w turbo koderze i zapisanie do pliku (bez zakłóceń)
            int[] encodedData = turboEncoder(input);
            saveEncodedData(encodedData, "encodedData.txt");

            // Generowanie danych z określonym prawdopodobieństwem błędu (zakłóceń)
            int[] encodedDataError = errorGenerator(encodedData, 0.001);
            saveEncodedData(encodedDataError, "encodedDataError.txt");

            // Dekodowanie za pomocą algorytmu Viterbiego
            ViterbiAlgorithm viterbiAlgorithm = new ViterbiAlgorithm();

            // Dekodowanie ciągu bez zakłóceń
//            int[] decodedData = viterbiAlgorithm.decode(encodedData);
//            saveEncodedData(decodedData, "decodedData.txt");

            // Dekodowanie ciągu z zakłóceniami
            int[] decodedDataError = viterbiAlgorithm.decode(encodedDataError);
            saveEncodedData(decodedDataError, "decodedDataError.txt");

            // Obliczenie BER
//            double BER = calculateBER(input, decodedData);
            double BER = calculateBER(input, decodedDataError);

            System.out.println("Dane wejściowe: " + Arrays.toString(input));
            System.out.println("Dane zakodowane: " + Arrays.toString(encodedData));
            //System.out.println("Dane zdekodowane: " + Arrays.toString(decodedData));
            System.out.println("Dane zdekodowane z zakłóceniami: " + Arrays.toString(decodedDataError));
            System.out.println("BER: " + BER);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Główna metoda obsługi turbo kodera
    private static int[] turboEncoder(int[] inputBits) {
        int[][] output = new int[inputBits.length][3];
        for (int i = 0; i < inputBits.length; i++) { // iteracja po danych wejściowych
            int inputBit = inputBits[i];

            // Operacje pierwszego kodera splotowego
            int S1_1 = register1[2] ^ register1[1]; // S1 = D3 ^ D2
            int S2_1 = inputBit ^ S1_1;             // S2 = X ^ S1
            int Y1_1 = inputBit;                    // Y1 = X
            int Y2_1 = register1[2] ^ register1[0] ^ inputBit ^ S1_1; // Y2 = D3 ^ D1 ^ X ^ S1

            int reg1Output = shiftRegister(register1, S2_1); // bit wypchnięty z pierwszego rejestru

            // Przejście wypchniętego bitu do interleavera i zamiana kolejności bitów
            int interleaverOutput = shiftInterleaver(interleaver, reg1Output); // bit wypchnięty z interleavera do rejestru 2

            // Operacje drugiego kodera splotowego
            int S1_2 = register2[2] ^ register2[1]; // S1 = D3 ^ D2
            int S2_2 = interleaverOutput ^ S1_2;    // S2 = interleaverOutput ^ S1
            int Y1_2 = interleaverOutput;           // Y1 = interleaverOutput
            int Y2_2 = register2[2] ^ register2[0] ^ interleaverOutput ^ S1_2; // Y2 = D3 ^ D1 ^ interleaverOutput ^ S1

            shiftRegister(register2, S2_2); // przesunięcie rejestru 2

            // Zapis zakodowanych danych
            output[i][0] = Y1_1;
            output[i][1] = Y2_1;
            output[i][2] = Y2_2;


            // Opcja analizy operacji wykonanych przez koder krok po kroku
            System.out.println("----------------");
//            System.out.println("s2_1: " + S2_1 + ", " + "y2: " + Y2_1);
            System.out.println("reg1: " + Arrays.toString(register1));
//            System.out.println("wyp z int: " + interleaverOutput);
            System.out.println("in: " + Arrays.toString(interleaver));
//            System.out.println("s2_2: " + S2_2 + ", " + "y3: " + Y2_2);
            System.out.println("reg2:" + Arrays.toString(register2));
        }

        // Wypisywanie zaszyfrowanych danych
        int[] flatOutput = new int[output.length * 3]; // tablica do przechowywania zakodowanych bitów
        int index = 0;

        // iteracja po zakodowanych danych
        for (int[] row : output) {
            for (int bit : row) {
                flatOutput[index++] = bit; // przypisywanie każdego bitu z tablicy wyjściowej tablicy zakodowanych bitów
            }
        }
        return flatOutput; // zwracanie zakodowanych bitów
    }


    // Przesunięcie danych w rejestrze
    private static int shiftRegister(int[] register, int newBit) {
        int shiftedOut = register[register.length - 1]; // bit wypchnięty
        for (int i = register.length - 1; i > 0; i--) {
            register[i] = register[i - 1]; // przesunięcie bitów w rejestrzr
        }
        register[0] = newBit; // zapisanie nowego bitu na początku
        return shiftedOut; // zwrócenie wypchniętego bitu
    }

    // Obsługa interleavera
    private static int shiftInterleaver(int[] interleaver, int newBit) {
        int output = interleaver[1];
        // Przesunięcie danych
        interleaver[1] = interleaver[0];
        interleaver[0] = newBit;
        // Zamiana miejsc bitów
        int bit = interleaver[0];
        interleaver[0] = interleaver[1];
        interleaver[1] = bit;
        return output; // zwrócenie wypchniętego bitu
    }

    // Generowanie zakłóceń w transmisji o określonym prawdopodobieństwie
    private static int[] errorGenerator(int[] encodedData, double errorProbability) {
        Random random = new Random();
        for (int i = 0; i < encodedData.length; i++) {
            if (random.nextDouble() < errorProbability) {
                encodedData[i] = encodedData[i] ^ 1; // wprowadzenie błędu do bitu
            }
        }
        return encodedData; // zwrócenie zakodowanych danych z błędami
    }

    // Czytanie danych z pliku
    private static int[] readInputData(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner fileContent = new Scanner(file); // tworzenie skanera do odczytu danych z pliku
        StringBuilder sb = new StringBuilder(); // tworzenie StringBuildera do przechowywania odczytanych danych

        // Odczytywanie danych bit po bicie
        while (fileContent.hasNextInt()) {
            sb.append(fileContent.nextInt()); // dodawanie odczytanych bitów do StringBuildera
        }
        fileContent.close(); // Zamknięcie skanera
        String inputString = sb.toString(); // konwersja zawartości StringBuildera na String
        int[] inputData = new int[inputString.length()]; // tworzenie tablicy do przechowywania bitów wejściowych

        // Konwersja stringa na tablicę bitów
        for (int i = 0; i < inputString.length(); i++) {
            inputData[i] = Character.getNumericValue(inputString.charAt(i)); // przypisywanie każdego znaku z stringa do tablicy bitów
        }
        return inputData; // zwrócenie tablicy bitów wejściowych
    }

    // Zapisywanie zakodowanych danych w pliku
    private static void saveEncodedData(int[] encodedData, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int bit : encodedData) {
            sb.append(bit).append(" "); // dodawanie bitów do stringa
        }
        Files.write(Paths.get(filePath), sb.toString().trim().getBytes()); // zapisanie stringa do pliku
    }

    // Obliczanie BER (Bit Error Rate)
    private static double calculateBER(int[] originalData, int[] decodedData) {
        int errorCount = 0;
        for (int i = 0; i < originalData.length; i++) {
            if (originalData[i] != decodedData[i]) {
                errorCount++; // liczenie błędnych bitów
            }
        }
        return (double) errorCount / originalData.length;
    }
}
