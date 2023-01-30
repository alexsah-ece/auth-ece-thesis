package auth.ece.app.reader;

import auth.ece.app.model.AMPds2Metric;
import auth.ece.app.model.DatasetMetric;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.Reader;
import java.util.Iterator;

public class AMPds2Reader extends DatasetReader {
    public AMPds2Reader(int startDayIndex) {
        super(startDayIndex);
    }

    @Override
    public Iterator<DatasetMetric> readFile(Reader reader) {
        return readSome(reader);
    }

    private Iterator<DatasetMetric> readSome(Reader reader) {
        CsvToBean<DatasetMetric> csvReader = new CsvToBeanBuilder(reader)
                .withSkipLines(calculateLinesToSkip())
                .withType(AMPds2Metric.class)
                .withSeparator(',')
                .withIgnoreQuotations(true)
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        Iterator<DatasetMetric> iterator = csvReader.iterator();
        return iterator;
    }

    /**
     * For AMPds2 dataset, each line is 1 minute, so we can calculate 1440 as one day. The first line
     * is the header, so we always skip it.
     * @return lines to skip on csv read
     */
    @Override
    protected int calculateLinesToSkip() {
        return 1440 * startDayIndex + 1;
    }
}
