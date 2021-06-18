package com.tave7.dobdob.data;

import android.graphics.Bitmap;

import java.io.File;

public class PhotoInfo {
    private boolean isNew = true;
    private File photoFile = null;
    private Bitmap photoBM;

    public PhotoInfo(Bitmap photoBM) {    //글 수정 시에 해당 글의 사진이 존재할 시의 생성자
        isNew = false;
        this.photoBM = photoBM;
    }

    public PhotoInfo (File photoFile, Bitmap photoBM) {    //글 수정 시에 해당 글의 사진이 존재할 시의 생성자
        this.photoFile = photoFile;
        this.photoBM = photoBM;
    }

    public boolean getIsNew() { return isNew; }
    public File getPhotoFile() { return photoFile; }
    public Bitmap getPhotoBM() { return photoBM; }
}
