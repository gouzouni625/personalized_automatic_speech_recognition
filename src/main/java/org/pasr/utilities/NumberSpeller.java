package org.pasr.utilities;


public class NumberSpeller {
    private NumberSpeller () {
    }

    public String spell(int number) {
        return spell(number, Types.NORMAL);
    }

    public String spell(int number, Types type) {
        // Google Translate pronounces:
        // 1009 -> one thousand and nine
        // 1010 -> ten ten
        switch (type){
            case NORMAL:
                return spellNormal(number);
            case DATE:
                return spellDate(number);
            default:
                return "Unknown Type";
        }
    }

    private String spellNormal(int number){
        if(number <= 20){
            return spellUpToTwenty(number);
        }
        else if(number <= 100){
            return spellUpToAHundred(number);
        }
        else if(number <= 1000){
            return spellUpToAThousand(number);
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int thousand = 0; thousand < 4; thousand++) {
                int currentNumber = number % 1000;

                if(currentNumber != 0) {
                    stringBuilder.insert(0, spellUpToAThousand(currentNumber) + " " +
                        THOUSANDS[thousand] + " ");
                }

                number /= 1000;
            }

            return stringBuilder.toString().trim();
        }
    }

    private String spellDate(int number){
        if(number <= 1009 || number > 10000){
            return spellNormal(number);
        }
        else {
            int hundreds = number / 100;
            int remainder = number % 100;

            StringBuilder stringBuilder = new StringBuilder();
            if (hundreds != 0) {
                stringBuilder.append(spellUpToAHundred(hundreds));
            }

            if (remainder != 0) {
                stringBuilder.append(" ").append(spellUpToAHundred(remainder));
            }
            else{
                stringBuilder.append(" hundred");
            }

            return stringBuilder.toString();
        }
    }

    private String spellUpToAThousand(int number){
        if(number <= 20){
            return spellUpToTwenty(number);
        }
        else if(number <= 100){
            return spellUpToAHundred(number);
        }
        else if(number < 1000){
            int hundreds = number / 100;
            int remainder = number % 100;

            StringBuilder stringBuilder = new StringBuilder();
            if(hundreds != 0){
                stringBuilder.append(spellUpToTwenty(hundreds)).append(" hundred");
            }

            if(remainder != 0){
                stringBuilder.append(" ").append(spellUpToAHundred(remainder));
            }

            return stringBuilder.toString();
        }
        else{
            return "one thousand";
        }
    }

    private String spellUpToAHundred(int number){
        if(number <= 20){
            return spellUpToTwenty(number);
        }
        else if(number < 100){
            int tens = number / 10;
            int units = number % 10;

            if(units == 0){
                return TENS[tens];
            }
            else{
                return TENS[tens] + " " + TWENTIES[units];
            }
        }
        else{
            return "one hundred";
        }
    }

    private String spellUpToTwenty(int number){
        return TWENTIES[number];
    }

    public static NumberSpeller getInstance () {
        return instance;
    }

    public enum Types{
        NORMAL,
        DATE
    }

    private final String[] TWENTIES = {
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen",
        "twenty"
    };

    private final String[] TENS = {
        "",
        "ten",
        "twenty",
        "thirty",
        "forty",
        "fifty",
        "sixty",
        "seventy",
        "eighty",
        "ninety",
        "hundred"
    };

    private final String[] THOUSANDS = {
        "",
        "thousand",
        "million",
        "billion"
    };

    private static NumberSpeller instance = new NumberSpeller();

}
