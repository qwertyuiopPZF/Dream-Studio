package blog.mapper;

import blog.entity.SiteResource;
import blog.vo.ResourceVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResourceMapper
{
    @Insert("""
            INSERT INTO site_resource(uploader_id, original_name, file_url, file_size, extension, category_key, mime_type, status, review_note, reviewer_id, download_count, create_time, update_time, review_time)
            VALUES(#{uploaderId}, #{originalName}, #{fileUrl}, #{fileSize}, #{extension}, #{categoryKey}, #{mimeType}, #{status}, #{reviewNote}, #{reviewerId}, #{downloadCount}, #{createTime}, #{updateTime}, #{reviewTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SiteResource resource);

    @Select("SELECT * FROM site_resource WHERE id = #{id} LIMIT 1")
    SiteResource selectEntityById(Long id);

    @Select("""
            SELECT sr.id,
                   sr.uploader_id AS uploaderId,
                   COALESCE(NULLIF(uploader.nickname, ''), uploader.username, 'Dream 用户') AS uploaderName,
                   uploader.avatar AS uploaderAvatar,
                   sr.original_name AS originalName,
                   sr.file_url AS fileUrl,
                   sr.file_size AS fileSize,
                   sr.extension,
                   sr.category_key AS categoryKey,
                   sr.mime_type AS mimeType,
                   sr.status,
                   sr.review_note AS reviewNote,
                   sr.reviewer_id AS reviewerId,
                   COALESCE(NULLIF(reviewer.nickname, ''), reviewer.username) AS reviewerName,
                   sr.download_count AS downloadCount,
                   sr.create_time AS createTime,
                   sr.update_time AS updateTime,
                   sr.review_time AS reviewTime
            FROM site_resource sr
            LEFT JOIN user_account uploader ON uploader.id = sr.uploader_id
            LEFT JOIN user_account reviewer ON reviewer.id = sr.reviewer_id
            WHERE sr.id = #{id}
            LIMIT 1
            """)
    ResourceVO selectById(Long id);

    @Select("""
            SELECT sr.id,
                   sr.uploader_id AS uploaderId,
                   COALESCE(NULLIF(uploader.nickname, ''), uploader.username, 'Dream 用户') AS uploaderName,
                   uploader.avatar AS uploaderAvatar,
                   sr.original_name AS originalName,
                   sr.file_url AS fileUrl,
                   sr.file_size AS fileSize,
                   sr.extension,
                   sr.category_key AS categoryKey,
                   sr.mime_type AS mimeType,
                   sr.status,
                   sr.review_note AS reviewNote,
                   sr.reviewer_id AS reviewerId,
                   COALESCE(NULLIF(reviewer.nickname, ''), reviewer.username) AS reviewerName,
                   sr.download_count AS downloadCount,
                   sr.create_time AS createTime,
                   sr.update_time AS updateTime,
                   sr.review_time AS reviewTime
            FROM site_resource sr
            LEFT JOIN user_account uploader ON uploader.id = sr.uploader_id
            LEFT JOIN user_account reviewer ON reviewer.id = sr.reviewer_id
            WHERE sr.status = 'APPROVED'
            ORDER BY COALESCE(sr.review_time, sr.create_time) DESC, sr.id DESC
            LIMIT 240
            """)
    List<ResourceVO> selectPublicList();

    @Select("""
            SELECT sr.id,
                   sr.uploader_id AS uploaderId,
                   COALESCE(NULLIF(uploader.nickname, ''), uploader.username, 'Dream 用户') AS uploaderName,
                   uploader.avatar AS uploaderAvatar,
                   sr.original_name AS originalName,
                   sr.file_url AS fileUrl,
                   sr.file_size AS fileSize,
                   sr.extension,
                   sr.category_key AS categoryKey,
                   sr.mime_type AS mimeType,
                   sr.status,
                   sr.review_note AS reviewNote,
                   sr.reviewer_id AS reviewerId,
                   COALESCE(NULLIF(reviewer.nickname, ''), reviewer.username) AS reviewerName,
                   sr.download_count AS downloadCount,
                   sr.create_time AS createTime,
                   sr.update_time AS updateTime,
                   sr.review_time AS reviewTime
            FROM site_resource sr
            LEFT JOIN user_account uploader ON uploader.id = sr.uploader_id
            LEFT JOIN user_account reviewer ON reviewer.id = sr.reviewer_id
            WHERE sr.uploader_id = #{uploaderId}
            ORDER BY CASE sr.status WHEN 'PENDING' THEN 0 WHEN 'REJECTED' THEN 1 ELSE 2 END,
                     sr.create_time DESC,
                     sr.id DESC
            LIMIT 240
            """)
    List<ResourceVO> selectByUploaderId(Long uploaderId);

    @Select("""
            <script>
            SELECT sr.id,
                   sr.uploader_id AS uploaderId,
                   COALESCE(NULLIF(uploader.nickname, ''), uploader.username, 'Dream 用户') AS uploaderName,
                   uploader.avatar AS uploaderAvatar,
                   sr.original_name AS originalName,
                   sr.file_url AS fileUrl,
                   sr.file_size AS fileSize,
                   sr.extension,
                   sr.category_key AS categoryKey,
                   sr.mime_type AS mimeType,
                   sr.status,
                   sr.review_note AS reviewNote,
                   sr.reviewer_id AS reviewerId,
                   COALESCE(NULLIF(reviewer.nickname, ''), reviewer.username) AS reviewerName,
                   sr.download_count AS downloadCount,
                   sr.create_time AS createTime,
                   sr.update_time AS updateTime,
                   sr.review_time AS reviewTime
            FROM site_resource sr
            LEFT JOIN user_account uploader ON uploader.id = sr.uploader_id
            LEFT JOIN user_account reviewer ON reviewer.id = sr.reviewer_id
            <where>
                <if test="status != null and status != ''">
                    sr.status = #{status}
                </if>
            </where>
            ORDER BY CASE sr.status WHEN 'PENDING' THEN 0 WHEN 'REJECTED' THEN 1 ELSE 2 END,
                     sr.create_time DESC,
                     sr.id DESC
            LIMIT 240
            </script>
            """)
    List<ResourceVO> selectAdminList(@Param("status") String status);

    @Update("""
            UPDATE site_resource
            SET status = #{status},
                review_note = #{reviewNote},
                reviewer_id = #{reviewerId},
                update_time = #{updateTime},
                review_time = #{reviewTime}
            WHERE id = #{id}
            """)
    void updateReview(SiteResource resource);

    @Update("UPDATE site_resource SET download_count = download_count + 1, update_time = NOW() WHERE id = #{id} AND status = 'APPROVED'")
    void incrementDownloadCount(Long id);

    @Delete("DELETE FROM site_resource WHERE id = #{id}")
    void deleteById(Long id);
}
