package blog.service;

import org.springframework.web.multipart.MultipartFile;

public interface ResourceStorageService
{
    String uploadImage(MultipartFile file);

    void deleteByReference(String reference);
}
