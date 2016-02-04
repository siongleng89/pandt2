package abstracts;

import com.potatoandtomato.common.DownloaderListener;
import com.potatoandtomato.common.IDownloader;
import com.potatoandtomato.common.SafeThread;
import com.potatoandtomato.common.Status;

import java.io.File;

/**
 * Created by SiongLeng on 15/12/2015.
 */
public class MockDownloader implements IDownloader {
    @Override
    public SafeThread downloadFileToPath(String urlString, File targetFile, DownloaderListener listener) {
        listener.onStep(100);
        listener.onCallback(null, Status.SUCCESS);
        return new SafeThread();
    }

    @Override
    public void downloadData(String url, DownloaderListener listener) {

    }
}
