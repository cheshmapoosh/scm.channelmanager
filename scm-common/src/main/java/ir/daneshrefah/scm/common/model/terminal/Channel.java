package ir.daneshrefah.scm.common.model.terminal;


import ir.daneshrefah.scm.common.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Channel extends BaseModel<String> {

    private String code;
    private String title;
    private String channelClassName;
    private String metadata;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChannelClassName() {
        return channelClassName;
    }

    public void setChannelClassName(String channelClassName) {
        this.channelClassName = channelClassName;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

}
