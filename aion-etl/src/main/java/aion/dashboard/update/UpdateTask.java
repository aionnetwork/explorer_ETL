package aion.dashboard.update;

import aion.dashboard.domainobject.UpdateState;
import aion.dashboard.service.UpdateStateServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public abstract class UpdateTask<T> implements Runnable{


    private final int id;
    protected final UpdateStateServiceImpl updateStateService = UpdateStateServiceImpl.getInstance();
    private UpdateState state;
    protected static final Logger GENERAL= LoggerFactory.getLogger("logger_general");
    protected UpdateTask(int id){
        this.id = id;
    }


    protected abstract List<T> readForBlock(long block, long end) throws Exception;

    protected abstract void writeUpdate(UpdateState state, List<T> ts) throws Exception;

    private UpdateState getState () {
        return updateStateService.find(id).orElse(null);
    }


    @Override
    public void run(){
        try {
            state = Objects.requireNonNull(getState());
            Thread.currentThread().setName("update-"+state.getTableName());

            while (state.isRunUpdate() && state.getEnd() >= state.getStart()){
                GENERAL.info("updating block {}", state.getStart());

                final var blockNumber = state.getStart();
                var res = readForBlock(blockNumber, blockNumber + 999);

                final UpdateState updateState = UpdateState.builder().setRunUpdate(state.isRunUpdate())
                        .setTableName(state.getTableName())
                        .setId(state.getId())
                        .setStart(state.getStart() + 1000)
                        .setEnd(state.getEnd()).createUpdateState();

                doWrite(blockNumber, res, updateState);

            }
            GENERAL.info("Completed update on: {}", state.getTableName());

        }catch (Exception e){
            GENERAL.warn("Failed to perform update for table with id: {}", id);
            GENERAL.warn("Error",e);
        }
    }

    private void doWrite(long blockNumber, List<T> res, UpdateState updateState) {
        try {
            writeUpdate(updateState, res);
            GENERAL.info("Successfully updated block: {}" ,blockNumber );
            state = updateState;
        }catch (Exception e){
            GENERAL.error(String.format("Caught an exception while updating %s:", state.getTableName()), e);
        }
    }

}
