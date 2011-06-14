package manyminds.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class
StateDateFormat
extends SimpleDateFormat {
    
    
    public
    StateDateFormat(String s) {
        super(s);
    }
    
    public Date
    parse(String s, ParsePosition pos) {
        try {
            Date retVal = super.parse(s,pos);
            return new StatedDate(retVal.getTime(), this);
        } catch (Throwable t) {
            System.err.println("Can't parse "+s);
            t.printStackTrace();
            return null;
        }
    }
}
    