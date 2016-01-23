/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * Source:
 * http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/jsr166e/extra/AtomicDoubleArray.java?revision=1.5
 * (Modified to adapt to guava coding conventions and
 * to use AtomicLongArray instead of sun.misc.Unsafe)
 */

/*
 * Re-implemented to handle float values
 */

package com.antigenomics.mist.misc;


import java.util.concurrent.atomic.AtomicIntegerArray;

import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

/**
 * A {@code float} array in which elements may be updated atomically.
 * See the {@link java.util.concurrent.atomic} package specification
 * for description of the properties of atomic variables.
 * <p/>
 * <p><a name="bitEquals">This class compares primitive {@code float}
 * values in methods such as {@link #compareAndSet} by comparing their
 * bitwise representation using {@link Float#floatToRawIntBits},
 * which differs from both the primitive float {@code ==} operator
 * and from {@link Float#equals}, as if implemented by:
 * <pre> {@code
 * static boolean bitEquals(float x, float y) {
 *   long xBits = Float.floatToRawIntBits(x);
 *   long yBits = Float.floatToRawIntBits(y);
 *   return xBits == yBits;
 * }}</pre>
 *
 * @author Doug Lea
 * @author Martin Buchholz
 * @since 11.0
 */
public class AtomicFloatArray implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    // Making this non-final is the lesser evil according to Effective
    // Java 2nd Edition Item 76: Write readObject methods defensively.
    private transient AtomicIntegerArray ints;

    /**
     * Creates a new {@code AtomicFloatArray} of the given length,
     * with all elements initially zero.
     *
     * @param length the length of the array
     */
    public AtomicFloatArray(int length) {
        this.ints = new AtomicIntegerArray(length);
    }

    /**
     * Creates a new {@code AtomicFloatArray} with the same length
     * as, and all elements copied from, the given array.
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicFloatArray(float[] array) {
        final int len = array.length;
        int[] intarray = new int[len];
        for (int i = 0; i < len; i++) {
            intarray[i] = floatToRawIntBits(array[i]);
        }
        this.ints = new AtomicIntegerArray(intarray);
    }

    /**
     * Returns the length of the array.
     *
     * @return the length of the array
     */
    public final int length() {
        return ints.length();
    }

    /**
     * Gets the current value at position {@code i}.
     *
     * @param i the index
     * @return the current value
     */
    public final float get(int i) {
        return intBitsToFloat(ints.get(i));
    }

    /**
     * Sets the element at position {@code i} to the given value.
     *
     * @param i        the index
     * @param newValue the new value
     */
    public final void set(int i, float newValue) {
        int next = floatToRawIntBits(newValue);
        ints.set(i, next);
    }

    /**
     * Eventually sets the element at position {@code i} to the given value.
     *
     * @param i        the index
     * @param newValue the new value
     */
    public final void lazySet(int i, float newValue) {
        int next = floatToRawIntBits(newValue);
        ints.lazySet(i, next);
    }

    /**
     * Atomically sets the element at position {@code i} to the given value
     * and returns the old value.
     *
     * @param i        the index
     * @param newValue the new value
     * @return the previous value
     */
    public final float getAndSet(int i, float newValue) {
        int next = floatToRawIntBits(newValue);
        return intBitsToFloat(ints.getAndSet(i, next));
    }

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value
     * if the current value is <a href="#bitEquals">bitwise equal</a>
     * to the expected value.
     *
     * @param i      the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(int i, float expect, float update) {
        return ints.compareAndSet(i,
                floatToRawIntBits(expect),
                floatToRawIntBits(update));
    }

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value
     * if the current value is <a href="#bitEquals">bitwise equal</a>
     * to the expected value.
     * <p/>
     * <p>May <a
     * href="http://download.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/package-summary.html#Spurious">
     * fail spuriously</a>
     * and does not provide ordering guarantees, so is only rarely an
     * appropriate alternative to {@code compareAndSet}.
     *
     * @param i      the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful
     */
    public final boolean weakCompareAndSet(int i, float expect, float update) {
        return ints.weakCompareAndSet(i,
                floatToRawIntBits(expect),
                floatToRawIntBits(update));
    }

    /**
     * Atomically adds the given value to the element at index {@code i}.
     *
     * @param i     the index
     * @param delta the value to add
     * @return the previous value
     */
    public final float getAndAdd(int i, float delta) {
        while (true) {
            int current = ints.get(i);
            float currentVal = intBitsToFloat(current);
            float nextVal = currentVal + delta;
            int next = floatToRawIntBits(nextVal);
            if (ints.compareAndSet(i, current, next)) {
                return currentVal;
            }
        }
    }

    /**
     * Atomically adds the given value to the element at index {@code i}.
     *
     * @param i     the index
     * @param delta the value to add
     * @return the updated value
     */
    public float addAndGet(int i, float delta) {
        while (true) {
            int current = ints.get(i);
            float currentVal = intBitsToFloat(current);
            float nextVal = currentVal + delta;
            int next = floatToRawIntBits(nextVal);
            if (ints.compareAndSet(i, current, next)) {
                return nextVal;
            }
        }
    }

    /**
     * Returns the String representation of the current values of array.
     *
     * @return the String representation of the current values of array
     */
    public String toString() {
        int iMax = length() - 1;
        if (iMax == -1) {
            return "[]";
        }

        // Float.toString((float)Math.PI).length() == 9
        StringBuilder b = new StringBuilder((9 + 2) * (iMax + 1));
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(intBitsToFloat(ints.get(i)));
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(',').append(' ');
        }
    }

    /**
     * Saves the state to a stream (that is, serializes it).
     *
     * @serialData The length of the array is emitted (int), followed by all
     * of its elements (each a {@code float}) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();

        // Write out array length
        int length = length();
        s.writeInt(length);

        // Write out all elements in the proper order.
        for (int i = 0; i < length; i++) {
            s.writeFloat(get(i));
        }
    }

    /**
     * Reconstitutes the instance from a stream (that is, deserializes it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Read in array length and allocate array
        int length = s.readInt();
        this.ints = new AtomicIntegerArray(length);

        // Read in all elements in the proper order.
        for (int i = 0; i < length; i++) {
            set(i, s.readFloat());
        }
    }
}
