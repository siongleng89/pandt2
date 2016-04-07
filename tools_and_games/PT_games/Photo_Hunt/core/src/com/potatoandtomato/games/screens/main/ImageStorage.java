package com.potatoandtomato.games.screens.main;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.potatoandtomato.common.GameCoordinator;
import com.potatoandtomato.common.absints.DownloaderListener;
import com.potatoandtomato.common.enums.Status;
import com.potatoandtomato.common.utils.SafeThread;
import com.potatoandtomato.common.utils.Threadings;
import com.potatoandtomato.common.utils.ThreadsPool;
import com.potatoandtomato.games.absintf.DatabaseListener;
import com.potatoandtomato.games.absintf.ImageStorageListener;
import com.potatoandtomato.games.models.ImageDetails;
import com.potatoandtomato.games.models.ImagePair;
import com.potatoandtomato.games.models.Services;
import com.potatoandtomato.games.services.Database;

import java.util.ArrayList;

/**
 * Created by SiongLeng on 7/4/2016.
 */
public class ImageStorage implements Disposable {

    private Services services;
    private GameCoordinator gameCoordinator;
    private int totalIndex;
    private ArrayList<ImagePair> imagePairs;
    private boolean randomize = true;
    private int imageCountPerDownload = 5;
    private int currentIndex;
    private int orderIndex;           //used to make sure final results is in same order
    private long downloadPeriod = 30000;
    private SafeThread safeThread;

    public ImageStorage(Services services, GameCoordinator gameCoordinator) {
        this.services = services;
        this.gameCoordinator = gameCoordinator;
        this.imagePairs = new ArrayList();
        safeThread = new SafeThread();
    }

    public void startMonitor(){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {

                final Threadings.ThreadFragment threadFragment = new Threadings.ThreadFragment();
                services.getDatabase().getLastImageIndex(new DatabaseListener<Integer>() {
                    @Override
                    public void onCallback(Integer obj, Status st) {
                        totalIndex = obj;
                        threadFragment.setFinished(true);
                    }
                });

                while (!threadFragment.isFinished()){
                   Threadings.sleep(300);
                }

                while (true){
                    if(gameCoordinator.meIsDecisionMaker()){
                        initiateDownloadsIfNeeded();
                    }
                    Threadings.sleep(downloadPeriod);
                    if(safeThread.isKilled()){
                        break;
                    }
                }
            }
        });
    }


    public void initiateDownloadsIfNeeded(){
        if(imagePairs.size() < 5){
            final ArrayList<Integer> indexes = new ArrayList<>();
            for(int i = 0; i < imageCountPerDownload; i++){
                if(randomize){
                    indexes.add(MathUtils.random(0, totalIndex));
                }
                else{
                    indexes.add(currentIndex);
                    currentIndex++;
                }
            }

            convertImageIndexesToImageIds(indexes, new DatabaseListener<ArrayList<String>>() {
                @Override
                public void onCallback(ArrayList<String> imageIds, Status st) {
                    services.getRoomMsgHandler().sendDownloadImageRequest(imageIds);
                }
            });
        }
    }

    public void convertImageIndexesToImageIds(final ArrayList<Integer> indexes, final DatabaseListener<ArrayList<String>> listener){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                ThreadsPool threadsPool = new ThreadsPool();
                final ArrayList<String> imageIds = new ArrayList<String>();

                int i = 0;
                for(Integer index : indexes){
                    final Threadings.ThreadFragment threadFragment = new Threadings.ThreadFragment();
                    final int finalI = i;
                    services.getDatabase().getImageDetailsByIndex(index, new DatabaseListener<ImageDetails>(ImageDetails.class) {
                        @Override
                        public void onCallback(ImageDetails details, Status st) {
                            if(st == Status.SUCCESS){
                                imageIds.add(finalI <= imageIds.size() ? finalI : imageIds.size(), details.getId());
                            }
                            threadFragment.setFinished(true);
                        }
                    });

                    threadsPool.addFragment(threadFragment);
                    i++;
                }

                while (!threadsPool.allFinished()){
                    Threadings.sleep(200);
                }

                listener.onCallback(imageIds, Status.SUCCESS);

            }
        });


    }

    public void receivedDownloadRequest(final ArrayList<String> imageIds){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                for(String id : imageIds){
                    final Threadings.ThreadFragment threadFragment = new Threadings.ThreadFragment();
                    services.getDatabase().getImageDetailsById(id, new DatabaseListener<ImageDetails>(ImageDetails.class) {
                        @Override
                        public void onCallback(ImageDetails details, Status st) {
                            if (st == Status.SUCCESS) {
                                downloadImages(details, orderIndex);
                            }
                            threadFragment.setFinished(true);
                        }
                    });

                    while (!threadFragment.isFinished()){
                        Threadings.sleep(200);
                    }
                    orderIndex++;
                }
            }
        });
    }

    public void downloadImages(final ImageDetails imageDetails, final int currentOrderIndex){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {

                    final Texture[] image1 = new Texture[1];
                    final Texture[] image2 = new Texture[1];

                    ThreadsPool threadsPool = new ThreadsPool();
                    final Threadings.ThreadFragment threadFragment1 = new Threadings.ThreadFragment();
                    final Threadings.ThreadFragment threadFragment2 = new Threadings.ThreadFragment();
                    threadsPool.addFragment(threadFragment1);
                    threadsPool.addFragment(threadFragment2);

                    gameCoordinator.getDownloader().downloadData(imageDetails.getImageOneUrl(), new DownloaderListener() {
                        @Override
                        public void onCallback(byte[] bytes, Status status) {
                            if(status == Status.SUCCESS){
                                image1[0] = processTextureBytes(bytes);
                            }
                            threadFragment1.setFinished(true);
                        }
                    });
                    gameCoordinator.getDownloader().downloadData(imageDetails.getImageTwoUrl(), new DownloaderListener() {
                        @Override
                        public void onCallback(byte[] bytes, Status status) {
                            if(status == Status.SUCCESS){
                                image2[0] = processTextureBytes(bytes);
                            }
                            threadFragment2.setFinished(true);
                        }
                    });

                    while (!threadsPool.allFinished()){
                        Threadings.sleep(100);
                    }

                    if(image1[0] != null && image2[0] != null){
                        int addingIndex = getStartIndexWithHigherOrderIndex(currentOrderIndex);
                        imagePairs.add(addingIndex,
                                new ImagePair(imageDetails, image1[0], image2[0], currentOrderIndex));
                    }
                }
        });

    }

    public Texture processTextureBytes(byte[] textureBytes) {
        if(textureBytes != null){
            try {
                Pixmap pixmap = new Pixmap(textureBytes, 0, textureBytes.length);
                Texture texture = new Texture(pixmap);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                pixmap.dispose();
                return texture;

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return null;
    }

    public void peek(final ImageStorageListener listener){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                while (imagePairs.size() <= 0){
                    Threadings.sleep(300);
                    if(safeThread.isKilled()) return;
                }

                ImagePair first = imagePairs.get(0);
                listener.onPeeked(first);
            }
        });
    }

    public void pop(final ImageStorageListener listener){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                while (imagePairs.size() <= 0){
                    Threadings.sleep(300);
                    if(safeThread.isKilled()) return;
                }

                ImagePair first = imagePairs.get(0);
                imagePairs.remove(first);
                listener.onPopped(first);
            }
        });
    }

    public void pop(final String id, final ImageStorageListener listener){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (getImagePairById(id) == null){
                    Threadings.sleep(300);
                    if(safeThread.isKilled()) return;
                    i++;
                    if(i > 10){     //redownload
                        ArrayList<String> ids = new ArrayList<String>();
                        ids.add(id);
                        receivedDownloadRequest(ids);
                        i = 0;
                    }
                }

                ImagePair first = getImagePairById(id);
                imagePairs.remove(first);
                listener.onPopped(first);
            }
        });
    }

    private ImagePair getImagePairById(String id){
        for(ImagePair imagePair : imagePairs){
            if(imagePair.getImageDetails().getId().equals(id)){
                return imagePair;
            }
        }
        return null;
    }

    private synchronized int getStartIndexWithHigherOrderIndex(int orderIndex){
        int i = 0;
        for(ImagePair imagePair : imagePairs){
            if(imagePair.getOrderIndex() >= orderIndex){
                return i;
            }
            i++;
        }
        return imagePairs.size();
    }

    @Override
    public void dispose() {
        safeThread.kill();
        for(ImagePair pair : imagePairs){
            pair.getImageOne().dispose();
            pair.getImageTwo().dispose();
        }
        imagePairs.clear();
    }

    public ArrayList<ImagePair> getImagePairs() {
        return imagePairs;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    public void setDownloadPeriod(long downloadPeriod) {
        this.downloadPeriod = downloadPeriod;
    }
}
