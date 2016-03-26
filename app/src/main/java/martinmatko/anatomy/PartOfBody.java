package martinmatko.anatomy;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Created by Martin on 11.10.2015.
 */
public class PartOfBody {
    private Paint paint;
    private Paint originalPaint;
    private Path path;
    private RectF boundaries;
    private boolean selected;
    private boolean right;
    private String identifier;
    public PartOfBody(Path path, Paint paint, String identifier) {
        this.paint = paint;
        this.identifier = identifier;
        this.path = path;
    }
    public PartOfBody(Path path, Paint paint) {
        this.paint = paint;
        this.path = path;
        boundaries = new RectF();
        path.computeBounds(boundaries, true);
    }

    public Paint getOriginalPaint() {
        return originalPaint;
    }

    public void setOriginalPaint(Paint originalPaint) {
        this.originalPaint = originalPaint;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public RectF getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(RectF boundaries) {
        this.boundaries = boundaries;
    }
}
