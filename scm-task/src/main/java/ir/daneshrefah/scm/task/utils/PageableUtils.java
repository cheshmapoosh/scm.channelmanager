package ir.daneshrefah.scm.task.utils;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageableUtils {

    private PageableUtils() {}

    public static Pageable getPageable(PagedRequestData requestData) {
        return PageRequest.of(Math.max(requestData.getPageNo() - 1, 0), requestData.getPageSize());
    }
}
