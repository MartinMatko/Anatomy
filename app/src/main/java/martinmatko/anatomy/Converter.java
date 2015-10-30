package martinmatko.anatomy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Martin on 28.9.2015.
 */
public class Converter {
    public static void Convert(String path1, String path2) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path1));
        FileReader reader = new FileReader(path1);
        try {
            StringBuilder sb = new StringBuilder();
            String l;
            String everything = "";
            while ((l = br.readLine())!=null)
            everything += l;

            /*while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();*/

            //String[] stringSeparators = new String[] { "\\\"d\\\": \\\"", "z\\\"" };
            String[] parsed = everything.split("\\\"d\\\":|z\\\"");

            FileWriter writer = new FileWriter(path2);
            writer.write(Long.toString(System.currentTimeMillis()));
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                    "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">" +
                    "<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"976px\"" +
                    "   height=\"325px\" viewBox=\"0 0 976 325\" enable-background=\"new 0 0 976 325\" xml:space=\"preserve\">\n<svg>\n");
            for (String line : parsed)
            {
                if (!line.startsWith(", \\\"bbox\\") && !line.startsWith("  \\\"color\\"))
                {
                    writer.write("<path d=\"" + line.replace(",", " ") + "z\" stroke=\"red\" stroke-width=\"3\" fill=\"none\"/>" +String.format("%n"));
                }

            }
            writer.write("<\\svg>");
            writer.write(Long.toString(System.currentTimeMillis()));
            writer.close();

        } finally {
            reader.close();
        }
    }
}
