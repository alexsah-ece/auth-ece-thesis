package auth.ece.app;

import auth.ece.app.model.EdfDataset;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.log4j.Log4j2;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Log4j2
public class NilmReader {

    private CSVParser parser;

    public NilmReader() {
        parser = new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .build();
    }

    public void readFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            readSome(reader);
        } catch (Exception e) {
            log.error("Exception occurred: " + e);
        }
    }

    private void readSome(Reader reader) {
        CsvToBean<EdfDataset> csvReader = new CsvToBeanBuilder(reader)
                .withType(EdfDataset.class)
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        Stream<EdfDataset> stream = csvReader.stream();
        stream.limit(10)
                .forEach(metric -> {
                    log.info(metric);
                });
    }
}
