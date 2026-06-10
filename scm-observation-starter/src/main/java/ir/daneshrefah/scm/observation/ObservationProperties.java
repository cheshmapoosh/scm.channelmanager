package ir.daneshrefah.scm.observation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "scm.observation")
public class ObservationProperties {
    private boolean enabled;
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
        private FileProperties file = new FileProperties();
        private RollingProperties rolling = new RollingProperties();
        private LevelProperties level = new LevelProperties();

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

        public FileProperties getFile() {
            return file;
        }

        public void setFile(FileProperties file) {
            this.file = file == null ? new FileProperties() : file;
        }

        public RollingProperties getRolling() {
            return rolling;
        }

        public void setRolling(RollingProperties rolling) {
            this.rolling = rolling == null ? new RollingProperties() : rolling;
        }

        public LevelProperties getLevel() {
            return level;
        }

        public void setLevel(LevelProperties level) {
            this.level = level == null ? new LevelProperties() : level;
        }
    }

    public static class TraceProperties {
        private boolean enabled;
        private FileProperties file = new FileProperties();
        private RollingProperties rolling = new RollingProperties();
        private AsyncProperties async = new AsyncProperties();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public FileProperties getFile() {
            return file;
        }

        public void setFile(FileProperties file) {
            this.file = file == null ? new FileProperties() : file;
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
        private FileProperties file = new FileProperties();
        private RollingProperties rolling = new RollingProperties();
        private AsyncProperties async = new AsyncProperties();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public FileProperties getFile() {
            return file;
        }

        public void setFile(FileProperties file) {
            this.file = file == null ? new FileProperties() : file;
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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class FileProperties {
        private boolean enabled;
        private Path directory;
        private String fileName;
        private Path archiveDirectory;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Path getDirectory() {
            return directory;
        }

        public void setDirectory(Path directory) {
            this.directory = directory;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public Path getArchiveDirectory() {
            return archiveDirectory;
        }

        public void setArchiveDirectory(Path archiveDirectory) {
            this.archiveDirectory = archiveDirectory;
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

    public static class LevelProperties {
        private String root = "INFO";
        private String application = "INFO";
        private String spring = "INFO";
        private String hibernate = "WARN";
        private String hazelcast = "INFO";

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        public String getSpring() {
            return spring;
        }

        public void setSpring(String spring) {
            this.spring = spring;
        }

        public String getHibernate() {
            return hibernate;
        }

        public void setHibernate(String hibernate) {
            this.hibernate = hibernate;
        }

        public String getHazelcast() {
            return hazelcast;
        }

        public void setHazelcast(String hazelcast) {
            this.hazelcast = hazelcast;
        }
    }
}
