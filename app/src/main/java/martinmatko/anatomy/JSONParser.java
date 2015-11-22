package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        //is = new DownloadFilesTask().doInBackground(new URL("http://anatom.cz/flashcards/context/280"));//
        is = ctx.getResources().openRawResource(R.raw.bodyjson);
        JSONObject body = new JSONObject();
        String json = readFully(is, "UTF-8");
        String content = null, caption;
        try {
            body = new JSONObject(json);
            JSONObject data = body.getJSONObject("data");
            caption = data.getString("name");
            content = data.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SVGParser parser = new SVGParser();
        List<PartOfBody> parts = new ArrayList<>();
        List<String> parsed;
        //parsed = new ArrayList<>(Arrays.asList(body.split("d\\\\\": \\\\\"")));
        parsed = new ArrayList<>(Arrays.asList(content.split("bbox")));
        parsed.remove(0);
        parsed.remove(parsed.size()-1);
        parsed.remove(parsed.size()-1);
        for (String line : parsed){
            //line = line.substring(0, line.indexOf("\\"));
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            int startOfColor = line.indexOf("color")+9;
            String color = line.substring(startOfColor, startOfColor + 7);
            paint.setColor(Color.parseColor(color));
            line = line.substring(line.indexOf('M'), line.indexOf('z')+1);
            if (line.startsWith("M")){
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
