package da.gammla.AnchoredTable;

import java.io.Serializable;

/**
 * Created by Mega
 * Intellij IDEA
 */
public class AnchoredData implements Serializable {

    String anchor, data;

    public AnchoredData(String anchor, String data){
        this.anchor = anchor;
        this.data = data;
    }

    public boolean isQualified(String anchor){
        return anchor == this.anchor || anchor.equals(this.anchor);
    }

    public String getAnchor() {
        return anchor;
    }

    public String getData() {
        return data;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public void setData(String data) {
        this.data = data;
    }

}
