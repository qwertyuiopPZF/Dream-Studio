package blog.service;

import blog.dto.ResourceReviewDTO;
import blog.vo.ResourceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService
{
    List<ResourceVO> listPublicResources();

    List<ResourceVO> listMyResources(String username);

    List<ResourceVO> listAdminResources(String username, String status);

    ResourceVO uploadResource(String username, MultipartFile file);

    ResourceVO reviewResource(String username, Long resourceId, ResourceReviewDTO request);

    void deleteResource(String username, Long resourceId);

    void recordDownload(Long resourceId);
}
