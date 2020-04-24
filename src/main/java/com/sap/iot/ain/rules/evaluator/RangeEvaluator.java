package com.sap.iot.ain.rules.evaluator;

import com.google.common.collect.Range;

public class RangeEvaluator {

    public <T extends Comparable<T>> Range<T> getRange(T rangeUpper, T rangeLower) {
        if (rangeLower != null && rangeUpper != null) {
            return Range.closed(rangeLower, rangeUpper); //Returns a range that contains all values greater than or equal to lower and less than or equal to upper.
        } else if (rangeUpper != null && rangeLower == null) {
            return Range.atMost(rangeUpper); //Returns a range that contains all values less than or equal to rangeUpper.
        } else if (rangeUpper == null && rangeLower != null) {
            return Range.atLeast(rangeLower); // Returns a range that contains all values greater than or equal to rangeLower.
        }
        return null;
    }

}
