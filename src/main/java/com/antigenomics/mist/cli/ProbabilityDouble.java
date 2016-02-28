package com.antigenomics.mist.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ProbabilityDouble implements IParameterValidator {

    @Override
    public void validate(String name, String value)
            throws ParameterException {
        double p = Integer.parseInt(value);
        if (p < 0 || p > 1) {
            throw new ParameterException("Parameter " + name
                    + " should in [0, 1] (found " + value +")");
        }
    }
}
