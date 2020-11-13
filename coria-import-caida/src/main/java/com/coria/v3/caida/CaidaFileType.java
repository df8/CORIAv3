package com.coria.v3.caida;

import com.coria.v3.utility.UploadedFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by David Fradin, 2020
 */
public class CaidaFileType {
    public enum FileType {AStoAS_Links, AStoOrganizationMapping, AStoTypeMapping, ASLinkToGeoLocationMapping, ASLocationsList, ASOrganizationsList, Unknown}

    public static final Predicate<String> REGEX_AStoAS_Links_LINE = Pattern.compile("^[DI](?:\\t[\\d_,]+){2}(?:\\t\\d+)*").asMatchPredicate();
    public static final Predicate<String> REGEX_AStoOrganizationMapping_LINE = Pattern.compile("^[\\d_]+\\|[\\d]*\\|[\\w-/ ]*\\|[\\w-@]+\\|[\\w]*\\|(?:ARIN|RIPE|APNIC|LACNIC|AFRINIC|JPNIC)$").asMatchPredicate();
    public static final Predicate<String> REGEX_AStoTypeMapping_LINE = Pattern.compile("^[\\d_]+\\|[\\w]*_class\\|(?:Content|Enterprise|Transit/Access)$").asMatchPredicate();
    public static final Predicate<String> REGEX_ASLinkToGeoLocationMapping_LINE = Pattern.compile("^\\d+\\|\\d+(?:\\|[\\w -]+,(?:(?:bc|mlp|edge|lg),?)+)+$").asMatchPredicate();
    public static final Predicate<String> REGEX_ASLocationsList_LINE = Pattern.compile("^[\\w\\s-]+\\|[\\w]{0,2}\\|[\\w]{2}\\|[\\w\\s?]*\\|[^|]+(?:\\|[+-]?[0-9]+([.][0-9]*)?|[.][0-9]+){2}\\|[\\d]+$").asMatchPredicate();
    public static final Predicate<String> REGEX_ASOrganizationsList_LINE = Pattern.compile("^[\\w-@]+\\|[\\d]*\\|[^|]*\\|[\\w]{2}\\|(?:ARIN|RIPE|APNIC|LACNIC|AFRINIC|JPNIC)$").asMatchPredicate();

    public static FileType[] classifyFiles(List<UploadedFile> files) throws Exception {
        CaidaFileType.FileType[] fileTypes = new CaidaFileType.FileType[files.size()];
        FileType[] fileTypeValues = FileType.values();
        final int MIN_COUNT_MATCHES = 8;
        for (int i = 0; i < files.size(); i++) {
            fileTypes[i] = CaidaFileType.FileType.Unknown;
            int[] matchCounts = new int[]{0, 0, 0, 0, 0, 0};
            try (
                    InputStream is = files.get(i).getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr)) {
                String line;

                while ((line = br.readLine()) != null) {
                    if (REGEX_AStoAS_Links_LINE.test(line))
                        matchCounts[0]++;
                    if (REGEX_AStoOrganizationMapping_LINE.test(line))
                        matchCounts[1]++;
                    if (REGEX_AStoTypeMapping_LINE.test(line))
                        matchCounts[2]++;
                    if (REGEX_ASLinkToGeoLocationMapping_LINE.test(line))
                        matchCounts[3]++;
                    if (REGEX_ASLocationsList_LINE.test(line))
                        matchCounts[4]++;
                    if (REGEX_ASOrganizationsList_LINE.test(line))
                        matchCounts[5]++;
                    boolean minCountMatchesReached = false;
                    for (int matchCount : matchCounts) {
                        if (matchCount >= MIN_COUNT_MATCHES) {
                            minCountMatchesReached = true;
                            break;
                        }
                    }
                    if (minCountMatchesReached)
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Search for the file type with the most matches.
            int maxAt = 0;
            for (int j = 1; j < matchCounts.length; j++)
                maxAt = matchCounts[j] > matchCounts[maxAt] ? j : maxAt;
            if (matchCounts[maxAt] > 0) {
                fileTypes[i] = fileTypeValues[maxAt];
            } else {
                //there is an unclassified file
                throw new Exception("Unsupported file format: " + files.get(i).getSubmittedFileName());
            }
        }
        return fileTypes;
    }

    public interface ProcessLineConsumer {
        void processLine(String line) throws Exception;
    }

    public static void processFileType(List<UploadedFile> files, CaidaFileType.FileType[] fileTypes, CaidaFileType.FileType fileType, String[] ignorePatterns, boolean throwOnInvalidFormat, Predicate<String> predicate, ProcessLineConsumer consumer) throws Exception {
        if (ignorePatterns == null)
            ignorePatterns = new String[]{"#"};//default comment prefix
        for (int i = 0; i < files.size(); i++) {
            if (fileTypes[i] == fileType) {
                try (
                        InputStream is = files.get(i).getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (Arrays.stream(ignorePatterns).noneMatch(line::startsWith))
                            if (predicate.test(line)) {
                                consumer.processLine(line);
                            } else if (throwOnInvalidFormat) {
                                throw new Exception(String.format("Invalid format in file: %s, line: \"%s\"", files.get(i).getSubmittedFileName(), line));
                            }
                    }
                }
            }
        }
    }
}
