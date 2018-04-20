/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dictionary;

/**
 *
 * @author Gokki
 */
public class Dictionary {

    private String word;

    public void setWord(String word) {
        this.word = word;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

    public Dictionary() {
    }

    public Dictionary(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
    }
    private String meaning;
    /**
     * @param args the command line arguments
     */
}
