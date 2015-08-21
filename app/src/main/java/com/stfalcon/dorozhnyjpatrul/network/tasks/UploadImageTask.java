package com.stfalcon.dorozhnyjpatrul.network.tasks;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.stfalcon.dorozhnyjpatrul.models.PhotoAnswer;
import com.stfalcon.dorozhnyjpatrul.network.HPatrulAPI;

import java.io.File;

import retrofit.mime.TypedFile;

/**
 * Created by alexandr on 18/08/15.
 */
public class UploadImageTask extends RetrofitSpiceRequest<PhotoAnswer, HPatrulAPI> {

    private final int photoID;
    private String fileUrl;
    private String id;

    public UploadImageTask(int userID, int photoID, String fileUrl) {
        super(PhotoAnswer.class, HPatrulAPI.class);
        this.fileUrl = fileUrl;
        this.id = String.valueOf(userID);
        this.photoID = photoID;
    }

    @Override
    public PhotoAnswer loadDataFromNetwork() {
        TypedFile file = new TypedFile("multipart/form-data", new File(fileUrl));
        //PhotoAnswer photoData = getService().uploadImage(file);
        PhotoAnswer photoData = getService().uploadImage(file, id);
        photoData.setId(photoID);
        return photoData;
    }
}
