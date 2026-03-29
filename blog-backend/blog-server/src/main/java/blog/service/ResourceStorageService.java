package blog.service;

import org.springframework.web.multipart.MultipartFile;

public interface ResourceStorageService
{
    default String uploadImage(MultipartFile file)
    {
        return uploadResource(file, "images");
    }

    String uploadResource(MultipartFile file, String folder);

    void deleteByReference(String reference);
}
