package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Martin on 7.11.2015.
 */
public class JSONParser {
    public List<PartOfBody> getBodyParts() throws IOException {
        Context ctx = MainActivity.getAppContext();
        InputStream is;
        is = ctx.getResources().openRawResource(R.raw.bodyjson);
        String body = readFully(is, "UTF-8");
        SVGParser parser = new SVGParser();
        List<PartOfBody> parts = new ArrayList<>();
        List<String> parsed;
        parsed = new ArrayList<>(Arrays.asList(body.split("d\\\\\": \\\\\"")));
        parsed.remove(0);
        for (String line : parsed){
            line = line.substring(0, line.indexOf("\\"));
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLACK);
            //paint.setStrokeWidth(2);
            if (line.startsWith("fill")){
                String color = line.substring(line.indexOf('#'), line.indexOf("\" d="));
                //paint.setColor(Color.parseColor(color   ));
                //paint.setColor(Integer.parseInt(color, 32));
            }
            line = line.substring(line.indexOf('M'), line.indexOf('z')+1);
            if (line.startsWith("M")){
                if (line == parsed.get(0))
                    parts.add(new PartOfBody(parser.doPath(line), paint, true));
                else
                    parts.add(new PartOfBody(parser.doPath(line), paint) );
            }
        }
        return parts;
    }
    public String readFully(InputStream inputStream, String encoding)
            throws IOException {
        return new String(readFully(inputStream), encoding);
    }

    private byte[] readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }
}
