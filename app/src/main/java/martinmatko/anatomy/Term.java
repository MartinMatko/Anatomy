package martinmatko.anatomy;

import android.graphics.RectF;

/**
 * Created by Martin on 19.12.2015.
 */
public class Term {
    private String name;
    private String identifier;
    private RectF button;

    public Term(String name, String identifier) {

        this.name = name;
        this.identifier = identifier;
    }

    public RectF getButton() {
        return button;
    }

    public void setButton(RectF button) {
        this.button = button;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
