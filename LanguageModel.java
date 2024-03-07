import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import javax.print.DocFlavor.STRING;

    public class LanguageModel {

        // The map of this model.
        // Maps windows to lists of charachter data objects.
        HashMap<String, List> CharDataMap;
        
        // The window length used in this model.
        int windowLength;
        
        // The random number generator used by this model. 
        private Random randomGenerator;

        /** Constructs a language model with the given window length and a given
         *  seed value. Generating texts from this model multiple times with the 
         *  same seed value will produce the same random texts. Good for debugging. */
        public LanguageModel(int windowLength, int seed) {
            this.windowLength = windowLength;
            randomGenerator = new Random(seed);
            CharDataMap = new HashMap<String, List>();
        }

        /** Constructs a language model with the given window length.
         * Generating texts from this model multiple times will produce
         * different random texts. Good for production. */
        public LanguageModel(int windowLength) {
            this.windowLength = windowLength;
            randomGenerator = new Random();
            CharDataMap = new HashMap<String, List>();
        }

        /** Builds a language model from the text in the given file (the corpus). */
        public void train(String fileName) {
            In in = new In(fileName);
            String window = "";
            char c;

            for (int i = 0 ; i < windowLength ; i++) {
                c = in.readChar();
                window = window + c;
            }

            while (!in.isEmpty()) {
                c = in.readChar();
                List probs = CharDataMap.get(window);
                if (probs == null) {
                    probs = new List();
                    CharDataMap.put(window, probs);
                }
                probs.update(c);
                window += c;
                window = window.substring(1);
            }
            
            for (List probs : CharDataMap.values()) {
                calculateProbabilities(probs);
            }
        }

        // Computes and sets the probabilities (p and cp fields) of all the
        // characters in the given list. */
        public void calculateProbabilities(List probs) {				
            int totalChars = 0;

            
            for (int i = 0; i < probs.getSize(); i++) {
                totalChars += probs.get(i).count;
            }
        
            double cumulativeProbability = 0;
        
            
            for (int i = 0; i < probs.getSize(); i++) {
                CharData charData = probs.get(i);
                charData.p = (double) charData.count / totalChars;  
                cumulativeProbability += charData.p;  
                charData.cp = cumulativeProbability;  
            }
        }


        // Returns a random character from the given probabilities list.
        public char getRandomChar(List probs) {
            double rand = randomGenerator.nextDouble();
            double cumulativeProbability = 0;
            int index = 0;
            CharData charData = null;

            while (true) {
                try {
                    charData = probs.get(index++);
                    cumulativeProbability += charData.p; 
                    if (rand <= cumulativeProbability) {
                        return charData.chr; 
                    }
                } catch (IndexOutOfBoundsException e) {
                    break; 
                }
            }

            return ' ';
        }

        /**
         * Generates a random text, based on the probabilities that were learned during training. 
         * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
         * doesn't appear as a key in Map, we generate no text and return only the initial text. 
         * @param numberOfLetters - the size of text to generate
         * @return the generated text
         */
        public String generate(String initialText, int textLength) {
            if (initialText.length() < windowLength){
           
                return initialText;
            }

            String window = initialText.substring(initialText.length() - windowLength); 
            String generatedText = window; 
    
            for (int i = 0; i < textLength; i++){
                List probs = CharDataMap.get(window);
                if (probs != null){
                    char newChar = getRandomChar(probs); 
                    generatedText += newChar ;
                    window = generatedText.substring(generatedText.length() - windowLength);
                } else {
                    return generatedText ; 
                }
            }
    
            return generatedText ;
        }

        /** Returns a string representing the map of this language model. */
        public String toString() {
            StringBuilder str = new StringBuilder();
            for (String key : CharDataMap.keySet()) {
                List keyProbs = CharDataMap.get(key);
                str.append(key + " : " + keyProbs + "\n");
            }
            return str.toString();
        }

        public static void main(String[] args) {
            int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
        lm = new LanguageModel(windowLength);
        else
        lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
