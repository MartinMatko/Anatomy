package martinmatko.anatomy;

import android.graphics.RectF;

/**
 * Created by Martin on 19.12.2015.
 */
public class Term {
    int color;
    private String id;
    private String name;
    private String identifier;
    private RectF button;
    public Term(String name, String identifier, String id) {

        this.name = name;
        this.identifier = identifier;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
