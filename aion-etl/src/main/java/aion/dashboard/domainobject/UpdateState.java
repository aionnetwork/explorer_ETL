package aion.dashboard.domainobject;

public class UpdateState {

    private final int id;
    private final String tableName;
    private boolean runUpdate;
    private long start;
    private long end;


    private UpdateState(int id, String tableName, boolean runUpdate, long start, long end) {
        this.id = id;
        this.tableName = tableName;
        this.runUpdate = runUpdate;
        this.start = start;
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isRunUpdate() {
        return runUpdate;
    }

    public UpdateState setRunUpdate(boolean runUpdate) {
        this.runUpdate = runUpdate;
        return this;
    }

    public long getStart() {
        return start;
    }

    public UpdateState setStart(long start) {
        this.start = start;
        return this;
    }

    public long getEnd() {
        return end;
    }

    public UpdateState setEnd(long end) {
        this.end = end;
        return this;
    }

    private static final ThreadLocal<UpdateStateBuilder> builder = ThreadLocal.withInitial(UpdateStateBuilder::new);
    
    public static UpdateStateBuilder builder(){return builder.get();}
    public static class UpdateStateBuilder {
        private int id;
        private String tableName;
        private boolean runUpdate;
        private long start;
        private long end;
        private UpdateStateBuilder(){}
        public UpdateStateBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public UpdateStateBuilder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public UpdateStateBuilder setRunUpdate(boolean runUpdate) {
            this.runUpdate = runUpdate;
            return this;
        }

        public UpdateStateBuilder setStart(long start) {
            this.start = start;
            return this;
        }

        public UpdateStateBuilder setEnd(long end) {
            this.end = end;
            return this;
        }

        public UpdateState createUpdateState() {
            return new UpdateState(id, tableName, runUpdate, start, end);
        }
    }
}
