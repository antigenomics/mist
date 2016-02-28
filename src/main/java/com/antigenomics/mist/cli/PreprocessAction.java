package com.antigenomics.mist.cli;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import com.antigenomics.mist.cli.barcodes.BarcodesParser;
import com.antigenomics.mist.preprocess.*;
import com.antigenomics.mist.primer.PrimerSearcherArray;
import com.antigenomics.mist.umi.UmiCoverageAndQuality;
import com.antigenomics.mist.umi.UmiCoverageAndQualityWriter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader;
import com.milaboratory.core.io.sequence.fastq.SingleFastqReader;
import com.milaboratory.mitools.cli.Action;
import com.milaboratory.mitools.cli.ActionHelper;
import com.milaboratory.mitools.cli.ActionParameters;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.antigenomics.mist.cli.MistCLIUtil.*;

public class PreprocessAction implements Action {
    public static final String OUTPUT_SUFFIX = "pre",
            DISCARDED_SUFFIX = "pre" + SUFFIX_SEP + "bad";

    final PreprocessParameters actionParameters = new PreprocessParameters();

    @SuppressWarnings("unchecked")
    @Override
    public void go(ActionHelper helper) throws Exception {
        PrimerSearcherArray primerSearcherArray;
        try {
            primerSearcherArray = BarcodesParser.read(actionParameters.getBarcodesFileName());
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse barcodes file.", e);
        }

        SequenceReader reader;
        ReadGroomer readGroomer;
        if (actionParameters.isPaired()) {
            // TODO: Currently wildcard replacement is implemented in ReadWrapperFactory class
            // so we set wildcards processing to false
            reader = new PairedFastqReader(actionParameters.arguments.get(1),
                    actionParameters.arguments.get(2), false);
            readGroomer = new PairedReadGroomer(!actionParameters.noTrim);
        } else {
            reader = new SingleFastqReader(actionParameters.arguments.get(1), false);
            readGroomer = new SingleReadGroomer(!actionParameters.noTrim);
        }

        PreprocessorPipeline preprocessorPipeline = new PreprocessorPipeline(
                new SearchProcessor(new ReadWrapperFactory(!actionParameters.sameStrand),
                        primerSearcherArray),
                readGroomer
        );

        preprocessorPipeline.setInput(reader);

        if (actionParameters.discardedPath != null) {
            InputPort<SequenceRead> discardedPort = createAndWrapFastqWriter(actionParameters.discardedPath,
                    DISCARDED_SUFFIX, actionParameters.isPaired(), actionParameters.compress);
            preprocessorPipeline.setDiscarded(discardedPort);
        }

        MistCLIUtil.ensureDirExtists(actionParameters.outputPrefix);
        preprocessorPipeline.setOutput(createAndWrapFastqWriter(actionParameters.outputPrefix,
                OUTPUT_SUFFIX, actionParameters.isPaired(), actionParameters.compress));

        preprocessorPipeline.setNumberOfThreads(actionParameters.threads);

        preprocessorPipeline.run();

        PrintWriter printWriter = new PrintWriter(updatePath(actionParameters.outputPrefix,
                OUTPUT_SUFFIX + ".log"));
        printWriter.write(preprocessorPipeline
                .getSearchProcessor()
                .getPrimerSearcherArray()
                .toString());
        printWriter.close();

        UmiCoverageAndQualityWriter umiCoverageAndQualityWriter = new UmiCoverageAndQualityWriter(
                new FileOutputStream(updatePath(actionParameters.outputPrefix,
                        OUTPUT_SUFFIX + "_umi.log"))
        );
        OutputPort<UmiCoverageAndQuality> umiCoverageAndQualityOutputPort = preprocessorPipeline
                .getSearchProcessor()
                .getUmiAccumulator()
                .getOutputPort();

        CUtils.drain(umiCoverageAndQualityOutputPort,
                umiCoverageAndQualityWriter);
    }

    @Override
    public String command() {
        return "preprocess";
    }

    @Override
    public ActionParameters params() {
        return actionParameters;
    }

    @Parameters(commandDescription = ".", optionPrefixes = "-")
    public static final class PreprocessParameters extends ActionParameters {
        @Parameter(description = "config.json input_file_R1.fastq[.gz] [input_file_R2.fastq[.gz]]",
                variableArity = true)
        public List<String> arguments = new ArrayList<>();

        @Parameter(description = "Output path (path can end with a prefix). " +
                "Note that a mandatory '_" + OUTPUT_SUFFIX + "_R1[2].fastq[.gz]' suffix will be added to processed reads. " +
                "Output will also include UMI sequence summary file with '_" + OUTPUT_SUFFIX + "_umi.txt' suffix and " +
                "a log file with '_" + OUTPUT_SUFFIX + ".log' suffix.",
                names = {"-o", "--output-prefix"})
        String outputPrefix = ".";

        @Parameter(description = "Store discarded reads to FASTQ files in the specified path " +
                "(path can end with a prefix). " +
                "Note that a mandatory '_" + DISCARDED_SUFFIX + "_R1[2].fastq[.gz]' suffix will be added.",
                names = {"-d", "--discarded"})
        String discardedPath;

        @Parameter(description = "Compress output files.",
                names = {"-c", "--compress-output"})
        boolean compress;

        @Parameter(description = "Assume that reads are on the same strand " +
                "(opposite of raw Illumina reads orientation).",
                names = {"-ss", "--same-strand"})
        boolean sameStrand;

        @Parameter(description = "Do not trim primer and/or UMI sequences.",
                names = {"-nt", "--no-trim"})
        boolean noTrim;

        @Parameter(description = "Number of threads.",
                names = {"-t", "--threads"}, validateWith = PositiveInteger.class)
        int threads = Math.min(Runtime.getRuntime().availableProcessors(), 4);

        public boolean isPaired() {
            return arguments.size() == 3;
        }

        public String getBarcodesFileName() {
            return arguments.get(0);
        }

        public String getR1FileName() {
            return arguments.get(1);
        }

        public String getR2FileName() {
            if (!isPaired()) {
                throw new NotImplementedException();
            }
            return arguments.get(2);
        }

        @Override
        public void validate() {
            if (arguments.size() > 3 || arguments.size() < 2)
                throw new ParameterException("Wrong number of parameters.");

            if (!new File(getBarcodesFileName()).exists()) {
                throw new RuntimeException("Barcodes file " + getBarcodesFileName() + " does not exist.");
            }

            if (!new File(getR1FileName()).exists()) {
                throw new RuntimeException("FASTQ file " + getR1FileName() + " does not exist.");
            }

            if (isPaired() && !new File(getR2FileName()).exists()) {
                throw new RuntimeException("FASTQ file " + getR2FileName() + " does not exist.");
            }
        }
    }
}
