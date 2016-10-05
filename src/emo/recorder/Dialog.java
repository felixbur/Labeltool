package emo.recorder;

import java.util.Iterator;
import java.util.Vector;


public class Dialog implements Angerable {
    Vector<Recording> recs;

    public void addRec(Recording r) {
        if (recs == null) {
            recs = new Vector<Recording>();
        }
        recs.add(r);
    }

    public int getTimeInSec() {
        int t=0;
        for (Iterator<Recording> iterator = recs.iterator(); iterator.hasNext();) {
            Recording rec = iterator.next();
            t+= rec.getTimeInSec();
        }
        return t;
    }

    public boolean isAngry() {
        for (Iterator<Recording> iterator = recs.iterator(); iterator.hasNext();) {
            Recording rec = iterator.next();
            if (rec.isAngry()) {
                return true;
            }
        }
        return false;
    }

    public boolean seemsAngry(double threshold) {
        for (Iterator<Recording> iterator = recs.iterator(); iterator.hasNext();) {
            Recording rec = iterator.next();
            if (rec.seemsAngry(threshold)) {
                return true;
            }
        }
        return false;
    }

}
