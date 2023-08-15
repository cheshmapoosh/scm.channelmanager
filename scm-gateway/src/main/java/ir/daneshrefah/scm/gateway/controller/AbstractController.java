package ir.daneshrefah.scm.gateway.controller;

import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-15
 */
public class AbstractController {

    protected <T> PagingResponse<T> createPagingResponse(List<T> data, Pageable pageable, long totalItems) {
        int currentPage = pageable.getPageNumber();
        int totalPages = (int) Math.ceil((double) totalItems / pageable.getPageSize());

        PagingResponse<T> response = new PagingResponse<>();
        response.setData(data);
        response.setCurrentPage(currentPage);
        response.setTotalPage(totalPages);
        response.setTotalItems(totalItems);

        return response;
    }

}
