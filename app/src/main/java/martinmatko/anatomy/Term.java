package martinmatko.anatomy;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 19.12.2015.
 */
public class Term {
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public List<PartOfBody> getPartOfBodyList() {
        return partOfBodyList;
    }

    public void setPartOfBodyList(List<PartOfBody> partOfBodyList) {
        this.partOfBodyList = partOfBodyList;
    }

    private int color;

    public int getOriginalColor() {
        return originalColor;
    }

    public void setOriginalColor(int originalColor) {
        this.originalColor = originalColor;
    }

    private int originalColor;
    private String id;
    private String name;
    private String identifier;
    private RectF button;
    private List<PartOfBody> partOfBodyList;

    public Term(String name, String identifier, String id) {

        this.name = name;
        this.identifier = identifier;
        this.id = id;
        partOfBodyList = new ArrayList<>();
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
