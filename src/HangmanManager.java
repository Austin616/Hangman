/*  Student information for assignment:
 *
 *  On my honor, <NAME>, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Austin Tran
 *  email address: austintran616@gmail.com
 *  UTEID: AAT3377
 *  Section 5-digit ID: 52620
 *  Grader name: SAI
 *  Number of slip days used on this assignment: 0
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class HangmanManager {
    private static final int MEDIUM_ALT_NUM = 4;
    private boolean debugOn;
    private int wordLen;
    private HangmanDifficulty diff;
    private int numGuesses;
    private int guessedWrong;
    private String currentPattern;
    private Set<String> words;
    private ArrayList<String> activeWords;
    private ArrayList<String> guessed;

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     *
     * @param words   A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) {
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("Words set must not be null or empty.");
        }
        this.words = words;
        this.debugOn = debugOn;
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     *
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        this(words, false);
    }

    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     *
     * @param length The given length to check.
     * @return the number of words in the original Dictionary
     * with the given length
     */
    public int numWords(int length) {
        int count = 0;
        for (String word : words) {
            if (word.length() == length) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get for a new round of Hangman. Think of a round as a
     * complete game of Hangman.
     *
     * @param wordLen    the length of the word to pick this time.
     *                   numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the
     *                   player loses the round. numGuesses >= 1
     * @param diff       The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        this.wordLen = wordLen;
        this.numGuesses = numGuesses;
        this.diff = diff;
        this.guessedWrong = 0;
        this.guessed = new ArrayList<>();
        this.currentPattern = "";
        this.activeWords = new ArrayList<>();
        initializeActiveWords();
        initializePattern();
    }

    //This method finds the initial words from the word length the user pics
    private void initializeActiveWords() {
        for (String word : words) {
            if (word.length() == wordLen) {
                activeWords.add(word);
            }
        }
    }

    //Creates a pattern of the initial word with asterisks
    private void initializePattern() {
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i < wordLen; i++) {
            patternBuilder.append('-');
        }
        currentPattern = patternBuilder.toString();
    }

    /**
     * The number of words still possible (live) based on the guesses so far.
     * Guesses will eliminate possible words.
     *
     * @return the number of words that are still possibilities based on the
     * original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return activeWords.size();
    }

    /**
     * Get the number of wrong guesses the user has left in
     * this round (game) of Hangman.
     *
     * @return the number of wrong guesses the user has left
     * in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return numGuesses - guessedWrong;
    }

    /**
     * Return a String that contains the letters the user has guessed
     * so far during this round.
     * The characters in the String are in alphabetical order.
     * The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     *
     * @return a String that contains the letters the user
     * has guessed so far during this round.
     */
    public String getGuessesMade() {
        Collections.sort(guessed);
        return guessed.toString();
    }

    /**
     * Check the status of a character.
     *
     * @param guess The character to check.
     * @return true if guess has been used or guessed this round of Hangman,
     * false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        return guessed.contains(String.valueOf(guess));
    }

    /**
     * Get the current pattern. The pattern contains '-''s for
     * unrevealed (or guessed) characters and the actual character
     * for "correctly guessed" characters.
     *
     * @return the current pattern.
     */
    public String getPattern() {
        return currentPattern;
    }

    /**
     * Update the game status (pattern, wrong guesses, word list),
     * based on the given guess.
     *
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        guessed.add(String.valueOf(guess));
        HashMap<String, ArrayList<String>> patternGroups = groupWordsByPattern(guess);
        TreeMap<String, Integer> patternCounts = countPatterns(patternGroups);
        //A series of nested if statements to find which pattern to use
        if (diff == HangmanDifficulty.HARD) {
            // For HARD difficulty, choose the hardest pattern.
            chooseHardest(patternGroups);
        } else if (diff == HangmanDifficulty.MEDIUM) {
            // For MEDIUM difficulty, choose the hardest pattern every 4th guess.
            if (guessed.size() % MEDIUM_ALT_NUM == 0) {
                secondHardest(patternGroups);
            } else {
                chooseHardest(patternGroups);
            }
        } else if (diff == HangmanDifficulty.EASY) {
            // For EASY difficulty, start with the hardest and then switch every move.
            if (guessed.size() % 2 == 0) {
                secondHardest(patternGroups);
            } else {
                chooseHardest(patternGroups);
            }
        }
        if (activeWords.isEmpty()) {
            throw new IllegalStateException("No words left in the active list.");
        }
        //update the list of activeWords
        activeWords = patternGroups.get(currentPattern);
        if (!currentPattern.contains(String.valueOf(guess))) {
            guessedWrong++;
        }
        return patternCounts;
    }

    // This helper method finds the hardest pattern in the patternGroup
    private void chooseHardest(HashMap<String, ArrayList<String>> patternGroups) {
        if (debugOn) {
            System.out.println("DEBUG: Choosing the hardest pattern.");
        }
        int maxWords = 0;
        String hardestPattern = "";

        // Iterate through each pattern in the patternGroups with a for loop
        for (Map.Entry<String, ArrayList<String>> entry : patternGroups.entrySet()) {
            int numWordsInPattern = entry.getValue().size();

            // Check if the current pattern has more words than the previous hardest pattern
            // or if it has the same number of words but comes before alphabetically
            if (numWordsInPattern > maxWords || (numWordsInPattern == maxWords &&
                    entry.getKey().compareTo(hardestPattern) < 0)) {
                // Update the hardest pattern and the maximum word count
                hardestPattern = entry.getKey();
                maxWords = numWordsInPattern;
            }
        }

        // Update the current pattern with the hardest pattern found
        if (!hardestPattern.isEmpty()) {
            currentPattern = hardestPattern;
        }
    }


    // This helper method chooses the second-hardest pattern from patternGroups
    private void secondHardest(HashMap<String, ArrayList<String>> patternGroups) {
        if (debugOn) {
            System.out.println("DEBUG: Choosing the second hardest pattern.");
        }
        // Check if patternGroups is empty, if so, exit as there are no patterns to choose from
        if (patternGroups.isEmpty()) {
            return;
        }
        HashMap<String, ArrayList<String>> copyPatternGroups = new HashMap<>(patternGroups);
        // Remove the currentPattern from the copy
        copyPatternGroups.remove(currentPattern);
        // If the copy still has patterns, choose the hardest pattern from it
        if (!copyPatternGroups.isEmpty()) {
            chooseHardest(copyPatternGroups);
        }
    }

    // This helper method groups words by their patterns based on the given guess
    private HashMap<String, ArrayList<String>> groupWordsByPattern(char guess) {
        // Initialize a HashMap to store patterns and corresponding words
        HashMap<String, ArrayList<String>> patternGroups = new HashMap<>();
        // Iterate through activeWords and create patterns for each word
        for (String word : activeWords) {
            String pattern = createPatternForWord(word, guess);
            // Check if the pattern already exists in patternGroups and adds it to the map if not
            if (patternGroups.containsKey(pattern)) {
                patternGroups.get(pattern).add(word);
            } else {
                // Create a new ArrayList for the pattern and add the word to it
                ArrayList<String> wordList = new ArrayList<>();
                wordList.add(word);
                patternGroups.put(pattern, wordList);
            }
        }

        return patternGroups;
    }

    // This helper method creates a pattern for a given word based on the current guess
    private String createPatternForWord(String word, char guess) {
        StringBuilder patternBuilder = new StringBuilder();

        // Iterate through the characters of the word
        for (int i = 0; i < word.length(); i++) {
            // If the character matches the guess or is already revealed, append the character
            if (word.charAt(i) == guess || currentPattern.charAt(i) != '-') {
                patternBuilder.append(word.charAt(i));
            } else {
                // if it doesn't match append a '-'
                patternBuilder.append('-');
            }
        }

        return patternBuilder.toString();
    }

    // This helper method counts the number of words in each pattern in patternGroups
    private TreeMap<String, Integer> countPatterns(HashMap<String, ArrayList<String>> pattern) {
        TreeMap<String, Integer> patternCounts = new TreeMap<>();
        // Iterate through patternGroups and count the number of words in each pattern
        for (Map.Entry<String, ArrayList<String>> entry : pattern.entrySet()) {
            int numWordsInPattern = entry.getValue().size();
            // If there are words in the pattern, add it to patternCounts
            if (numWordsInPattern > 0) {
                patternCounts.put(entry.getKey(), numWordsInPattern);
            }
        }

        return patternCounts;
    }

    /**
     * Return the secret word this HangmanManager finally ended up
     * picking for this round.
     * If there are multiple possible words left, one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     *
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if (activeWords.isEmpty()) {
            throw new IllegalStateException("No words left in the active list.");
        }
        ArrayList<String> activeWordsList = new ArrayList<>(activeWords);
        // Mix up the list, so you get a random word
        Collections.shuffle(activeWordsList);
        return activeWordsList.get(0);
    }
}