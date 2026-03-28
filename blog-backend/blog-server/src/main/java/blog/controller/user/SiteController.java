package blog.controller.user;

import blog.result.Result;
import blog.service.AnnouncementService;
import blog.service.SiteSettingsService;
import blog.vo.AnnouncementVO;
import blog.vo.SiteSettingsVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/site")
public class SiteController
{
    @Autowired
    private SiteSettingsService siteSettingsService;

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/info")
    @ApiOperation("获取站点信息")
    public Result<SiteSettingsVO> getSiteInfo()
    {
        return Result.success(siteSettingsService.getSiteSettings());
    }

    @GetMapping("/announcements")
    @ApiOperation("获取已发布公告")
    public Result<List<AnnouncementVO>> getAnnouncements()
    {
        return Result.success(announcementService.listAnnouncements(true));
    }
}
