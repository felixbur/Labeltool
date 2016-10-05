package emo.recorder;
import java.util.Iterator;
import java.util.Vector;


public class Analyser {
    public static String analyse(Vector<? extends Angerable> recs, double threshold) {
        int total = 0, nn = 0, aa = 0, an = 0, na = 0, nlength = 0, alength = 0;
        for (Iterator<? extends Angerable> iterator = recs.iterator(); iterator.hasNext();) {
            Angerable angerable = iterator.next();
            if (angerable.isAngry()) {
                alength += angerable.getTimeInSec();
                if (angerable.seemsAngry(threshold)) {
                    aa++;
                } else {
                    an++;
                }
            } else {
                nlength += angerable.getTimeInSec();
                if (angerable.seemsAngry(threshold)) {
                    na++;
                } else {
                    nn++;
                }
            }
        }
        total = nn + an + na + aa;
        int neut = nn + na;
        int ang = aa + an;
        double neutMins = Util.roundDouble(nlength / 60.0);
        double angMins = Util.roundDouble(alength / 60.0);
        int aap = (int) (100 * Util.roundDouble((double) aa / total));
        int anp = (int) (100 * Util.roundDouble((double) an / total));
        int nnp = (int) (100 * Util.roundDouble((double) nn / total));
        int nap = (int) (100 * Util.roundDouble((double) na / total));
        double nPrec = Util.roundDouble((double) nn / (nn+an));
        double nRecall = Util.roundDouble((double) nn / (nn+na));
        double aPrec = Util.roundDouble((double) aa / (aa+an));
        double aRecall = Util.roundDouble((double) aa / (aa+na));
        double f_a_05 = Util.roundDouble(1.5 * ((aPrec*aRecall)/(0.5*(aPrec+aRecall))));
        String anatext = "all: " + total + " n: " + neut + " (" + neutMins + " min), a: " + ang + " (" + angMins
                + " min), aa: " + aap + ", an: " + anp + ", nn: " + nnp + ", _neutralPred: " + nap;
        anatext+= ", nRec: "+nRecall+", nPrec: "+nPrec+", aRec: "+aRecall+", aPrec: "+aPrec+", F-measure 0,5: "+f_a_05;
        
        return anatext;
    }

}
