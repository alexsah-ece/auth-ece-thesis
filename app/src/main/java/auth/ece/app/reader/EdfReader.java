package auth.ece.app.reader;

import auth.ece.app.model.DatasetMetric;
import auth.ece.app.model.EdfMetric;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.log4j.Log4j2;

import java.io.Reader;
import java.util.Iterator;

@Log4j2
public class EdfReader {

    private int startDayIndex;

    public EdfReader(int startDayIndex) {
        if (startDayIndex < 0) {
            log.warn("Start day index provided is negative. Reading from 1st day");
            this.startDayIndex = 0;
        } else {
            this.startDayIndex = startDayIndex;
        }
    }

    public Iterator<DatasetMetric> readFile(Reader reader) {
        return readSome(reader);
    }

    private Iterator<DatasetMetric> readSome(Reader reader) {
        CsvToBean<DatasetMetric> csvReader = new CsvToBeanBuilder(reader)
                .withSkipLines(calculateLinesToSkip())
                .withType(EdfMetric.class)
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        Iterator<DatasetMetric> iterator = csvReader.iterator();
        return iterator;
    }

    /**
     * Calculates how many lines to skip on the CSV file, based on the provided day offset.
     * For EDF dataset, each line is 1 minute, so we can calculate 1440 as one day. The first line
     * is the header, so we always skip it.
     * @return lines to skip on csv read
     */
    private int calculateLinesToSkip() {
        return 1440 * startDayIndex + 1;
    }
}
