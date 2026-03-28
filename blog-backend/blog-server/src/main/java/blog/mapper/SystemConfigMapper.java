package blog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SystemConfigMapper
{
    @Select("SELECT config_key as configKey, config_value as configValue FROM system_config WHERE config_key IN ('site.name', 'site.description', 'site.keywords', 'site.author', 'site.icp')")
    List<Map<String, String>> selectSiteSettings();

    @Update("UPDATE system_config SET config_value = #{configValue} WHERE config_key = #{configKey}")
    void updateConfig(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
