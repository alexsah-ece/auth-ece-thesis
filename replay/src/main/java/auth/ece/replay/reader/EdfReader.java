package auth.ece.replay.reader;

import auth.ece.replay.model.DatasetMetric;
import auth.ece.replay.model.EdfMetric;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.log4j.Log4j2;

import java.io.Reader;
import java.util.Iterator;

@Log4j2
public class EdfReader extends DatasetReader {

    public EdfReader(int startDayIndex) {
        super(startDayIndex);
    }

    @Override
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
     * For EDF dataset, each line is 1 minute, so we can calculate 1440 as one day. The first line
     * is the header, so we always skip it.
     * @return lines to skip on csv read
     */
    @Override
    protected int calculateLinesToSkip() {
        return 1440 * startDayIndex + 1;
    }
}
