package martinmatko.anatomy;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Created by Martin on 11.10.2015.
 */
public class PartOfBody {
    private Paint paint;
    private Path path;
    private RectF boundaries;

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

    public boolean isRightAnswer() {
        return isRightAnswer;
    }

    public void setRightAnswer(boolean isRightAnswer) {
        this.isRightAnswer = isRightAnswer;
    }

    private boolean isRightAnswer;

    public PartOfBody(Path path, Paint paint) {
        this.paint = paint;
        this.path = path;
        boundaries = new RectF();
        path.computeBounds(boundaries, true);
    }

    public RectF getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(RectF boundaries) {
        this.boundaries = boundaries;
    }
}
