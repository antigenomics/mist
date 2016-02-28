package com.antigenomics.mist.cli;

import cc.redberry.pipe.InputPort;
import com.antigenomics.mist.SequenceWriterWrapper;
import com.milaboratory.core.io.CompressionType;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.fastq.PairedFastqWriter;
import com.milaboratory.core.io.sequence.fastq.QualityFormat;
import com.milaboratory.core.io.sequence.fastq.SingleFastqWriter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class MistCLIUtil {
    public static final String SUFFIX_SEP = "_";

    public static void ensureDirExtists(String outputPrefix) {
        String dir = FilenameUtils.getFullPathNoEndSeparator(new File(outputPrefix).getAbsolutePath());

        if (!new File(dir).mkdirs()) {
            throw new RuntimeException("Unable to create directory " + dir);
        }
    }

    public static String updatePath(String path, String suffix) {
        return updatePath(path, suffix, false);
    }


    public static String updatePath(String path, String suffix, boolean compressed) {
        if (path.equals(".") || path.endsWith(File.separator + ".")) {
            path = path.substring(0, path.length() - 1);
        }
        String newPath = path.endsWith(File.separator) ? path + suffix : path + SUFFIX_SEP + suffix;
        return compressed ? newPath + ".gz" : newPath;
    }

    @SuppressWarnings("unchecked")
    public static InputPort<SequenceRead> createAndWrapFastqWriter(String path, String suffix, boolean paired,
                                                                   boolean compress) {
        ensureDirExtists(path);
        InputPort<SequenceRead> port;
        try {
            port = new SequenceWriterWrapper(paired ?
                    new PairedFastqWriter(
                            updatePath(path,
                                    suffix + SUFFIX_SEP + "R1.fastq",
                                    compress),
                            updatePath(path,
                                    suffix + SUFFIX_SEP + "R2.fastq",
                                    compress),
                            QualityFormat.Phred33,
                            compress ? CompressionType.GZIP : CompressionType.None) :
                    new SingleFastqWriter(
                            updatePath(path,
                                    suffix + SUFFIX_SEP + "R1.fastq",
                                    compress),
                            QualityFormat.Phred33,
                            compress ? CompressionType.GZIP : CompressionType.None));
        } catch (Exception e) {
            throw new RuntimeException("Unable to create FASTQ writer", e);
        }
        return port;
    }
}
