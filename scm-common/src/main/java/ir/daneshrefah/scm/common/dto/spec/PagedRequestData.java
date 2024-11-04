package ir.daneshrefah.scm.common.dto.spec;

import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-30
 */
@Data
public class PagedRequestData implements RequestData {

    public static final int DEFAULT_PAGE_NO = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private Integer pageNo = DEFAULT_PAGE_NO;
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public void setPageNo(Integer pageNo) {
        this.pageNo = null != pageNo && pageNo > 0 ? pageNo : DEFAULT_PAGE_NO;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = null != pageSize && pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
    }
}
