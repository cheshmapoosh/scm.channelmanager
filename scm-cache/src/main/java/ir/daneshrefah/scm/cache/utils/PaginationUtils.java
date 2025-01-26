package ir.daneshrefah.scm.cache.utils;

import ir.daneshrefah.scm.common.exception.InvalidInputException;

import java.util.Collections;
import java.util.List;

public class PaginationUtils {

    private PaginationUtils() {}

    public static <T> List<T> getListByPagination(List<T> sourceList, int pageNo, int pageSize) {
        if (pageSize <= 0 || pageNo <= 0) {
            throw new InvalidInputException("invalid page size: " + pageSize);
        }

        int fromIndex = (pageNo - 1) * pageSize;

        if (sourceList == null || sourceList.size() <= fromIndex) {
            return Collections.emptyList();
        }
        return sourceList.subList(fromIndex, Math.min(fromIndex + pageSize, sourceList.size()));
    }
}
