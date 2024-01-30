package ir.daneshrefah.scm.common.dto;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-30
 */
@Getter
public class PagedResponseData<T> implements ResponseData {

    private Integer pageNo;
    private Integer pageSize;
    private Integer totalCount;
    private Integer totalPage;
    private List<T> data;

    public PagedResponseData(Integer pageNo, Integer pageSize, List<T> data) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = data.size();
        this.totalPage = (totalCount + pageSize - 1) / pageSize;
        int skip = (pageNo - 1) * pageSize;
        this.data = data.stream().skip(skip).limit(pageSize).collect(Collectors.toList());
    }

}
