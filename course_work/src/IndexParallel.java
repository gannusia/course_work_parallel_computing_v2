import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class IndexParallel extends Thread {

    String[] arrayPath; //список з адресами
    String[] arrayName; //список з назвами файлів
    int start; //початковий індекс для обробки
    int finish; //кінцевий індекс для обробки

    Map<String, HashSet<String>> tempInd = new HashMap<>(); //хешмапа для результату роботи кожного потоку

    public IndexParallel(String[] arrayPath, String[] arrayName, int start, int finish) {
        this.arrayPath = arrayPath;
        this.arrayName = arrayName;
        this.start = start;
        this.finish = finish;
    }

    public Map<String, HashSet<String>> getResult() {
        return  tempInd;
    }

    @Override
    public void run() {
        for (int i = start; i < finish; i++) { //обирається, які саме файли буде оброблено
            try (BufferedReader file = new BufferedReader(new FileReader(arrayPath[i]))) {
                String line;
                while ((line = file.readLine()) != null) {
                    String[] words = line.split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        if (!tempInd.containsKey(word)) {
                            tempInd.put(word, new HashSet<String>());
                        }
                        tempInd.get(word).add(arrayName[i]);
                    }
                }
            } catch (IOException e) {
                System.out.println("File " + arrayPath[i] + " not found.");
            }
        }
    }
}