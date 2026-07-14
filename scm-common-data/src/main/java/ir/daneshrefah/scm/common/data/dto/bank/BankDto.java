package ir.daneshrefah.scm.common.data.dto.bank;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class BankDto implements Serializable {
    private Long id;

    private String name;

    private String iin;

    private String imageUrl;
}

