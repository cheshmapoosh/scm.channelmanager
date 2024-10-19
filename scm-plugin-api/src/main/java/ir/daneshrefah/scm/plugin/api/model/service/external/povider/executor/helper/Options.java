package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper;

public class Options {
    private final StringBuilder options = new StringBuilder("?");
    private Options(){}
    public static Options create(){
        return new Options();
    }
    public Options set(NettyOptions option, String value){
        options.append(option.getValue()).append("=").append(value).append("&");
        return this;
    }

    public Options set(NettyOptions option, Boolean value){
        options.append(option.getValue()).append("=").append(value).append("&");
        return this;
    }

    public String build(){
        return options.substring(0, options.toString().length() - 1);
    }
}
