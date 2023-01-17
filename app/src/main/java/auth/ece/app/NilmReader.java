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
import java.util.List;
import java.util.stream.Collectors;
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

    public List<EdfDataset> readFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            return readSome(reader);
        } catch (Exception e) {
            log.error("Exception occurred: " + e);
        }
        return List.of();
    }

    private List<EdfDataset> readSome(Reader reader) {
        CsvToBean<EdfDataset> csvReader = new CsvToBeanBuilder(reader)
                .withType(EdfDataset.class)
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        Stream<EdfDataset> stream = csvReader.stream();
        Stream<EdfDataset> limited = stream.limit(10);
        List<EdfDataset> datasetList = limited.collect(Collectors.toList());
        datasetList.forEach(metric -> {
            log.info(metric);
        });
        return datasetList;
    }
}
