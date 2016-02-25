package com.antigenomics.mist.cli;

import cc.redberry.pipe.InputPort;
import com.antigenomics.mist.cli.barcodes.BarcodesParser;
import com.antigenomics.mist.preprocess.*;
import com.antigenomics.mist.primer.PrimerSearcherArray;
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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PreprocessAction implements Action {
    final PreprocessParameters actionParameters = new PreprocessParameters();

    // TODO: ensure output dir implementation

    @SuppressWarnings("unchecked")
    @Override
    public void go(ActionHelper helper) throws Exception {
        // TODO: Currently wildcard replacement is implemented in ReadWrapperFactory class
        SequenceReader reader;
        ReadGroomer readGroomer;

        if (!new File(actionParameters.getBarcodesFileName()).exists()) {
            throw new RuntimeException("Barcodes file " + actionParameters.getBarcodesFileName() + " does not exist.");
        }

        PrimerSearcherArray primerSearcherArray;
        try {
            primerSearcherArray = BarcodesParser.read(actionParameters.getBarcodesFileName());
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse barcodes file.", e);
        }

        if (!new File(actionParameters.getR1FileName()).exists()) {
            throw new RuntimeException("FASTQ file " + actionParameters.getR1FileName() + " does not exist.");
        }

        if (actionParameters.isPaired() && !new File(actionParameters.getR2FileName()).exists()) {
            throw new RuntimeException("FASTQ file " + actionParameters.getR2FileName() + " does not exist.");
        }

        if (actionParameters.isPaired()) {
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

        if (actionParameters.discardedPrefix != null) {
            InputPort<SequenceRead> discardedPort = parseDiscarded();
            MistCLIUtil.ensureDirExtists(actionParameters.discardedPrefix);
            preprocessorPipeline.setDiscarded(discardedPort);
        }

        MistCLIUtil.ensureDirExtists(actionParameters.outputPrefix);
        preprocessorPipeline.setOutput(parseOutput());

        preprocessorPipeline.setNumberOfThreads(actionParameters.threads);

        preprocessorPipeline.run();

        if (actionParameters.logFileName != null) {
            PrintWriter printWriter = new PrintWriter(actionParameters.logFileName);
            printWriter.write(preprocessorPipeline.toString());
            printWriter.close();
        }
    }

    private InputPort<SequenceRead> parseDiscarded() {
        throw new NotImplementedException();
    }

    private InputPort<SequenceRead> parseOutput() {
        throw new NotImplementedException();
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

        @Parameter(description = "Store log to specified file name.",
                names = {"-l", "--log"})
        String logFileName;

        @Parameter(description = "Prefix for output files.",
                names = {"-o", "--output-prefix"})
        String outputPrefix = "mist_pre";

        @Parameter(description = "Store discarded reads to FASTQ files with a specified prefix.",
                names = {"-d", "--discarded"})
        String discardedPrefix;

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
        }
    }
}
