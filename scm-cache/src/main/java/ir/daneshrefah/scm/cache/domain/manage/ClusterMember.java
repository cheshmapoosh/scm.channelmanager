package ir.daneshrefah.scm.cache.domain.manage;

import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-01
 */
@Data
public class ClusterMember {

    private String id;
    private String address;
    private boolean localMember;
    private boolean liteMember;
    private String version;

}
