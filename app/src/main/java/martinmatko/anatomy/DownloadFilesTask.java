package martinmatko.anatomy;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class DownloadFilesTask extends AsyncTask<URL, Integer, InputStream> {
    protected InputStream doInBackground(URL... urls) {
        int count = urls.length;
        String u = urls[0].toString();
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(u);
        HttpResponse res = null;
        InputStream is;
        try {
            res = client.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            is = res.getEntity().getContent();
            return is;
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            DataInputStream stream = new DataInputStream(u.openStream());
//            return stream;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        long totalSize = 0;
//        for (int i = 0; i < count; i++) {
//            totalSize += Downloader.downloadFile(urls[i]);
//            publishProgress((int) ((i / (float) count) * 100));
//            // Escape early if cancel() is called
//            if (isCancelled()) break;
//        }
        return null;
    }

//    protected void onProgressUpdate(Integer... progress) {
//        setProgressPercent(progress[0]);
//    }
//
//    protected void onPostExecute(Long result) {
//        showDialog("Downloaded " + result + " bytes");
//    }
}
