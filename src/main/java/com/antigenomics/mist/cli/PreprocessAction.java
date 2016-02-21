package com.antigenomics.mist.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.mitools.cli.Action;
import com.milaboratory.mitools.cli.ActionHelper;
import com.milaboratory.mitools.cli.ActionParameters;
import com.milaboratory.mitools.cli.MiCLIUtil;

import java.util.ArrayList;
import java.util.List;

public class PreprocessAction implements Action {
    @Override
    public void go(ActionHelper helper) throws Exception {

    }

    @Override
    public String command() {
        return null;
    }

    @Override
    public ActionParameters params() {
        return null;
    }

    @Parameters(commandDescription = ".", optionPrefixes = "-")
    public static final class MergingParameters extends ActionParameters {
        @Parameter(description = "input_file_R1.fastq[.gz] input_file_R2.fastq[.gz] -|output_file.fastq[.gz]", variableArity = true)
        public List<String> parameters = new ArrayList<>();

        @Parameter(description = "FASTQ file to put non-overlapped forward reads (supported formats: " +
                "*.fastq and *.fastq.gz).",
                names = {"-nf", "--negative-forward"},
                converter = MiCLIUtil.FileConverter.class)
        File forwardNegative;

        @Parameter(description = "FASTQ file to put non-overlapped reverse reads (supported formats: " +
                "*.fastq and *.fastq.gz).",
                names = {"-nr", "--negative-reverse"},
                converter = MiCLIUtil.FileConverter.class)
        File reverseNegative;

        @Parameter(description = "Report file.",
                names = {"-r", "--report"})
        String report;

        //@Parameter(description = "Output FASTQ file or \"-\" for STDOUT to put non-overlapped " +
        //        "reverse reads (supported formats: *.fastq and *.fastq.gz).",
        //        names = {"-o", "--out"},
        //        required = true)
        //String output;

        @Parameter(description = "Include both paired-end reads for pairs where no overlap was found.",
                names = {"-i", "--include-non-overlapped"})
        boolean includeNonOverlapped;

        @Parameter(description = "Discard original sequence header and put sequential ids.",
                names = {"-d", "--discard-header"})
        boolean discardHeader;

        @Parameter(description = "Assume that reads are on the same strand (opposite of raw Illumina reads orientation).",
                names = {"-ss", "--same-strand"})
        boolean sameStrand;

        @Parameter(description = "Possible values: 'max' - take maximal score value in letter conflicts, 'sub' - " +
                "subtract minimal quality from maximal",
                names = {"-q", "--quality-merging-algorithm"})
        String qualityMergingAlgorithm = QualityMergingAlgorithm.SumSubtraction.cliName;

        @Parameter(description = "Minimal overlap.",
                names = {"-p", "--overlap"}, validateWith = PositiveInteger.class)
        int overlap = 15;

        @Parameter(description = "Minimal allowed similarity",
                names = {"-s", "--similarity"})
        double similarity = 0.85;

        @Parameter(description = "Maximal quality to set for letters within overlap.",
                names = {"-m", "--max-quality"})
        int maxQuality = MergerParameters.DEFAULT_MAX_QUALITY_VALUE;

        @Parameter(description = "Threads",
                names = {"-t", "--threads"}, validateWith = PositiveInteger.class)
        int threads = Math.min(Runtime.getRuntime().availableProcessors(), 4);

        public String getR1() {
            return parameters.get(0);
        }

        public String getR2() {
            return parameters.get(1);
        }

        public String getOutput() {
            return parameters.size() == 2 ? "-" : parameters.get(2);
        }

        public QualityMergingAlgorithm getQualityMergingAlgorithm() {
            return QualityMergingAlgorithm.getFromCLIName(qualityMergingAlgorithm);
        }

        @Override
        public void validate() {
            if (parameters.size() > 3 || parameters.size() < 2)
                throw new ParameterException("Wrong number of parameters.");
        }
    }
}
