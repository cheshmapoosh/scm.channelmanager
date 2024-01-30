package ir.daneshrefah.scm.common.dto;

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

    private Integer pageNo = 1;
    private Integer pageSize = 10;

}
