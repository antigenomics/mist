package com.antigenomics.mist.cli;


import cc.redberry.pipe.InputPort;
import com.antigenomics.mist.umi.*;
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
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.antigenomics.mist.cli.MistCLIUtil.SUFFIX_SEP;
import static com.antigenomics.mist.cli.MistCLIUtil.createAndWrapFastqWriter;

public class CorrectAction implements Action {
    public static final String OUTPUT_SUFFIX = "cor",
            DISCARDED_SUFFIX = "cor" + SUFFIX_SEP + "bad";

    final CorrectorParameters actionParameters = new CorrectorParameters();

    @SuppressWarnings("unchecked")
    @Override
    public void go(ActionHelper helper) throws Exception {
        SequenceReader reader;
        UmiCorrector umiCorrector;
        if (actionParameters.isPaired()) {
            // TODO: Currently wildcard replacement is implemented in ReadWrapperFactory class
            // so we set wildcards processing to false
            reader = new PairedFastqReader(actionParameters.arguments.get(1),
                    actionParameters.arguments.get(2), false);
            umiCorrector = new UmiCorrectorPaired(new UmiCoverageAndQualityReader(
                    new FileInputStream(actionParameters.getUmisFileName())),
                    actionParameters.filterDecisionCoverageThreshold,
                    actionParameters.densityModelErrorThreshold,
                    actionParameters.maxMismatches,
                    actionParameters.errorPvalueThreshold,
                    actionParameters.independentAssemblyFdrThreshold);
        } else {
            reader = new SingleFastqReader(actionParameters.arguments.get(1), false);
            umiCorrector = new UmiCorrectorSingle(new UmiCoverageAndQualityReader(
                    new FileInputStream(actionParameters.getUmisFileName())),
                    actionParameters.filterDecisionCoverageThreshold,
                    actionParameters.densityModelErrorThreshold,
                    actionParameters.maxMismatches,
                    actionParameters.errorPvalueThreshold,
                    actionParameters.independentAssemblyFdrThreshold);
        }

        CorrectorPipeline correctorPipeline = new CorrectorPipeline(
                umiCorrector
        );

        correctorPipeline.setInput(reader);

        if (actionParameters.discardedPath != null) {
            InputPort<SequenceRead> discardedPort = createAndWrapFastqWriter(actionParameters.discardedPath,
                    DISCARDED_SUFFIX, actionParameters.isPaired(), actionParameters.compress);
            correctorPipeline.setDiscarded(discardedPort);
        }

        MistCLIUtil.ensureDirExtists(actionParameters.outputPrefix);
        correctorPipeline.setOutput(createAndWrapFastqWriter(actionParameters.outputPrefix,
                OUTPUT_SUFFIX, actionParameters.isPaired(), actionParameters.compress));

        correctorPipeline.setNumberOfThreads(actionParameters.threads);

        correctorPipeline.run();

        // todo:
        PrintWriter printWriter = new PrintWriter(MistCLIUtil.updatePath(actionParameters.outputPrefix,
                OUTPUT_SUFFIX + ".log"));
        printWriter.write(correctorPipeline.toString());
        printWriter.close();
    }

    @Override
    public String command() {
        return "correct";
    }

    @Override
    public ActionParameters params() {
        return actionParameters;
    }

    @Parameters(commandDescription = ".", optionPrefixes = "-")
    public static final class CorrectorParameters extends ActionParameters {
        @Parameter(description = PreprocessAction.OUTPUT_SUFFIX + MistCLIUtil.SUFFIX_SEP + "umi.txt " +
                PreprocessAction.OUTPUT_SUFFIX + MistCLIUtil.SUFFIX_SEP + "R1.fastq[.gz] " +
                "[" + PreprocessAction.OUTPUT_SUFFIX + MistCLIUtil.SUFFIX_SEP + "R2.fastq[.gz]]",
                variableArity = true)
        public List<String> arguments = new ArrayList<>();

        @Parameter(description = "Output path (path can end with a prefix). " +
                "Note that a mandatory '_" + OUTPUT_SUFFIX + "_R1[2].fastq[.gz]' suffix will be added to processed reads. " +
                "Output will also include a log file with '_" + OUTPUT_SUFFIX + ".log' suffix.",
                names = {"-o", "--output-prefix"})
        String outputPrefix = ".";

        @Parameter(description = "Store discarded reads to FASTQ files in the specified path " +
                "(path can end with a prefix). " +
                "Note that a mandatory '_" + DISCARDED_SUFFIX + "_R1[2].fastq[.gz]' suffix will be added.",
                names = {"-d", "--discarded"})
        String discardedPath;

        @Parameter(description = "Scan depth (max number of mismatches) for sequence-based UMI correction. " +
                "Should be set to 1 for most scenarios.",
                names = {"--depth"}, validateWith = PositiveInteger.class)
        int maxMismatches = UmiCorrector.MAX_MISMATCHES_DEFAULT;

        @Parameter(description = "P-value threshold for correction based on.",
                names = {"--error-pvalue-threshold"}, validateWith = ProbabilityDouble.class)
        double errorPvalueThreshold = UmiCorrector.ERROR_PVALUE_THRESHOLD;

        @Parameter(description = "FDR threshold for correction based on the probability of random " +
                "assembly of similar UMI sequences.",
                names = {"--assembly-fdr-threshold"}, validateWith = ProbabilityDouble.class)
        double independentAssemblyFdrThreshold = UmiCorrector.INDEPENDENT_FDR_ASSEMBLY_THRESHOLD;

        @Parameter(description = "Allow coverage-based filtering when global coverage threshold estimate reaches specified value.",
                names = {"--coverage-filter-triggers-at"}, validateWith = PositiveInteger.class)
        int filterDecisionCoverageThreshold = UmiCorrector.FILTER_DECISION_COVERAGE_THRESHOLD;

        @Parameter(description = "Classifier probability threshold for filtering based on UMI coverage distribution.",
                names = {"--coverage-classifier-threshold"}, validateWith = ProbabilityDouble.class)
        double densityModelErrorThreshold = UmiCorrector.DENSITY_MODEL_ERROR_THRESHOLD;

        @Parameter(description = "Compress output files.",
                names = {"-c", "--compress-output"})
        boolean compress;

        @Parameter(description = "Number of threads.",
                names = {"-t", "--threads"}, validateWith = PositiveInteger.class)
        int threads = Math.min(Runtime.getRuntime().availableProcessors(), 4);

        public boolean isPaired() {
            return arguments.size() == 3;
        }

        public String getUmisFileName() {
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

            if (!new File(getUmisFileName()).exists()) {
                throw new RuntimeException("UMI summary file " + getUmisFileName() + " does not exist.");
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
