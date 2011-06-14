package manyminds.util;

import java.text.DateFormat;
import java.util.Date;

public class
StatedDate
extends Date {

    private DateFormat myFormat;
    
    public
    StatedDate(long d, DateFormat df) {
        super(d);
        myFormat = df;
    }
    
    public String
    toString() {
        return myFormat.format(this);
    }
}
