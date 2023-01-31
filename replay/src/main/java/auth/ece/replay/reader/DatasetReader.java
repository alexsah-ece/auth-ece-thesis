package auth.ece.replay.reader;

import auth.ece.replay.model.DatasetMetric;
import lombok.extern.log4j.Log4j2;

import java.io.Reader;
import java.util.Iterator;

@Log4j2
public abstract class DatasetReader {

    protected int startDayIndex;

    public DatasetReader(int startDayIndex) {
        setStartDayIndex(startDayIndex);
    }

    private void setStartDayIndex(int startDayIndex) {
        if (startDayIndex < 0) {
            log.warn("Start day index provided is negative. Reading from 1st day");
            this.startDayIndex = 0;
        } else {
            this.startDayIndex = startDayIndex;
        }
    }

    /**
     * Calculates how many lines to skip on the CSV file, based on the provided day offset.
     * @return the number of lines to skip in the file to end up starting from the given offset
     */
    protected abstract int calculateLinesToSkip();

    public abstract Iterator<DatasetMetric> readFile(Reader reader);
}
