package ir.daneshrefah.scm.observation.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "scm.observation")
public class ObservationProperties {
    private boolean enabled;
    private FileStorageProperties file = new FileStorageProperties();
    private LogProperties log = new LogProperties();
    private TraceProperties trace = new TraceProperties();
    private AuditProperties audit = new AuditProperties();
    private MetricProperties metric = new MetricProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FileStorageProperties getFile() {
        return file;
    }

    public void setFile(FileStorageProperties file) {
        this.file = file == null ? new FileStorageProperties() : file;
    }

    public LogProperties getLog() {
        return log;
    }

    public void setLog(LogProperties log) {
        this.log = log == null ? new LogProperties() : log;
    }

    public TraceProperties getTrace() {
        return trace;
    }

    public void setTrace(TraceProperties trace) {
        this.trace = trace == null ? new TraceProperties() : trace;
    }

    public AuditProperties getAudit() {
        return audit;
    }

    public void setAudit(AuditProperties audit) {
        this.audit = audit == null ? new AuditProperties() : audit;
    }

    public MetricProperties getMetric() {
        return metric;
    }

    public void setMetric(MetricProperties metric) {
        this.metric = metric == null ? new MetricProperties() : metric;
    }

    public static class LogProperties {
        private boolean enabled;
        private ConsoleProperties console = new ConsoleProperties();
        private FileSinkProperties file = new FileSinkProperties();
        private RollingProperties rolling = new RollingProperties();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public ConsoleProperties getConsole() {
            return console;
        }

        public void setConsole(ConsoleProperties console) {
            this.console = console == null ? new ConsoleProperties() : console;
        }

        public FileSinkProperties getFile() {
            return file;
        }

        public void setFile(FileSinkProperties file) {
            this.file = file == null ? new FileSinkProperties() : file;
        }

        public RollingProperties getRolling() {
            return rolling;
        }

        public void setRolling(RollingProperties rolling) {
            this.rolling = rolling == null ? new RollingProperties() : rolling;
        }

    }

    public static class TraceProperties {
        private boolean enabled;
        private ConsoleProperties console = new ConsoleProperties();
        private FileSinkProperties file = new FileSinkProperties();
        private RollingProperties rolling = new RollingProperties();
        private AsyncProperties async = new AsyncProperties();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public ConsoleProperties getConsole() {
            return console;
        }

        public void setConsole(ConsoleProperties console) {
            this.console = console == null ? new ConsoleProperties() : console;
        }

        public FileSinkProperties getFile() {
            return file;
        }

        public void setFile(FileSinkProperties file) {
            this.file = file == null ? new FileSinkProperties() : file;
        }

        public RollingProperties getRolling() {
            return rolling;
        }

        public void setRolling(RollingProperties rolling) {
            this.rolling = rolling == null ? new RollingProperties() : rolling;
        }

        public AsyncProperties getAsync() {
            return async;
        }

        public void setAsync(AsyncProperties async) {
            this.async = async == null ? new AsyncProperties() : async;
        }
    }

    public static class AuditProperties {
        private boolean enabled;
        private ConsoleProperties console = new ConsoleProperties();
        private FileSinkProperties file = new FileSinkProperties();
        private RollingProperties rolling = new RollingProperties();
        private AsyncProperties async = new AsyncProperties();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public ConsoleProperties getConsole() {
            return console;
        }

        public void setConsole(ConsoleProperties console) {
            this.console = console == null ? new ConsoleProperties() : console;
        }

        public FileSinkProperties getFile() {
            return file;
        }

        public void setFile(FileSinkProperties file) {
            this.file = file == null ? new FileSinkProperties() : file;
        }

        public RollingProperties getRolling() {
            return rolling;
        }

        public void setRolling(RollingProperties rolling) {
            this.rolling = rolling == null ? new RollingProperties() : rolling;
        }

        public AsyncProperties getAsync() {
            return async;
        }

        public void setAsync(AsyncProperties async) {
            this.async = async == null ? new AsyncProperties() : async;
        }
    }

    public static class MetricProperties {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ConsoleProperties {
        private boolean enabled;
        private String format = "simple";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }

    public static class FileSinkProperties {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

    public static class FileStorageProperties {
        private Path rootDirectory;
        private String archiveDirectoryName;

        public Path getRootDirectory() {
            return rootDirectory;
        }

        public void setRootDirectory(Path rootDirectory) {
            this.rootDirectory = rootDirectory;
        }

        public String getArchiveDirectoryName() {
            return archiveDirectoryName;
        }

        public void setArchiveDirectoryName(String archiveDirectoryName) {
            this.archiveDirectoryName = archiveDirectoryName;
        }
    }

    public static class RollingProperties {
        private String maxFileSize;
        private Integer maxHistory;
        private String totalSizeCap;
        private boolean cleanHistoryOnStart;

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public Integer getMaxHistory() {
            return maxHistory;
        }

        public void setMaxHistory(Integer maxHistory) {
            this.maxHistory = maxHistory;
        }

        public String getTotalSizeCap() {
            return totalSizeCap;
        }

        public void setTotalSizeCap(String totalSizeCap) {
            this.totalSizeCap = totalSizeCap;
        }

        public boolean isCleanHistoryOnStart() {
            return cleanHistoryOnStart;
        }

        public void setCleanHistoryOnStart(boolean cleanHistoryOnStart) {
            this.cleanHistoryOnStart = cleanHistoryOnStart;
        }
    }

    public static class AsyncProperties {
        private boolean enabled;
        private Integer queueSize;
        private Integer discardingThreshold;
        private Boolean neverBlock;
        private Integer maxFlushTime;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(Integer queueSize) {
            this.queueSize = queueSize;
        }

        public Integer getDiscardingThreshold() {
            return discardingThreshold;
        }

        public void setDiscardingThreshold(Integer discardingThreshold) {
            this.discardingThreshold = discardingThreshold;
        }

        public Boolean getNeverBlock() {
            return neverBlock;
        }

        public void setNeverBlock(Boolean neverBlock) {
            this.neverBlock = neverBlock;
        }

        public Integer getMaxFlushTime() {
            return maxFlushTime;
        }

        public void setMaxFlushTime(Integer maxFlushTime) {
            this.maxFlushTime = maxFlushTime;
        }
    }

}
