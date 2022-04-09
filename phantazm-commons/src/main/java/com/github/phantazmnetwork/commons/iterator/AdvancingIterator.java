package com.github.phantazmnetwork.commons.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>An implementation of {@link Iterator} that uses an alternate mechanism for iteration called <i>advancing</i>.
 * Instead of separating the logic for determining if an element exists and actually computing it into two methods
 * ({@link Iterator#hasNext()} and {@link Iterator#next()}, respectively), AdvancingIterator uses a single method
 * {@code advance()} that tries to compute the next value as well as reporting if said computation was successful.</p>
 *
 * <p>This class does not serve as a general replacement or improvement upon the design pattern typically used for
 * iterators. Notably, it violates the generally-accepted "rule" that {@code hasNext} should be stateless. However, it
 * can be very useful in instances which the logic for determining <i>if</i> an element exists and the logic for
 * actually <i>retrieving</i> that element are both similar and equally expensive (for example, if return values are
 * being calculated on the fly, and there is no efficient way to determine beforehand that said calculation will or will
 * not succeed).</p>
 *
 * <p>As an Iterator, AdvancingIterator acts as expected. Calls to {@code hasNext()} should never fail; calls to
 * {@code next()} may fail with a {@link NoSuchElementException} if there is no cached value and a call to
 * {@code advance()} fails. AdvancingIterator does not itself implement {@link Iterator#remove()}, as its typical use
 * case (sequential computation of elements not associated with a collection) does not call for it.</p>
 * @param <TValue> the type of value returned by {@link Iterator#next()}
 */
public abstract class AdvancingIterator<TValue> implements EnhancedIterator<TValue> {
    //use boolean flag to indicate presence of a value: null values are allowed
    private boolean hasValue;

    /**
     * The value cached by this AdvancingIterator. May represent a null value, or be set to null after a call to
     * {@code next()}.
     */
    protected TValue value;

    @Override
    public boolean hasNext() {
        //handle redundant calls to hasNext if the next value exists and has not been consumed by returning true
        if(hasValue) {
            return true;
        }

        return hasValue = advance();
    }

    @Override
    public TValue next() {
        //we don't currently have a value
        if(!hasValue) {
            //so try to advance; if we fail to advance in next() then throw an exception because we're out of elements
            if(!advance()) {
                value = null;
                throw new NoSuchElementException();
            }
        }

        //reset hasValue so we try to re-compute
        hasValue = false;

        //copy value and set field to null to avoid maintaining a persistent reference
        TValue valueCopy = value;
        value = null;

        return valueCopy;
    }

    /**
     * <p>Advances this iterator. If this method is able to compute a value, it will be stored in the protected field
     * {@code value}. If it is unable, {@code value} will remain unchanged. When {@code next()} is called, {@code value}
     * will be returned.</p>
     *
     * <p>This method may be called when either {@code hasNext()} or {@code next()} are called. If {@code hasNext()} is
     * called, and this method returns true, subsequent calls to {@code hasNext()} before {@code next()} will <i>not</i>
     * call {@code advance} again ({@code hasNext()} will return true). {@code next()} <i>may</i> invoke this method,
     * but it will only do so if there has been no previous call to {@code hasNext()} that returned true. This
     * corresponds to "unsafe" {@code next()} calls that are performed without checking the result of {@code hasNext()}.
     * Otherwise, calls to {@code next()} will use the value computed by this method when {@code hasNext()} was called
     * last.</p>
     *
     * <p>Implementations should take care to ensure that this method cannot throw runtime exceptions. Doing so can
     * violate the general expectation that {@code hasNext()} may be called without the possibility of an exception
     * being thrown.</p>
     * @return true if this method assigned an element (or null) to {@code value}; false otherwise
     */
    public abstract boolean advance();
}
