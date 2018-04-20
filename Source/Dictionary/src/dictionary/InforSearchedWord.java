/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dictionary;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Gokki
 */
public class InforSearchedWord implements Serializable{

    Date SearchDay;
    int num = 0;

    public InforSearchedWord() {
    }

    public InforSearchedWord(Date SearchDay, int num) {
        this.SearchDay = SearchDay;
        this.num = num;
    }

    public void setSearchDay(Date SearchDay) {
        this.SearchDay = SearchDay;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Date getSearchDay() {
        return SearchDay;
    }

    public int getNum() {
        return num;
    }

}
