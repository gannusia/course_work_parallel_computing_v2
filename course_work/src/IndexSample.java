import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;

public class IndexSample {

    Map<String, HashSet<String>> index;

    IndexSample() {
        index = new HashMap<String, HashSet<String>>();
    }

    public void buildIndex(String[] files, String[] names) {
        for (int i = 0; i < files.length; i++) {
            try (BufferedReader file = new BufferedReader(new FileReader(files[i]))) { //відкриваємо для читання поточний файл
                String line;
                while ((line = file.readLine()) != null) { //зчитуємо вміст файлу у змінну line
                    String[] words = line.split("\\W+"); //розділяємо line на окремі слова та записуємо їх до масиву
                    for (String word : words) {
                        word = word.toLowerCase();
                        if (!index.containsKey(word)) { //якщо такого слова ще немає у хешмапі
                            index.put(word, new HashSet<String>()); //додаємо слово до хешмапи
                        }
                        index.get(word).add(names[i]); //додаємо назву файлу за відповідним ключем
                    }
                }
            } catch (IOException e) {
                System.out.println("File " + files[i] + " not found.");
            }
        }
    }

    public void findWord (String phrase) {
        String[] words = phrase.split("\\W+"); //розділяємо введений користувачем рядок на окремі слова
        if (words.length > 1) {
            HashSet<String> answer = new HashSet<String>(index.get(words[0].toLowerCase()));
            for (String word : words) {
                answer.retainAll(index.get(word)); //залишаємо лише ті файли, у яких є всі перелічені слова
            }
            if (answer.size() == 0) {
                System.out.println("Not found");
            } else {
                System.out.println("Intersection of words: ");
                for (String ans : answer) {
                    System.out.println("\t" + ans);
                }
            }
        }
        if (words.length >= 1) {
            for (String word : words) {
                System.out.println("Files for '" + word + "' are: " + index.get(word));
            }
        }
    }

    public static void main (String[] args) throws IOException, InterruptedException {
        //проходимо по усіх файлах та додаємо їх до списків
        List<String> filePath = new ArrayList<>(); //список адрес усіх файлів
        List<String> fileName = new ArrayList<>(); //список назв усіх файлів

        File dirPath1 = new File("D:/PO/Kursova/datasets/test_neg");
        File dirPath2 = new File("D:/PO/Kursova/datasets/test_pos");
        File dirPath3 = new File("D:/PO/Kursova/datasets/train_neg");
        File dirPath4 = new File("D:/PO/Kursova/datasets/train_pos");
        File dirPath5 = new File("D:/PO/Kursova/datasets/train_unsup");

        //Зчитуємо дані для файлів за відповідною адресою
        File files1[] = dirPath1.listFiles();
        for (int i = 0; i < files1.length; i++) {
            filePath.add(files1[i].getPath());
            fileName.add(files1[i].getName());
            //System.out.println(files1[i].getName());
        }
        File files2[] = dirPath2.listFiles();
        for (int i = 0; i < files2.length; i++) {
            filePath.add(files2[i].getPath());
            fileName.add(files2[i].getName());
        }
        File files3[] = dirPath3.listFiles();
        for (int i = 0; i < files3.length; i++) {
            filePath.add(files3[i].getPath());
            fileName.add(files3[i].getName());
        }
        File files4[] = dirPath4.listFiles();
        for (int i = 0; i < files4.length; i++) {
            filePath.add(files4[i].getPath());
            fileName.add(files4[i].getName());
        }
        File files5[] = dirPath5.listFiles();
        for (int i = 0; i < files5.length; i++) {
            filePath.add(files5[i].getPath());
            fileName.add(files5[i].getName());
        }

        //перетворюємо отримані списки у масиви
        String[] sPath = new String[filePath.size()];
        String[] sName = new String[fileName.size()];
        sPath = filePath.toArray(sPath);
        sName = fileName.toArray(sName);

        //користувач задає кількість потоків
        System.out.println("\nEnter the number of threads: ");
        BufferedReader readerTwo = new BufferedReader(new InputStreamReader(System.in));
        String threadsStr = readerTwo.readLine();
        int threads = Integer.parseInt(threadsStr);

        System.out.println("Print search phrase: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String phrase = in.readLine();


        //-------------------| Послідовний алгоритм |----------------------//

        long timeSerialStart = System.currentTimeMillis();
        IndexSample index = new IndexSample();
        index.buildIndex(sPath, sName);
        long timeSerialFinish = System.currentTimeMillis() - timeSerialStart;


        //-------------------| Паралельний алгоритм |---------------------//

        long timeParallelStart = System.currentTimeMillis();
        Map<String, HashSet<String>> parallelIndex = new HashMap<>();
        IndexParallel ThreadArray[] = new IndexParallel[threads];

        for (int i = 0; i < threads; i++) { //розподіляємо роботу між потоками
            ThreadArray[i] = new IndexParallel(sPath, sName,sPath.length / threads * i, i == (threads - 1) ? sPath.length : sPath.length / threads * (i + 1));
            ThreadArray[i].start();
        }
        for (int i = 0; i < threads; i++) {
            ThreadArray[i].join();
        }
        for (int i = 0; i < threads; i++) {
            //об'єднуємо усі хешмапи в одну
            for (Map.Entry<String, HashSet<String>> temp : ThreadArray[i].getResult().entrySet()) { //перебір елементів хештаблиці
                String key = temp.getKey(); //зчитуємо ключ для цього запису в Map
                HashSet<String> value = temp.getValue(); //зчитуємо значення для цього запису в Map
                if (parallelIndex.containsKey(key)) {
                    parallelIndex.computeIfAbsent(key, w -> new HashSet<>()).addAll(value); //додаємо значення за вказаним ключем
                } else {
                    parallelIndex.put(key, value);
                }
            }
        }
        long timeParallelFinish = System.currentTimeMillis() - timeParallelStart;


        //---------------------| Виведення результатів |----------------------//

        System.out.println("\nSerial time for building an index: "+ (timeSerialFinish));
        System.out.println("Parallel time for building an index: "+ (timeParallelFinish));

        System.out.println("\nSerial result: ");
        index.findWord(phrase);

        System.out.println("\nFor " + threads + " threads we have: ");
        System.out.println("Parallel result: ");
        String[] words = phrase.split("\\W+"); //розділяємо введений користувачем рядок на окремі слова
        if (words.length > 1) {
            HashSet<String> answer = new HashSet<String>(parallelIndex.get(words[0].toLowerCase()));
            for (String word : words) {
                answer.retainAll(parallelIndex.get(word)); //залишаємо лише ті файли, у яких є всі перелічені слова
            }
            if (answer.size() == 0) {
                System.out.println("Not found");
            } else {
                System.out.println("Intersection of words: ");
                for (String ans : answer) {
                    System.out.println("\t" + ans);
                }
            }
        }
        if (words.length >= 1) {
            for (String word : words) {
                System.out.println("Files for '" + word + "' are: " + parallelIndex.get(word));
            }
        }
    }
}
