package blog.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果类
 *
 * 用于封装分页查询的响应数据，包含分页信息和数据列表
 *
 * @param <T> 数据列表的元素类型
 * @author Eleven
 * @version 1.0
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    // ========== 新增字段 ==========

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页条数
     */
    private Integer size;

    // ========== 原有构造方法 ==========

    /**
     * 快捷构造方法
     *
     * @param total 总记录数
     * @param records 当前页数据列表
     */
    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    /**
     * 快捷构造方法（支持int类型的total）
     *
     * @param total 总记录数
     * @param records 当前页数据列表
     */
    public PageResult(int total, List<T> records) {
        this.total = (long) total;
        this.records = records;
    }

    // ========== 新增构造方法 ==========

    /**
     * 完整构造方法（包含分页信息）
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param total 总记录数
     * @param records 当前页数据列表
     */
    public PageResult(Integer page, Integer size, Long total, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.records = records;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 静态工厂方法 - 创建分页结果（带分页信息）
     */
    public static <T> PageResult<T> of(Integer page, Integer size, Long total, List<T> records) {
        return new PageResult<>(page, size, total, records);
    }

    /**
     * 静态工厂方法 - 创建分页结果（不带分页信息，保持原有用法）
     */
    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }

    /**
     * 静态工厂方法 - 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L, List.of());
    }
}
