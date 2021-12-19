package com.company.yavlash.reader;

import com.company.yavlash.exception.RouteException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class BusReader {
    private static final Logger logger = LogManager.getLogger();

    public List<String> readBusData (String filename) throws RouteException {
        if (getClass().getClassLoader().getResource(filename) == null) {
            logger.log(Level.ERROR,"File \"{}\" doesn't exist in specified directory.", filename);
            throw new RouteException("File \"" + filename + "\" doesn't exist in specified directory.");
        }
        List<String> busesData;
        Stream<String> lines = Stream.<String>builder().build();
        try {
            Path pathToFile = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
            lines = Files.lines(pathToFile);
            busesData = lines.toList();
        } catch (IOException | URISyntaxException exception) {
            logger.log(Level.ERROR,"Error was found while extracting buses' data from the file  \"{}\"", filename);
            throw new RouteException("Error was found while extracting buses' data from the file  \"" + filename + "\"", exception);
        } finally {
            lines.close();
        }
        return busesData;
    }
}