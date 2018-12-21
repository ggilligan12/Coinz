package com.example.george.coinz;

public class Coin {

    private static String ID;
    private static String currency;
    private static Float value;

    public static void setID(String s){ID=s;}
    public static void setCurrency(String s){currency=s;}
    public static void setValue(Float f){value=f;}

    public static String getID() {return ID;}
    public static String getCurrency() {return currency;}
    public static Float getValue() {return value;}

}
