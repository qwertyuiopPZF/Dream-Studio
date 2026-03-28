package blog.mapper;

import blog.entity.Announcement;
import blog.vo.AnnouncementVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper
{
    @Insert("INSERT INTO announcement(author_id, title, content, status, publish_time, create_time, update_time) VALUES(#{authorId}, #{title}, #{content}, #{status}, #{publishTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Announcement announcement);

    @Select("SELECT a.id, a.author_id as authorId, a.title, a.content, a.status, a.publish_time as publishTime, a.create_time as createTime, a.update_time as updateTime, ua.nickname as authorNickname FROM announcement a LEFT JOIN user_account ua ON a.author_id = ua.id ORDER BY COALESCE(a.publish_time, a.create_time) DESC")
    List<AnnouncementVO> selectAll();

    @Select("SELECT a.id, a.author_id as authorId, a.title, a.content, a.status, a.publish_time as publishTime, a.create_time as createTime, a.update_time as updateTime, ua.nickname as authorNickname FROM announcement a LEFT JOIN user_account ua ON a.author_id = ua.id WHERE a.status = 1 ORDER BY COALESCE(a.publish_time, a.create_time) DESC")
    List<AnnouncementVO> selectPublished();

    @Select("SELECT a.id, a.author_id as authorId, a.title, a.content, a.status, a.publish_time as publishTime, a.create_time as createTime, a.update_time as updateTime, ua.nickname as authorNickname FROM announcement a LEFT JOIN user_account ua ON a.author_id = ua.id WHERE a.id = #{id} LIMIT 1")
    AnnouncementVO selectById(Long id);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    void deleteById(Long id);
}
