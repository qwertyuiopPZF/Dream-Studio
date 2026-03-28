package blog.service.impl;

import blog.dto.SiteSettingsDTO;
import blog.mapper.SystemConfigMapper;
import blog.service.SiteSettingsService;
import blog.vo.SiteSettingsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SiteSettingsServiceImpl implements SiteSettingsService
{
    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public SiteSettingsVO getSiteSettings()
    {
        Map<String, String> valueMap = toValueMap(systemConfigMapper.selectSiteSettings());
        return new SiteSettingsVO(
                valueMap.getOrDefault("site.name", "Dream Studio"),
                valueMap.getOrDefault("site.description", ""),
                valueMap.getOrDefault("site.keywords", ""),
                valueMap.getOrDefault("site.author", ""),
                valueMap.getOrDefault("site.icp", "")
        );
    }

    @Override
    public SiteSettingsVO updateSiteSettings(SiteSettingsDTO siteSettingsDTO)
    {
        systemConfigMapper.updateConfig("site.name", safeValue(siteSettingsDTO.getName()));
        systemConfigMapper.updateConfig("site.description", safeValue(siteSettingsDTO.getDescription()));
        systemConfigMapper.updateConfig("site.keywords", safeValue(siteSettingsDTO.getKeywords()));
        systemConfigMapper.updateConfig("site.author", safeValue(siteSettingsDTO.getAuthor()));
        systemConfigMapper.updateConfig("site.icp", safeValue(siteSettingsDTO.getIcp()));
        return getSiteSettings();
    }

    private Map<String, String> toValueMap(List<Map<String, String>> rows)
    {
        Map<String, String> valueMap = new HashMap<>();
        for (Map<String, String> row : rows) {
            valueMap.put(row.get("configKey"), row.get("configValue"));
        }
        return valueMap;
    }

    private String safeValue(String value)
    {
        return value == null ? "" : value.trim();
    }
}
