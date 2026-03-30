package com.authServer.shared.tools;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Getter
@Setter
@Builder
public class DiskBasedMultipartFile implements MultipartFile{

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final File file;

    public DiskBasedMultipartFile(
            String name,
            String originalFilename,
            String contentType,
            File file
    ){
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.file = file;
    }

    @Override
    @NonNull
    public String getName(){
        return this.name;
    }

    @Override
    public String getOriginalFilename(){
        return this.originalFilename;
    }

    @Override
    public String getContentType(){
        return this.contentType;
    }

    @Override
    public boolean isEmpty(){
        return file == null || file.length() == 0;
    }

    @Override
    public long getSize(){
        return file.length();
    }

    @Override
    public byte @NonNull [] getBytes() throws IOException {
        return java.nio.file.Files.readAllBytes(file.toPath());
    }

    @Override
    @NonNull
    public InputStream getInputStream () throws IOException{
        return new FileInputStream(this.file);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException{
        try (
                InputStream inputStream = getInputStream();
                OutputStream outputStream = new FileOutputStream(dest)
        ) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, bytesRead);
        }
    }
}
