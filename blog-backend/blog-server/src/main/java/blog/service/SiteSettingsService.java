package blog.service;

import blog.dto.SiteSettingsDTO;
import blog.vo.SiteSettingsVO;

public interface SiteSettingsService
{
    SiteSettingsVO getSiteSettings();

    SiteSettingsVO updateSiteSettings(SiteSettingsDTO siteSettingsDTO);
}
