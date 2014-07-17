package Events;

import org.apache.commons.exec.ExecuteWatchdog;


public class JobStartedEvent {

    private ExecuteWatchdog watchdog;
    private String jobid;

    public JobStartedEvent(ExecuteWatchdog watchdog,String jobid) {
        this.watchdog = watchdog;
        this.jobid = jobid;
    }

    public ExecuteWatchdog getWatchdog() {
        return this.watchdog;
    }

    /**
     * @return the jobid
     */
    public String getJobid() {
        return jobid;
    }

}
